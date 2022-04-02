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
package com.netflix.spectator.sidecar;

import com.netflix.spectator.api.RegistryConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Configuration for sidecar registry.
 */
public interface SidecarConfig extends RegistryConfig {

  /**
   * Returns the URI for the aggregation service. The default is
   * {@code http://localhost:7101/api/v4/update}.
   */
  default SidecarWriter writer() {
    String v = get("sidecar.output-location");
    try {
      return SidecarWriter.create((v == null) ? "none" : v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Returns the common tags to apply to all metrics. The default is an empty map.
   */
  default Map<String, String> commonTags() {
    return Collections.emptyMap();
  }
}
