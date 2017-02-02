package com.netflix.spectator.metrics2;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PctTest {

  public static void main(String[] args) throws Exception {
    com.yammer.metrics.core.MetricsRegistry m = Metrics.defaultRegistry();

    Random r = new Random(42);
    Timer t = m.newTimer(PctTest.class, "test");

    for (int i = 0; ; ++i) {
      long v = -1;
      if (i < 25) {
        v = r.nextInt(10000);
        t.update(v, TimeUnit.MILLISECONDS);
      }
      Snapshot s = t.getSnapshot();
      System.out.printf("%5d: %8d  %8.2f  %8.2f%n", i, v, s.getMedian(), s.get99thPercentile());
      Thread.sleep(1000);
    }
  }
}
