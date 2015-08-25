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
package com.netflix.spectator.servo;

import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.stats.StatsConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
public class StatsTimerRecord {

  private final long[] values = new long[1000000];

  private final StatsConfig config = new StatsConfig.Builder()
      .withComputeFrequencyMillis(5000)
      .withPercentiles(new double[] {25.0, 50.0, 75.0, 90.0, 95.0, 99.0, 99.5})
      .withSampleSize(1000)
      .build();
  private final StatsTimer timer = new StatsTimer(MonitorConfig.builder("jmh").build(), config);

  private AtomicInteger pos = new AtomicInteger();

  @Setup(Level.Iteration)
  public void setup() {
    Random random = new Random();
    for (int i = 0; i < values.length; ++i) {
      values[i] = random.nextLong();
    }
  }

  @Threads(1)
  @Benchmark
  public void defaultRecord_T1(Blackhole bh) {
    int i = pos.getAndIncrement() % values.length;
    timer.record(values[i], TimeUnit.SECONDS);
    //bh.consume(timer.totalTime());
  }

  @Threads(2)
  @Benchmark
  public void defaultRecord_T2(Blackhole bh) {
    int i = pos.getAndIncrement() % values.length;
    timer.record(values[i], TimeUnit.SECONDS);
    //bh.consume(timer.totalTime());
  }

  @Threads(4)
  @Benchmark
  public void defaultRecord_T4(Blackhole bh) {
    int i = pos.getAndIncrement() % values.length;
    timer.record(values[i], TimeUnit.SECONDS);
    //bh.consume(timer.totalTime());
  }

  @Threads(8)
  @Benchmark
  public void defaultRecord_T8(Blackhole bh) {
    int i = pos.getAndIncrement() % values.length;
    timer.record(values[i], TimeUnit.SECONDS);
    //bh.consume(timer.totalTime());
  }

  @TearDown
  public void tearDown() {
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(".*")
        .forks(1)
        .build();
    new Runner(opt).run();
  }
}
