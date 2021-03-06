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

import static com.google.common.base.Preconditions.checkState;

import org.jclouds.reflect.Invocation;

import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Allows the provider to supply a value set in a threadlocal.
 * 
 * @author Adrian Cole
 */
public class SetCaller {

   private final ThreadLocal<TypeToken<?>> callerEnclosingType = new ThreadLocal<TypeToken<?>>();
   private final ThreadLocal<Invocation> caller = new ThreadLocal<Invocation>();

   public void enter(TypeToken<?> callerEnclosingType, Invocation caller) {
      checkState(this.callerEnclosingType.get() == null, "A scoping block is already in progress");
      this.callerEnclosingType.set(callerEnclosingType);
      this.caller.set(caller);
   }

   public void exit() {
      checkState(caller.get() != null, "No scoping block in progress");
      callerEnclosingType.remove();
      caller.remove();
   }

   public static class Module extends AbstractModule {
      public void configure() {
         SetCaller delegateScope = new SetCaller();
         bind(CALLER_ENCLOSING_TYPE).toProvider(delegateScope.new CallerEnclosingTypeProvider());
         bind(CALLER_INVOCATION).toProvider(delegateScope.new CallerInvocationProvider());
         bind(SetCaller.class).toInstance(delegateScope);
      }
   }

   private static final Key<TypeToken<?>> CALLER_ENCLOSING_TYPE = Key.get(new TypeLiteral<TypeToken<?>>() {
   }, Names.named("caller"));

   private static final Key<Invocation> CALLER_INVOCATION = Key.get(new TypeLiteral<Invocation>() {
   }, Names.named("caller"));

   class CallerEnclosingTypeProvider implements Provider<TypeToken<?>> {
      @Override
      public TypeToken<?> get() {
         return callerEnclosingType.get();
      }
   }

   class CallerInvocationProvider implements Provider<Invocation> {
      @Override
      public Invocation get() {
         return caller.get();
      }
   }

}
