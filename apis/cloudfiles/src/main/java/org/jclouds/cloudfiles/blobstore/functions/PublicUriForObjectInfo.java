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
package org.jclouds.cloudfiles.blobstore.functions;

import static org.jclouds.http.Uris.uriBuilder;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.openstack.swift.domain.ObjectInfo;

import com.google.common.base.Function;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author Adrian Cole
 */
@Singleton
public class PublicUriForObjectInfo implements Function<ObjectInfo, URI> {
   
   private final LoadingCache<String, URI> cdnContainer;

   @Inject
   public PublicUriForObjectInfo(LoadingCache<String, URI> cdnContainer) {
      this.cdnContainer = cdnContainer;
   }

   public URI apply(ObjectInfo from) {
      if (from == null)
         return null;
      try {
         return uriBuilder(cdnContainer.getUnchecked(from.getContainer()))
               .clearQuery().appendPath(from.getName()).build();
      } catch (NullPointerException e) {
         // nulls not permitted from cache loader
         return null;
      } catch (CacheLoader.InvalidCacheLoadException e) {
         // nulls not permitted from cache loader
         return null;
      }
   }
}
