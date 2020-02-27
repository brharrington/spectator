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

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Timer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Timer that keeps track of the deltas since the last time it was measured.
 */
class StatelessTimer extends StatelessMeter implements Timer {

  /** Create a new instance. */
  StatelessTimer(Id id, Clock clock, long ttl) {
    super(id, clock, ttl);
  }

  @Override public void record(long amount, TimeUnit unit) {
    final double seconds = unit.toNanos(amount) / 1e9;
    send(seconds);
  }

  @Override public <T> T record(Callable<T> f) throws Exception {
    final long start = clock.monotonicTime();
    try {
      return f.call();
    } finally {
      record(clock.monotonicTime() - start, TimeUnit.NANOSECONDS);
    }
  }

  @Override public void record(Runnable f) {
    final long start = clock.monotonicTime();
    try {
      f.run();
    } finally {
      record(clock.monotonicTime() - start, TimeUnit.NANOSECONDS);
    }
  }

  @Override public long count() {
    return 0L;
  }

  @Override public long totalTime() {
    return 0L;
  }

  @Override public String typeInfo() {
    return "t";
  }
}
