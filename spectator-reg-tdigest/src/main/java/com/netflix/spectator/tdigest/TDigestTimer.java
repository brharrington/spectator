package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Timer;
import com.tdunning.math.stats.TDigest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by brharrington on 2/13/15.
 */
public class TDigestTimer implements Timer {

  private final Clock clock;
  private final Timer wrapped;
  private final StepDigest digest;

  TDigestTimer(Clock clock, Timer wrapped) {
    this.clock = clock;
    this.wrapped = wrapped;
    this.digest = new StepDigest(100.0, clock, 60000L);
  }

  @Override public void record(long amount, TimeUnit unit) {
    if (amount >= 0L) {
      final long nanos = unit.toNanos(amount);
      digest.current().add(nanos / 1e9);
      wrapped.record(amount, unit);
    }
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

  public double percentile(double p) {
    if (p < 0.0 || p > 100.0) {
      throw new IllegalArgumentException("p should be in [0.0, 100.0], got " + p);
    }
    final double q = p / 100.0;
    return digest.poll().quantile(q);
  }

  @Override public long count() {
    return wrapped.count();
  }

  @Override public long totalTime() {
    return wrapped.totalTime();
  }

  @Override public Id id() {
    return wrapped.id();
  }

  @Override public Iterable<Measurement> measure() {
    return wrapped.measure();
  }

  @Override public boolean hasExpired() {
    return wrapped.hasExpired();
  }
}
