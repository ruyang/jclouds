/*
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

package org.jclouds.googlecompute.features;

import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.collect.PagedIterable;
import org.jclouds.googlecompute.domain.Kernel;
import org.jclouds.googlecompute.domain.ListPage;
import org.jclouds.googlecompute.functions.internal.ParseKernels;
import org.jclouds.googlecompute.options.ListOptions;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.oauth.v2.config.OAuthScopes;
import org.jclouds.oauth.v2.filters.OAuthAuthenticator;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SkipEncoding;
import org.jclouds.rest.annotations.Transform;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static org.jclouds.Fallbacks.EmptyIterableWithMarkerOnNotFoundOr404;
import static org.jclouds.Fallbacks.EmptyPagedIterableOnNotFoundOr404;
import static org.jclouds.Fallbacks.NullOnNotFoundOr404;
import static org.jclouds.googlecompute.GoogleComputeConstants.COMPUTE_READONLY_SCOPE;

/**
 * Provides asynchronous access to Kernels via their REST API.
 *
 * @author David Alves
 */
@SkipEncoding({'/', '='})
@RequestFilters(OAuthAuthenticator.class)
@Consumes(MediaType.APPLICATION_JSON)
public interface KernelAsyncApi {

   /**
    * @see KernelApi#get(String)
    */
   @GET
   @Path("/kernels/{kernel}")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @Fallback(NullOnNotFoundOr404.class)
   ListenableFuture<Kernel> get(@PathParam("kernel") String kernelName);

   /**
    * @see KernelApi#listFirstPage()
    */
   @GET
   @Path("/kernels")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseKernels.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListenableFuture<ListPage<Kernel>> listFirstPage();

   /**
    * @see KernelApi#listAtMarker(String)
    */
   @GET
   @Path("/kernels")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseKernels.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListenableFuture<ListPage<Kernel>> listAtMarker(@QueryParam("pageToken") @Nullable String marker);

   /**
    * @see KernelApi#listAtMarker(String, org.jclouds.googlecompute.options.ListOptions)
    */
   @GET
   @Path("/kernels")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseKernels.class)
   @Fallback(EmptyIterableWithMarkerOnNotFoundOr404.class)
   ListenableFuture<ListPage<Kernel>> listAtMarker(@QueryParam("pageToken") @Nullable String marker,
                                                   ListOptions listOptions);

   /**
    * @see org.jclouds.googlecompute.features.KernelApi#list()
    */
   @GET
   @Path("/kernels")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseKernels.class)
   @Transform(ParseKernels.ToPagedIterable.class)
   @Fallback(EmptyPagedIterableOnNotFoundOr404.class)
   ListenableFuture<? extends PagedIterable<Kernel>> list();

   /**
    * @see KernelApi#list(org.jclouds.googlecompute.options.ListOptions)
    */
   @GET
   @Path("/kernels")
   @OAuthScopes(COMPUTE_READONLY_SCOPE)
   @ResponseParser(ParseKernels.class)
   @Transform(ParseKernels.ToPagedIterable.class)
   @Fallback(EmptyPagedIterableOnNotFoundOr404.class)
   ListenableFuture<? extends PagedIterable<Kernel>> list(ListOptions listOptions);

}
