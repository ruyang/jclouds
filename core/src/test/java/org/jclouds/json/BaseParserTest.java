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
package org.jclouds.json;

import static org.testng.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.config.SaxParserModule;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.json.config.GsonModule;
import org.jclouds.reflect.Invocation;
import com.google.common.reflect.Invokable;
import org.jclouds.rest.internal.TransformerForRequest;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * 
 * @author Adrian Cole
 */
public abstract class BaseParserTest<T, G> {

   @Retention(value = RetentionPolicy.RUNTIME)
   @Target(value = { ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
   @Qualifier
   public @interface Nested {

   }

   @SuppressWarnings("unchecked")
   protected Function<HttpResponse, T> parser(Injector i) {
      TypeToken<TransformerForRequest<T>> token = new TypeToken<TransformerForRequest<T>>() {
         private static final long serialVersionUID = 1L;
      }.where(new TypeParameter<T>() {
      }, new TypeToken<T>(getClass()) {
         private static final long serialVersionUID = 1L;
      });
      Key<TransformerForRequest<T>> xform = (Key<TransformerForRequest<T>>) Key.get(token.getType());
      try {
         return (Function<HttpResponse, T>) i
               .createChildInjector(new SaxParserModule())
               .getInstance(xform)
               .getTransformerForMethod(
                     Invocation.create(Invokable.from(getClass().getMethod("expected")), ImmutableList.of()), i);
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
   }

   @Test
   public void test() {
      T expects = expected();
      Function<HttpResponse, T> parser = parser(injector());
      T response = parser.apply(HttpResponse.builder().statusCode(200).message("ok").payload(payload()).build());
      compare(expects, response);
   }

   protected Payload payload() {
      return Payloads.newInputStreamPayload(getClass().getResourceAsStream(resource()));
   }

   public void compare(T expects, T response) {
      assertEquals(response.toString(), expects.toString());
   }

   protected Injector injector() {
      return Guice.createInjector(new GsonModule() {
         protected void configure() {
            bind(DateAdapter.class).to(Iso8601DateAdapter.class);
            super.configure();
         }
      });

   }

   protected String resource() {
      throw new IllegalStateException("please define resource such as \"/testaddresses.json\"");
   }

   public abstract T expected();
}
