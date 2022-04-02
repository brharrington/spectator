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

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Gauge;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Tag;
import com.netflix.spectator.api.TagList;
import com.netflix.spectator.api.Timer;
import com.netflix.spectator.api.patterns.PolledMeter;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO:
 * - comments
 * - tests
 * - buffering
 * - performance
 * - unix domain sockets
 *
 * Registry for reporting deltas to an aggregation service. This registry is intended for
 * use-cases where the instance cannot maintain state over the step interval. For example,
 * if running via a FaaS system like AWS Lambda, the lifetime of an invocation can be quite
 * small. Thus this registry would track the deltas and rely on a separate service to
 * consolidate the state over time if needed.
 *
 * The registry should be tied to the lifecyle of the container to ensure that the last set
 * of deltas are flushed properly. This will happen automatically when calling {@link #stop()}.
 */
public final class SidecarRegistry implements Registry, AutoCloseable {

  private final Clock clock;
  private final TagList commonTags;
  private final SidecarWriter writer;

  private final ConcurrentHashMap<Id, Object> state;

  /** Create a new instance. */
  public SidecarRegistry(Clock clock, SidecarConfig config) {
    this.clock = clock;
    this.commonTags = TagList.create(config.commonTags());
    this.writer = config.writer();
    this.state = new ConcurrentHashMap<>();
  }

  /**
   * Stop the scheduler reporting data.
   */
  @Override public void close() throws Exception {
    writer.close();
    state.clear();
  }

  @Override
  public Clock clock() {
    return clock;
  }

  @Override
  public Id createId(String name) {
    return Id.create(name);
  }

  @Override
  public Id createId(String name, Iterable<Tag> tags) {
    return Id.create(name).withTags(tags);
  }

  @Deprecated
  @Override
  public void register(Meter meter) {
    PolledMeter.monitorMeter(this, meter);
  }

  @Override
  public ConcurrentMap<Id, Object> state() {
    return state;
  }

  private Id mergeCommonTags(Id id) {
    return commonTags.size() == 0 ? id : id.withTags(commonTags);
  }

  @Override
  public Counter counter(Id id) {
    return new SidecarCounter(mergeCommonTags(id), writer);
  }

  @Override
  public DistributionSummary distributionSummary(Id id) {
    return new SidecarDistributionSummary(mergeCommonTags(id), writer);
  }

  @Override
  public Timer timer(Id id) {
    return new SidecarTimer(mergeCommonTags(id), clock, writer);
  }

  @Override
  public Gauge gauge(Id id) {
    return new SidecarGauge(mergeCommonTags(id), writer);
  }

  @Override
  public Gauge maxGauge(Id id) {
    return new SidecarMaxGauge(mergeCommonTags(id), writer);
  }

  @Override
  public Meter get(Id id) {
    return null;
  }

  @Override
  public Iterator<Meter> iterator() {
    return Collections.emptyIterator();
  }
}
