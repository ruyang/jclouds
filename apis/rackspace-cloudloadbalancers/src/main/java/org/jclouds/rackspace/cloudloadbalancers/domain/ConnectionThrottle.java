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
package org.jclouds.rackspace.cloudloadbalancers.domain;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * The connection throttling feature imposes limits on the number of connections per IP address to help mitigate 
 * malicious or abusive traffic to your applications. The attributes in the table that follows can be configured 
 * based on the traffic patterns for your sites.
 * 
 * @author Everett Toews
 */
public class ConnectionThrottle {

   private final int maxConnections;
   private final int minConnections;
   private final int maxConnectionRate;
   private final int rateInterval;

   protected ConnectionThrottle(int maxConnections, int minConnections, int maxConnectionRate, int rateInterval) {
      this.maxConnections = maxConnections;
      this.minConnections = minConnections;
      this.maxConnectionRate = maxConnectionRate;
      this.rateInterval = rateInterval;
   }

   public int getMaxConnections() {
      return this.maxConnections;
   }

   public int getMinConnections() {
      return this.minConnections;
   }

   public int getMaxConnectionRate() {
      return this.maxConnectionRate;
   }

   public int getRateInterval() {
      return this.rateInterval;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(maxConnections, minConnections, maxConnectionRate, rateInterval);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null || getClass() != obj.getClass())
         return false;
      ConnectionThrottle that = ConnectionThrottle.class.cast(obj);

      return Objects.equal(this.maxConnections, that.maxConnections)
            && Objects.equal(this.minConnections, that.minConnections)
            && Objects.equal(this.maxConnectionRate, that.maxConnectionRate)
            && Objects.equal(this.rateInterval, that.rateInterval);
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper(this).add("maxConnections", maxConnections).add("minConnections", minConnections)
            .add("maxConnectionRate", maxConnectionRate).add("rateInterval", rateInterval);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   public static class Builder {
      private int maxConnections;
      private int minConnections;
      private int maxConnectionRate;
      private int rateInterval;

      /** 
       * Maximum number of connections to allow for a single IP address. Setting a value of 0 will allow unlimited 
       * simultaneous connections; otherwise set a value between 1 and 100000.
       */
      public Builder maxConnections(int maxConnections) {
         this.maxConnections = maxConnections;
         return this;
      }

      /** 
       * Allow at least this number of connections per IP address before applying throttling restrictions. Setting 
       * a value of 0 allows unlimited simultaneous connections; otherwise, set a value between 1 and 1000.
       */
      public Builder minConnections(int minConnections) {
         this.minConnections = minConnections;
         return this;
      }

      /** 
       * Maximum number of connections allowed from a single IP address in the defined rateInterval. Setting a value 
       * of 0 allows an unlimited connection rate; otherwise, set a value between 1 and 100000.
       */
      public Builder maxConnectionRate(int maxConnectionRate) {
         this.maxConnectionRate = maxConnectionRate;
         return this;
      }

      /** 
       * Frequency (in seconds) at which the maxConnectionRate is assessed. For example, a maxConnectionRate of 30 
       * with a rateInterval of 60 would allow a maximum of 30 connections per minute for a single IP address. This 
       * value must be between 1 and 3600.
       */
      public Builder rateInterval(int rateInterval) {
         this.rateInterval = rateInterval;
         return this;
      }

      public ConnectionThrottle build() {
         return new ConnectionThrottle(maxConnections, minConnections, maxConnectionRate, rateInterval);
      }

      public Builder from(ConnectionThrottle in) {
         return this.maxConnections(in.getMaxConnections()).minConnections(in.getMinConnections())
               .maxConnectionRate(in.getMaxConnectionRate()).rateInterval(in.getRateInterval());
      }
   }

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().from(this);
   }
}
