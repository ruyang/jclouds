/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.rest.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.util.concurrent.Atomics.newReference;
import static org.jclouds.Constants.PROPERTY_TIMEOUTS_PREFIX;
import static org.jclouds.rest.config.BinderUtils.bindHttpApi;
import static org.jclouds.util.Maps2.transformKeys;
import static org.jclouds.util.Predicates2.startsWith;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.functions.IdentityFunction;
import org.jclouds.http.functions.config.SaxParserModule;
import org.jclouds.internal.FilterStringsBoundToInjectorByName;
import org.jclouds.json.config.GsonModule;
import org.jclouds.location.config.LocationModule;
import com.google.common.reflect.Invokable;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.HttpAsyncClient;
import org.jclouds.rest.HttpClient;
import org.jclouds.rest.binders.BindToJsonPayloadWrappedWith;
import org.jclouds.rest.internal.BlockOnFuture;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class RestModule extends AbstractModule {

   public static final TypeLiteral<Supplier<URI>> URI_SUPPLIER_TYPE = new TypeLiteral<Supplier<URI>>() {
   };

   protected final Map<Class<?>, Class<?>> sync2Async;
   protected final AtomicReference<AuthorizationException> authException = newReference();

   public RestModule() {
      this(ImmutableMap.<Class<?>, Class<?>> of());
   }

   private static final Set<Method> objectMethods = ImmutableSet.copyOf(Object.class.getMethods());

   public RestModule(Map<Class<?>, Class<?>> sync2Async) {
      this.sync2Async = sync2Async;
   }
   
   /**
    * seeds well-known invokables.
    */
   @Provides
   @Singleton
   protected Cache<Invokable<?, ?>, Invokable<?, ?>> seedKnownSync2AsyncInvokables() {
      Cache<Invokable<?, ?>, Invokable<?, ?>> sync2AsyncBuilder = CacheBuilder.newBuilder().build();
      putInvokables(TypeToken.of(HttpClient.class), TypeToken.of(HttpAsyncClient.class), sync2AsyncBuilder);
      for (Class<?> s : sync2Async.keySet()) {
         putInvokables(TypeToken.of(s), TypeToken.of(sync2Async.get(s)), sync2AsyncBuilder);
      }
      return sync2AsyncBuilder;
   }

   // accessible for ClientProvider
   public static void putInvokables(TypeToken<?> sync, TypeToken<?> async, Cache<Invokable<?, ?>, Invokable<?, ?>> cache) {
      for (Method invoked : sync.getRawType().getMethods()) {
         if (!objectMethods.contains(invoked)) {
            try {
               Method delegatedMethod = async.getRawType().getMethod(invoked.getName(), invoked.getParameterTypes());
               checkArgument(Arrays.equals(delegatedMethod.getExceptionTypes(), invoked.getExceptionTypes()),
                     "invoked %s has different typed exceptions than delegated invoked %s", invoked, delegatedMethod);
               invoked.setAccessible(true);
               delegatedMethod.setAccessible(true);
               cache.put(Invokable.from(invoked), Invokable.from(delegatedMethod));
            } catch (SecurityException e) {
               throw propagate(e);
            } catch (NoSuchMethodException e) {
               throw propagate(e);
            }
         }
      }
   }

   protected void installLocations() {
      install(new LocationModule());
   }

   @Override
   protected void configure() {
      bind(new TypeLiteral<Map<Class<?>, Class<?>>>() {
      }).toInstance(sync2Async);
      install(new SaxParserModule());
      install(new GsonModule());
      install(new SetCaller.Module());
      install(new FactoryModuleBuilder().build(BindToJsonPayloadWrappedWith.Factory.class));
      install(new FactoryModuleBuilder().build(BlockOnFuture.Factory.class));
      bind(IdentityFunction.class).toInstance(IdentityFunction.INSTANCE);
      bindHttpApi(binder(), HttpClient.class, HttpAsyncClient.class);
      // this will help short circuit scenarios that can otherwise lock out users
      bind(new TypeLiteral<AtomicReference<AuthorizationException>>() {
      }).toInstance(authException);
      bind(new TypeLiteral<Function<Predicate<String>, Map<String, String>>>() {
      }).to(FilterStringsBoundToInjectorByName.class);
      bind(new TypeLiteral<Function<Predicate<String>, Map<String, String>>>() {
      }).to(FilterStringsBoundToInjectorByName.class);
      installLocations();
   }

   @Provides
   @Singleton
   @Named("TIMEOUTS")
   protected Map<String, Long> timeouts(Function<Predicate<String>, Map<String, String>> filterStringsBoundByName) {
      Map<String, String> stringBoundWithTimeoutPrefix = filterStringsBoundByName
            .apply(startsWith(PROPERTY_TIMEOUTS_PREFIX));
      Map<String, Long> longsByName = transformValues(stringBoundWithTimeoutPrefix, new Function<String, Long>() {
         public Long apply(String input) {
            return Long.valueOf(String.valueOf(input));
         }
      });
      return transformKeys(longsByName, new Function<String, String>() {
         public String apply(String input) {
            return input.replaceFirst(PROPERTY_TIMEOUTS_PREFIX, "");
         }
      });

   }
}
