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

import com.netflix.spectator.api.Clock;
import com.tdunning.math.stats.TDigest;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Utility class for managing a set of AtomicLong instances mapped to a particular step interval.
 * The current implementation keeps an array of with two items where one is the current value
 * being updated and the other is the value from the previous interval and is only available for
 * polling.
 */
class StepDigest {
  private static final int PREVIOUS = 0;
  private static final int CURRENT = 1;

  private final double init;
  private final Clock clock;
  private final long step;

  private final AtomicReference<TDigest> previous;
  private final AtomicReference<TDigest> current;

  private final AtomicLong lastPollTime;

  private final AtomicLong lastInitPos;

  /** Create a new instance. */
  StepDigest(double init, Clock clock, long step) {
    this.init = init;
    this.clock = clock;
    this.step = step;
    lastInitPos = new AtomicLong(0L);
    lastPollTime = new AtomicLong(0L);
    previous = new AtomicReference<TDigest>(TDigest.createTreeDigest(init));
    current = new AtomicReference<TDigest>(TDigest.createTreeDigest(init));
  }

  private void roll(long now) {
    final long stepTime = now / step;
    final long lastInit = lastInitPos.get();
    if (lastInit < stepTime && lastInitPos.compareAndSet(lastInit, stepTime)) {
      previous.set(current.getAndSet(TDigest.createTreeDigest(init)));
    }
  }

  TDigest previous() {
    roll(clock.wallTime());
    return previous.get();
  }

  TDigest current() {
    roll(clock.wallTime());
    return current.get();
  }

  /** Get the value for the last completed interval. */
  TDigest poll() {
    final long now = clock.wallTime();

    roll(now);
    final TDigest value = previous.get();

    final long last = lastPollTime.getAndSet(now);
    final long missed = (now - last) / step - 1;

    if (last / step == now / step) {
      return value;
    } else if (last > 0L && missed > 0L) {
      return null;
    } else {
      return value;
    }
  }

  @Override public String toString() {
    return "StepDigest{init=" + init
        + ", step=" + step
        + ", lastPollTime=" + lastPollTime.get()
        + ", lastInitPos=" + lastInitPos.get() + '}';
  }
}
