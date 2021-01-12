/*
 * Copyright 2014-2021 Netflix, Inc.
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

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class StatelessRegistryTest {

  private ManualClock clock = new ManualClock();
  private StatelessRegistry registry = new StatelessRegistry(clock, newConfig());

  private StatelessConfig newConfig() {
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<>();
    props.put("stateless.frequency", "PT10S");
    props.put("stateless.batchSize", "3");
    return new StatelessConfig() {
      @Override
      public String get(String k) {
        return props.get(k);
      }

      @Override
      public RollupPolicy rollupPolicy() {
        return measurements -> {
          List<Measurement> ms = measurements
              .stream()
              .map(m -> {
                Id id = m.id().filterByKey(k -> !k.startsWith("_"));
                return new Measurement(id, m.timestamp(), m.value());
              })
              .collect(Collectors.toList());
          return Collections.singletonList(new RollupPolicy.Result(Collections.emptyMap(), ms));
        };
      }
    };
  }

  @Test
  public void measurementsEmpty() {
    Assertions.assertEquals(0, registry.getMeasurements().size());
  }

  @Test
  public void measurementsWithCounter() {
    registry.counter("test").increment();
    Assertions.assertEquals(1, registry.getMeasurements().size());
  }

  @Test
  public void measurementsWithTimer() {
    registry.timer("test").record(42, TimeUnit.NANOSECONDS);
    Assertions.assertEquals(4, registry.getMeasurements().size());
  }

  @Test
  public void measurementsWithDistributionSummary() {
    registry.distributionSummary("test").record(42);
    Assertions.assertEquals(4, registry.getMeasurements().size());
  }

  @Test
  public void measurementsWithGauge() {
    registry.gauge("test").set(4.0);
    Assertions.assertEquals(1, registry.getMeasurements().size());
  }

  @Test
  public void measurementsWithMaxGauge() {
    registry.maxGauge(registry.createId("test")).set(4.0);
    Assertions.assertEquals(1, registry.getMeasurements().size());
  }

  @Test
  public void batchesEmpty() {
    Assertions.assertEquals(0, registry.getBatches().size());
  }

  @Test
  public void batchesExact() {
    for (int i = 0; i < 9; ++i) {
      registry.counter("" + i).increment();
    }
    Assertions.assertEquals(3, registry.getBatches().size());
    for (RollupPolicy.Result batch : registry.getBatches()) {
      Assertions.assertEquals(3, batch.measurements().size());
    }
  }

  @Test
  public void batchesLastPartial() {
    for (int i = 0; i < 7; ++i) {
      registry.counter("" + i).increment();
    }
    List<RollupPolicy.Result> batches = registry.getBatches();
    Assertions.assertEquals(3, batches.size());
    for (int i = 0; i < batches.size(); ++i) {
      Assertions.assertEquals((i < 2) ? 3 : 1, batches.get(i).measurements().size());
    }
  }

  @Test
  public void batchesExpiration() {
    for (int i = 0; i < 9; ++i) {
      registry.counter("" + i).increment();
    }
    Assertions.assertEquals(3, registry.getBatches().size());
    for (RollupPolicy.Result batch : registry.getBatches()) {
      Assertions.assertEquals(3, batch.measurements().size());
    }

    clock.setWallTime(Duration.ofMinutes(15).toMillis() + 1);
    Assertions.assertEquals(0, registry.getBatches().size());
  }

  @Test
  public void rollupPolicy() {
    registry.counter("foo", "_bar", "123", "baz", "456").increment();
    List<RollupPolicy.Result> results = registry.getBatches();
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(1, results.get(0).measurements().size());
    Measurement m = results.get(0).measurements().get(0);
    Assertions.assertEquals(Id.create("foo").withTags("baz", "456", "statistic", "count"), m.id());
  }
}
