/*
 * Copyright 2014-2026 Netflix, Inc.
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
package com.netflix.spectator.api;

/**
 * A device for collecting a set of measurements. Note, this interface is only intended to be
 * implemented by registry implementations.
 */
public interface Meter {

  /**
   * Identifier used to lookup this meter in the registry.
   */
  Id id();

  /**
   * Get the set of measurements for this meter.
   */
  Iterable<Measurement> measure();

  /**
   * Indicates whether the meter is expired. For example, a counter might expire if there is no
   * activity within a given time frame.
   */
  boolean hasExpired();

  /**
   * Indicates the meter is no longer registered, so a reference to it is stale and updates
   * applied to it will not be reported. This is checked on every update through a reference the
   * user holds, so implementations should make it cheap: a registry that removes expired meters
   * can set a flag as part of the removal rather than deriving it from a clock reading.
   *
   * <p>Defaults to {@link #hasExpired()}, which is a safe over-approximation: a meter past its
   * TTL that is still registered resolves back to the same instance, so the only cost of the
   * default is a redundant lookup.</p>
   */
  default boolean isRemoved() {
    return hasExpired();
  }
}
