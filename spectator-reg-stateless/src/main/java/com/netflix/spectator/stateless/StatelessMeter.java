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

import com.netflix.spectator.api.*;

import java.util.Collections;

/** Base class for core meter types used by {@link StatelessRegistry}. */
abstract class StatelessMeter implements Meter {

  /** Base identifier for all measurements supplied by this meter. */
  protected final Id id;
  private final StringBuilder buffer;
  private final int valuePosition;

  /** Time source for checking if the meter has expired. */
  protected final Clock clock;

  /** TTL value for an inactive meter. */
  private final long ttl;

  /** Last time this meter was updated. */
  private volatile long lastUpdated;

  /** Create a new instance. */
  StatelessMeter(Id id, Clock clock, long ttl) {
    this.id = id;

    // Encode id and reuse for each send
    this.buffer = new StringBuilder()
        .append("1:")
        .append(typeInfo())
        .append(':')
        .append(id.name());
    for (Tag t : id.tags()) {
      this.buffer.append(',').append(t.key()).append('=').append(t.value());
    }
    this.buffer.append(':');
    this.valuePosition = this.buffer.length();

    this.clock = clock;
    this.ttl = ttl;
    lastUpdated = clock.wallTime();
  }

  /**
   * Updates the last updated timestamp for the meter to indicate it is active and should
   * not be considered expired.
   */
  private void updateLastModTime() {
    lastUpdated = clock.wallTime();
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return clock.wallTime() - lastUpdated > ttl;
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  protected abstract String typeInfo();

  protected void send(double value) {
    updateLastModTime();

    // Clear any previous value if present
    buffer.delete(valuePosition, buffer.length());

    // Encode new value and send
    buffer.append(value);
    UdpUtils.send(buffer);
  }
}
