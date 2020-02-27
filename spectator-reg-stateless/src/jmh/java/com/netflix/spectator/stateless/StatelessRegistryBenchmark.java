package com.netflix.spectator.stateless;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Id;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class StatelessRegistryBenchmark {

  private final Id id = Id.create("test");
  private final Id idWithTags = Id.create("ipc.server.call")
      .withTag("ipc.client.app", "application")
      .withTag("ipc.client.cluster", "application-main")
      .withTag("ipc.client.asg", "application-main-v000")
      .withTag("ipc.status", "success")
      .withTag("ipc.result", "success")
      .withTag("http.status", "200")
      .withTag("owner", "niws");

  private final StatelessRegistry registry =
      new StatelessRegistry(Clock.SYSTEM, System::getProperty);

  @Benchmark
  public void counterIncrementNameOnly() {
    registry.counter(id).increment();
  }

  @Benchmark
  public void counterIncrementWithTags() {
    registry.counter(idWithTags).increment();
  }
}
