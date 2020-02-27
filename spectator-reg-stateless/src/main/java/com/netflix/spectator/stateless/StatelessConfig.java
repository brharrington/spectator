/*
 * Copyright 2014-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spectator.stateless;

import com.netflix.spectator.api.RegistryConfig;

import java.time.Duration;

/**
 * Configuration for stateless registry.
 */
public interface StatelessConfig extends RegistryConfig {

  /**
   * Returns the TTL for meters that do not have any activity. After this period the meter
   * will be considered expired and will not get reported. Default is 15 minutes.
   */
  default Duration meterTTL() {
    String v = get("stateless.meterTTL");
    return (v == null) ? Duration.ofMinutes(15) : Duration.parse(v);
  }
}
