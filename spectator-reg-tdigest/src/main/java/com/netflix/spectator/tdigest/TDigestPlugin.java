package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Singleton
public class TDigestPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(TDigestPlugin.class);

  private final TDigestRegistry registry;
  private final TDigestWriter writer;

  private ScheduledExecutorService executor;

  @Inject
  public TDigestPlugin(TDigestRegistry registry, TDigestWriter writer) {
    this.registry = registry;
    this.writer = writer;
  }

  @PostConstruct
  public void init() {
    executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override public Thread newThread(Runnable r) {
          return new Thread(r, "TDigestPlugin");
        }
      });

    Runnable task = new Runnable() {
      @Override public void run() {
        try {
          writeData();
        } catch (Exception e) {
          LOGGER.error("failed to publish percentile data", e);
        }
      }
    };

    executor.scheduleWithFixedDelay(task, 0L, 40L, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdown();
    try {
      writer.close();
    } catch (IOException e) {
      LOGGER.error("failed to close writer", e);
    }
  }

  void writeData() {
    LOGGER.debug("starting collection of digests");
    List<TDigestMeasurement> ms = new ArrayList<>();
    for (Meter m : registry) {
      if (m instanceof TDigestMeter) {
        TDigestMeasurement measurement = ((TDigestMeter) m).measureDigest();
        ms.add(measurement);
      }
    }
    if (!ms.isEmpty()) {
      LOGGER.debug("writing {} measurements", ms.size());
      try {
        writer.write(ms);
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
      }
    } else {
      LOGGER.debug("no digest measurements found, nothing to do");
    }
  }
}
