package com.netflix.spectator.sandbox;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by brharrington on 4/19/16.
 */
public class OutlierCounter {

  // TODO: needs to be shared
  public static OutlierCounter get(Registry registry, Id baseId, String key) {
    return new OutlierCounter(registry, baseId, key);
  }

  // Number of events to accumulate before trying to recompute the outliers
  private static final int NUM_EVENTS = 1000;

  private final Registry registry;
  private final Id baseId;
  private final String key;

  private final Counter others;
  private final AtomicReference<Map<String, Counter>> outliers;

  private final AtomicLong count;
  private final ConcurrentHashMap<String, RollingCounter> countsByValue;

  private OutlierCounter(Registry registry, Id baseId, String key) {
    this.registry = registry;
    this.baseId = baseId;
    this.key = key;

    others = registry.counter(baseId.withTag(key, "others"));
    outliers = new AtomicReference<>(Collections.emptyMap());

    count = new AtomicLong(0L);
    countsByValue = new ConcurrentHashMap<>();
  }

  public void increment(String value) {
    countsByValue.computeIfAbsent(value, v -> newRollingCounter()).increment();
    if (count.incrementAndGet() % NUM_EVENTS == 0) {
      computeOutliers();
    }

    Counter c = outliers.get().get(value);
    ((c != null) ? c : others).increment();
  }

  private List<Pair> snapshot() {
    List<Pair> counts = new ArrayList<>(countsByValue.size());
    for (Map.Entry<String, RollingCounter> entry : countsByValue.entrySet()) {
      double avg = entry.getValue().avg(10);
      if (!Double.isNaN(avg)) {
        if (avg <= 0.0) {
          countsByValue.remove(entry.getKey());
        } else {
          Pair pair = new Pair(entry.getKey(), avg);
          counts.add(pair);
        }
      }
    }
    return counts;
  }

  private Stats computeStats(List<Pair> counts) {
    double max = 0.0;
    double min = Double.MAX_VALUE;
    int n = 0;
    double mean = 0.0;
    double m2 = 0.0;

    for (Pair pair : counts) {
      max = Math.max(max, pair.avg);
      min = Math.min(min, pair.avg);
      ++n;
      double delta = pair.avg - mean;
      mean += delta / n;
      m2 += delta * (pair.avg - mean);
    }

    double stddev = Math.sqrt((n < 2) ? Double.NaN : m2 / (n - 1));

    return new Stats(stddev, mean, min, max, n);
  }

  private void computeOutliers() {
    List<Pair> counts = snapshot();
    Stats stats = computeStats(counts);
    Map<String, Counter> counters = counts.stream()
        .filter(p -> Math.abs(stats.mean - p.avg) > stats.threshold)
        .collect(Collectors.toMap(p -> p.value, p -> registry.counter(baseId.withTag(key, p.value))));
    outliers.set(counters);
  }

  public String summary() {
    List<Pair> counts = snapshot();
    Collections.sort(counts, (a, b) -> a.value.compareTo(b.value));

    StringBuilder buf = new StringBuilder();
    buf.append("counts:\n");
    for (Pair pair : counts) {
      String s = String.format("%,24.2f : %s%n", pair.avg, pair.value);
      buf.append(outliers.get().containsKey(pair.value) ? '>' : ' ').append(s);
    }
    buf.append(computeStats(counts));
    return buf.toString();
  }

  private RollingCounter newRollingCounter() {
    return new RollingCounter(registry.clock(), 60000L, 15);
  }

  private static class Pair {
    final String value;
    final double avg;

    Pair(String value, double avg) {
      this.value = value;
      this.avg = avg;
    }
  }

  private static class Stats {
    final double stddev;
    final double mean;
    final double min;
    final double max;
    final int count;

    final double maxDelta;
    final double multiple;
    final double threshold;

    Stats(double stddev, double mean, double min, double max, int count) {
      this.stddev = stddev;
      this.mean = mean;
      this.min = min;
      this.max = max;
      this.count = count;

      maxDelta = Math.max(Math.abs(max - mean), Math.abs(mean - min));
      multiple = 3.0; //Math.max(Math.floor(maxDelta / stddev), 1.0);
      threshold = multiple * stddev;
    }

    @Override public String toString() {
      return "stats:\n"
          + String.format("%,25d : count%n",       count)
          + String.format("%,25.2f : mean%n",      mean)
          + String.format("%,25.2f : min%n",       min)
          + String.format("%,25.2f : max%n",       max)
          + String.format("%,25.2f : stddev%n",    stddev)
          + String.format("%,25.2f : max delta%n", maxDelta)
          + String.format("%,25.2f : multiple%n",  multiple)
          + String.format("%,25.2f : threshold%n", threshold);
    }
  }
}
