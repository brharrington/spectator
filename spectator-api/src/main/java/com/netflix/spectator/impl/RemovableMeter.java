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

import com.netflix.spectator.api.Meter;

/**
 * Meter that can be told it is no longer registered, so that
 * {@link com.netflix.spectator.api.Meter#isRemoved()} can answer from a flag rather than from a
 * clock reading. The registry marks the meter as part of the removal, which is off the update
 * path, so only the read has to be cheap.
 *
 * <p>A meter that does not implement this keeps the default behavior, deriving staleness from
 * {@code hasExpired()}.</p>
 *
 * <p><b>This class is an internal implementation detail only intended for use within
 * spectator. It is subject to change without notice.</b></p>
 */
public interface RemovableMeter extends Meter {

  /**
   * Record that this meter has been removed from the registry. Idempotent: a meter is never
   * returned to the registry once removed, a new instance is created instead.
   */
  void markRemoved();
}
