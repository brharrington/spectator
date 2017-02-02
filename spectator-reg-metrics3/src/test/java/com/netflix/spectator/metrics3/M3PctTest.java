package com.netflix.spectator.metrics3;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class M3PctTest {

  public static void main(String[] args) throws Exception {
    MetricRegistry m = new MetricRegistry();
    ScheduledReporter reporter = ConsoleReporter.forRegistry(m).outputTo(System.out).build();
    reporter.start(1, TimeUnit.SECONDS);

    Random r = new Random(42);
    Timer t = m.timer("test");

    for (int i = 0; ; ++i) {
      long v = -1;
      if (i < 25) {
        v = r.nextInt(10000);
        t.update(v, TimeUnit.MILLISECONDS);
      }
      Snapshot s = t.getSnapshot();
      //System.out.printf("%5d: %8d  %8.2f  %8.2f%n", i, v, s.getMedian(), s.get99thPercentile());
      Thread.sleep(1000);
    }
  }
}
