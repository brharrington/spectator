package com.netflix.spectator.sandbox;

import com.netflix.spectator.api.Clock;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Created by brharrington on 4/19/16.
 */
class RollingCounter {

  private final Clock clock;
  private final long step;
  private final int windowSize;
  private final AtomicLongArray times;
  private final AtomicLongArray counts;

  RollingCounter(Clock clock, long step, int windowSize) {
    this.clock = clock;
    this.step = step;
    this.windowSize = windowSize;
    this.counts = new AtomicLongArray(windowSize);
    this.times = new AtomicLongArray(windowSize);
  }

  void increment() {
    long now = clock.wallTime();
    long t = now / step;
    int i = (int) (t % windowSize);
    long p = times.get(i);
    if (p != t && times.compareAndSet(i, p, t)) {
      counts.set(i, 0L);
    }
    counts.incrementAndGet(i);
  }

  long count() {
    long cutoff = clock.wallTime() / step - windowSize;
    long total = 0L;
    for (int i = 0; i < windowSize; ++i) {
      if (times.get(i) > cutoff) {
        total += counts.get(i);
      }
    }
    return total;
  }

  double avg(int minN) {
    long cutoff = clock.wallTime() / step - windowSize;
    long total = 0L;
    int n = 0;
    for (int i = 0; i < windowSize; ++i) {
      if (times.get(i) > cutoff) {
        long c = counts.get(i);
        total += c;
        if (c > 0L) ++n;
      }
    }
    return (n >= minN) ? (double) total / n : Double.NaN;

  }
}
