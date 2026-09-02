/*
 * Copyright 2014-2026 Netflix, Inc.
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
package com.netflix.spectator.impl;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;

import java.util.function.LongSupplier;

/**
 * Base type for meters that allow the underlying implementation to be replaced with
 * another. This is used by {@link com.netflix.spectator.api.AbstractRegistry} as the
 * basis for expiring types where a user may have a reference in their code.
 *
 * <p><b>This class is an internal implementation detail only intended for use within
 * spectator. It is subject to change without notice.</b></p>
 */
public abstract class SwapMeter<T extends Meter> implements Meter {

  /** Registry used to lookup values after expiration. */
  protected final Registry registry;

  // Changes when the shape of the registry changes, for example a registry being added to a
  // composite. Feeds both hasExpired() and get().
  private final LongSupplier versionSupplier;
  private volatile long currentVersion;

  /** Id to use when performing a lookup after expiration. */
  protected final Id id;

  /** Current meter to delegate operations. */
  private volatile T underlying;

  /** Create a new instance. */
  public SwapMeter(Registry registry, LongSupplier versionSupplier, Id id, T underlying) {
    this.registry = registry;
    this.versionSupplier = versionSupplier;
    this.currentVersion = versionSupplier.getAsLong();
    this.id = id;
    this.underlying = unwrap(underlying);
  }

  /**
   * Lookup the meter from the registry.
   */
  public abstract T lookup();

  @Override public Id id() {
    return id;
  }

  @Override public Iterable<Measurement> measure() {
    return get().measure();
  }

  /** {@inheritDoc} Routine meter removal is deliberately not part of this signal. */
  @Override public boolean hasExpired() {
    return currentVersion < versionSupplier.getAsLong() || underlying.hasExpired();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to the underlying meter rather than falling back to {@link #hasExpired()}, so
   * that a wrapper nested inside another one, as {@code CompositeRegistry} creates, passes the
   * cheap removal flag through instead of forcing the parent back onto a wall clock read. The
   * version of this wrapper is deliberately not part of the answer: it is checked by this
   * wrapper's own {@link #get()}, which the parent will reach on the next update.</p>
   */
  @Override public boolean isRemoved() {
    final T meter = underlying;
    return meter == null || meter.isRemoved();
  }

  /**
   * Set the underlying instance of the meter to use. This can be set to {@code null}
   * to indicate that the meter has expired and is no longer in the registry.
   */
  public void set(T meter) {
    underlying = unwrap(meter);
  }

  /**
   * Return the underlying meter, resolving a new one if the registry has removed it.
   *
   * <p>This runs on every meter update, so it asks the meter whether it is still registered
   * rather than whether it has expired: for {@code AtlasMeter} the former is a flag set by the
   * removal and the latter costs a wall clock read. Only a wrapper whose own meter was removed
   * resolves again; one holding a live meter never does.</p>
   */
  public T get() {
    // Sampled once, before the lookup: re-reading it for the assignment could store a version
    // newer than the one lookup() observed, which would swallow the change it describes.
    final long version = versionSupplier.getAsLong();
    T meter = underlying;
    if (meter == null || meter.isRemoved() || currentVersion < version) {
      meter = unwrap(lookup());
      underlying = meter;
      // Published after the meter it describes, so a concurrent caller that sees the new version
      // also sees the new meter. Publishing it first lets that caller skip the resolve and keep
      // updating the instance this lookup just replaced, silently dropping the update.
      currentVersion = version;
    }
    return meter;
  }

  /**
   * If the values are nested, then unwrap any that have the same registry instance.
   */
  @SuppressWarnings("unchecked")
  private T unwrap(T meter) {
    T tmp = meter;
    while (tmp instanceof SwapMeter<?> && registry == ((SwapMeter<?>) tmp).registry) {
      tmp = ((SwapMeter<T>) tmp).underlying;
    }
    return tmp;
  }
}
