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

/**
 * Marker shared by the meters a registry handed out while its shape was unchanged. A registry
 * whose composition can change, such as {@code CompositeRegistry}, marks the current generation
 * stale and installs a fresh one, which tells every meter created against the old shape to
 * resolve again.
 *
 * <p>This is the same shape of signal as {@link com.netflix.spectator.api.Meter#isRemoved()}: one
 * boolean read on the update path, written once when the thing it describes goes away. A registry
 * with a fixed shape uses {@link #PERMANENT}, which is never marked.</p>
 *
 * <p><b>This class is an internal implementation detail only intended for use within
 * spectator. It is subject to change without notice.</b></p>
 */
public final class Generation {

  /** Generation for a registry whose shape never changes, so it is never stale. */
  public static final Generation PERMANENT = new Generation();

  private volatile boolean stale;

  /** Create a new instance. */
  public Generation() {
    this.stale = false;
  }

  /** Whether the meters created against this generation need to resolve again. */
  public boolean isStale() {
    return stale;
  }

  /**
   * Record that the shape this generation describes is gone. Only ever called on a generation the
   * registry has already replaced, so meters resolving in response cannot land on it again.
   */
  public void markStale() {
    stale = true;
  }
}
