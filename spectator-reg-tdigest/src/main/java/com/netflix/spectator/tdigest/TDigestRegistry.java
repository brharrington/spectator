/**
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.*;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Registry that maps spectator types to servo. */
public class TDigestRegistry implements Registry {


  private static final Logger LOGGER = LoggerFactory.getLogger(TDigestRegistry.class);

  private final Registry fallback;

  /** Create a new instance. */
  public TDigestRegistry(Registry fallback) {
    this.fallback = fallback;
  }

  public TDigestRegistry() {
    this(new DefaultRegistry());
  }

  @Override public Clock clock() {
    return fallback.clock();
  }

  @Override public Id createId(String name) {
    return fallback.createId(name);
  }

  @Override public Id createId(String name, Iterable<Tag> tags) {
    return fallback.createId(name, tags);
  }

  @Override public void register(Meter meter) {
    fallback.register(meter);
  }

  @Override public Counter counter(Id id) {
    return fallback.counter(id);
  }

  @Override public TDigestDistributionSummary distributionSummary(Id id) {
    return new TDigestDistributionSummary(clock(), fallback.distributionSummary(id));
  }

  @Override public TDigestTimer timer(Id id) {
    return new TDigestTimer(clock(), fallback.timer(id));
  }

  @Override public Meter get(Id id) {
    return fallback.get(id);
  }

  @Override public Iterator<Meter> iterator() {
    return fallback.iterator();
  }
}
