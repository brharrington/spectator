package com.netflix.spectator.stateless;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Registry;
import org.junit.jupiter.api.Test;

public class StatelessRegistryTest {

  @Test
  public void counter() {
    Registry registry = new StatelessRegistry(Clock.SYSTEM, System::getProperty);
    registry.counter("test").increment();
    registry.counter("test").increment();
    registry.counter("test").increment(42);
  }

  @Test
  public void counterWithTags() {
    Registry registry = new StatelessRegistry(Clock.SYSTEM, System::getProperty);
    registry.counter("test", "a", "1", "b", "2").increment();
  }
}
