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
package com.netflix.spectator.sandbox;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class OutlierCounterTest {

  private static final long STEP = 60000L;

  private static final Random RANDOM = new Random(42);

  // Multiple bands of data
  // Red/black deployment
  // Noise

  @Test
  public void singleLongRunning() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        String node = String.format("i-%05d", i % 10);
        counter.increment(node);
        counter.increment(String.format("i-%05d", 7));
      }
    }

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void singleLongRunningSmallN() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        String node = String.format("i-%05d", i % 3);
        counter.increment(node);
        if (i % 10 == 0) {
          counter.increment(String.format("i-%05d", 1));
        }
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void shortSpike() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        String node = String.format("i-%05d", i % 10);
        counter.increment(node);
        if (t >= 27 && t <= 30) {
          counter.increment(String.format("i-%05d", 7));
        }
      }
      if (t >= 25 && t <= 48) {
        System.out.println(counter.summary());
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void redBlack() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    int offset = 0;
    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        String node = String.format("i-%05d", i % 10 + offset);
        counter.increment(node);
      }

      if (t == 60) {
        offset = 10;
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void replacement() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    int offset = 0;
    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        String node = String.format("i-%05d", i % 10 + offset);
        counter.increment(node);
      }

      if (t == 60) {
        offset = 2;
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void multipleBands() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 2000; ++i) {
        int n = i % 99;
        String node = String.format("i-%05d", n);
        counter.increment(node);
        counter.increment(node);
        counter.increment(node);
        counter.increment(node);
        if (n <= 32) {
          counter.increment(node);
        }
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

  @Test
  public void noisy() {
    ManualClock clock = new ManualClock();
    Registry registry = new DefaultRegistry(clock);
    OutlierCounter counter = OutlierCounter.get(registry, registry.createId("test"), "dst.node");

    SecureRandom r = new SecureRandom();
    for (int t = 0; t < 180; ++t) {
      clock.setWallTime(t * STEP);

      for (int i = 0; i < 1000; ++i) {
        int j = r.nextInt(10);
        String node = String.format("i-%05d", j);
        counter.increment(node);
      }
    }
    System.out.println(counter.summary());

    registry.counters().forEach(c -> System.out.printf("%s => %d%n", c.id(), c.count()));
  }

}
