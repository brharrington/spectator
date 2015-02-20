package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Timer;
import com.tdunning.math.stats.TDigest;

/**
 * Created by brharrington on 2/20/15.
 */
public class TDigestDistributionSummary implements DistributionSummary {

  private final Clock clock;
  private final DistributionSummary wrapped;
  private final TDigest digest;

  TDigestDistributionSummary(Clock clock, DistributionSummary wrapped) {
    this.clock = clock;
    this.wrapped = wrapped;
    this.digest = TDigest.createTreeDigest(100.0);
  }

  @Override public void record(long amount) {
    if (amount >= 0L) {
      digest.add(amount);
      wrapped.record(amount);
    }
  }

  @Override public long count() {
    return wrapped.count();
  }

  @Override public long totalAmount() {
    return wrapped.totalAmount();
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
