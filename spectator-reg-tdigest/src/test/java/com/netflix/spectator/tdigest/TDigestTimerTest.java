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

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Statistic;
import com.netflix.spectator.api.Timer;
import com.netflix.spectator.api.Utils;
import com.tdunning.math.stats.TDigest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class TDigestTimerTest {

  private final ManualClock clock = new ManualClock();

  private TDigestTimer newTimer(String name) {
    final TDigestRegistry r = new TDigestRegistry(clock);
    return (TDigestTimer) r.timer(r.createId(name));
  }

  @Before
  public void before() {
    clock.setWallTime(0L);
    clock.setMonotonicTime(0L);
  }

  @Test
  public void testInit() {
    TDigestTimer t = newTimer("foo");
    Assert.assertEquals(t.count(), 0L);
    Assert.assertEquals(t.totalTime(), 0L);
    Assert.assertEquals(t.percentile(99.9), Double.NaN, 1e-12);
  }

  @Test
  public void testRecord() {
    TDigestTimer t = newTimer("foo");
    for (int i = 0; i < 99; ++i) {
      t.record(42, TimeUnit.MILLISECONDS);
    }
    t.record(1000, TimeUnit.MILLISECONDS);
    clock.setWallTime(61000);
    Assert.assertEquals(t.count(), 100L);
    for (int i = 0; i <= 99; ++i) {
      final double p = i / 10.0;
      Assert.assertEquals(t.percentile(p), 42 / 1000.0, 1e-12);
    }
    Assert.assertEquals(t.percentile(100.0), 1.0, 1e-12);
  }

  @Test
  public void testRecordNegative() {
    Timer t = newTimer("foo");
    t.record(-42, TimeUnit.MILLISECONDS);
    Assert.assertEquals(t.count(), 0L);
    Assert.assertEquals(t.totalTime(), 0L);
  }

  @Test
  public void testRecordZero() {
    Timer t = newTimer("foo");
    t.record(0, TimeUnit.MILLISECONDS);
    Assert.assertEquals(t.count(), 1L);
    Assert.assertEquals(t.totalTime(), 0L);
  }

  @Test
  public void testRecordCallable() throws Exception {
    Timer t = newTimer("foo");
    clock.setMonotonicTime(100L);
    int v = t.record(new Callable<Integer>() {
      public Integer call() throws Exception {
        clock.setMonotonicTime(500L);
        return 42;
      }
    });
    Assert.assertEquals(v, 42);
    Assert.assertEquals(t.count(), 1L);
    Assert.assertEquals(t.totalTime(), 400L);
  }

  @Test
  public void testRecordCallableException() throws Exception {
    Timer t = newTimer("foo");
    clock.setMonotonicTime(100L);
    boolean seen = false;
    try {
      t.record(new Callable<Integer>() {
        public Integer call() throws Exception {
          clock.setMonotonicTime(500L);
          throw new RuntimeException("foo");
        }
      });
    } catch (Exception e) {
      seen = true;
    }
    Assert.assertTrue(seen);
    Assert.assertEquals(t.count(), 1L);
    Assert.assertEquals(t.totalTime(), 400L);
  }

  @Test
  public void testRecordRunnable() throws Exception {
    Timer t = newTimer("foo");
    clock.setMonotonicTime(100L);
    t.record(new Runnable() {
      public void run() {
        clock.setMonotonicTime(500L);
      }
    });
    Assert.assertEquals(t.count(), 1L);
    Assert.assertEquals(t.totalTime(), 400L);
  }

  @Test
  public void testRecordRunnableException() throws Exception {
    Timer t = newTimer("foo");
    clock.setMonotonicTime(100L);
    boolean seen = false;
    try {
      t.record(new Runnable() {
        public void run() {
          clock.setMonotonicTime(500L);
          throw new RuntimeException("foo");
        }
      });
    } catch (Exception e) {
      seen = true;
    }
    Assert.assertTrue(seen);
    Assert.assertEquals(t.count(), 1L);
    Assert.assertEquals(t.totalTime(), 400L);
  }
}
