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

import com.netflix.spectator.api.AbstractRegistry;
import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Gauge;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Timer;
import com.netflix.spectator.impl.Scheduler;
import com.netflix.spectator.ipc.http.HttpClient;
import com.netflix.spectator.ipc.http.HttpResponse;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.Deflater;

/**
 * Registry for reporting deltas to an aggregation service. This registry is intended for
 * use-cases where the instance cannot maintain state over the step interval. For example,
 * if running via a FaaS system like AWS Lambda, the lifetime of an invocation can be quite
 * small. Thus this registry would track the deltas and rely on a separate service to
 * consolidate the state over time if needed.
 *
 * The registry should be tied to the lifecyle of the container to ensure that the last set
 * of deltas are flushed properly. This will happen automatically when calling {@link #stop()}.
 */
public final class StatelessRegistry extends AbstractRegistry {

  private final long meterTTL;

  /** Create a new instance. */
  public StatelessRegistry(Clock clock, StatelessConfig config) {
    super(clock, config);
    this.meterTTL = config.meterTTL().toMillis();
  }

  @Override protected Counter newCounter(Id id) {
    return new StatelessCounter(id, clock(), meterTTL);
  }

  @Override protected DistributionSummary newDistributionSummary(Id id) {
    return new StatelessDistributionSummary(id, clock(), meterTTL);
  }

  @Override protected Timer newTimer(Id id) {
    return new StatelessTimer(id, clock(), meterTTL);
  }

  @Override protected Gauge newGauge(Id id) {
    return new StatelessGauge(id, clock(), meterTTL);
  }

  @Override protected Gauge newMaxGauge(Id id) {
    return new StatelessMaxGauge(id, clock(), meterTTL);
  }
}
