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

import java.lang.reflect.Proxy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.rest.internal.DelegatesToInvocationFunction;
import org.jclouds.rest.internal.InvokeHttpMethod;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * 
 * @author Adrian Cole
 */
@Singleton
public class AsyncHttpApiProvider<A> implements Provider<A> {
   private final Class<? super A> asyncApiType;
   private final DelegatesToInvocationFunction<A, A, InvokeHttpMethod<A, A>> httpInvoker;

   @Inject
   private AsyncHttpApiProvider(DelegatesToInvocationFunction<A, A, InvokeHttpMethod<A, A>> httpInvoker,
         TypeLiteral<A> asyncApiType) {
      this.httpInvoker = httpInvoker;
      this.asyncApiType = asyncApiType.getRawType();
   }

   @SuppressWarnings("unchecked")
   @Override
   public A get() {
      return (A) Proxy.newProxyInstance(asyncApiType.getClassLoader(), new Class<?>[] { asyncApiType }, httpInvoker);
   }
}
