package com.netflix.spectator.sidecar;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Counter;

import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put("sidecar.output-location", "none");
    try (SidecarRegistry registry = new SidecarRegistry(Clock.SYSTEM, props::get)) {
      Counter c = registry.counter("ipc.client.call",
          "ipc.server.app", "foo",
          "ipc.server.cluster", "foo-main",
          "ipc.server.asg", "foo-main-v001",
          "http.status", "200",
          "ipc.status", "success",
          "ipc.result", "success",
          "ipc.attempt", "initial",
          "ipc.attempt.final", "false",
          "ipc.endpoint", "/test",
          "id", "test",
          "owner", "sidecar");
      int n = 10_000_000;
      long start = System.nanoTime();
      for (int i = 0; i < n; ++i) {
        c.increment();
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1e9;
      System.out.printf("%f messages per second%n", n / duration);
    }
  }
}
