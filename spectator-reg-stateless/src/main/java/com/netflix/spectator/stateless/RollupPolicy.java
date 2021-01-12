/*
 * Copyright 2014-2021 Netflix, Inc.
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
package com.netflix.spectator.stateless;

import com.netflix.spectator.api.Measurement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Policy for performing a rollup on a set of measurements. This typically involves
 * removing some dimensions from the ids and combining the results into an aggregate
 * measurement.
 */
public interface RollupPolicy extends Function<List<Measurement>, List<RollupPolicy.Result>> {

  /** Does nothing, returns the input list without modification. */
  static RollupPolicy noop(Map<String, String> commonTags) {
    return ms -> Collections.singletonList(new Result(commonTags, ms));
  }

  /** Result of applying the rollup policy. */
  final class Result {
    private final Map<String, String> commonTags;
    private final List<Measurement> measurements;

    /** Create a new instance. */
    public Result(List<Measurement> measurements) {
      this(Collections.emptyMap(), measurements);
    }

    /**
     * Create a new instance.
     *
     * @param commonTags
     *     Common tags that should be applied to all measurements in this result.
     * @param measurements
     *     Measurments aggregated according to the policy.
     */
    public Result(Map<String, String> commonTags, List<Measurement> measurements) {
      this.commonTags = commonTags;
      this.measurements = measurements;
    }

    /** Return the common tags for this result. */
    public Map<String, String> commonTags() {
      return commonTags;
    }

    /** Return the measurements for this result. */
    public List<Measurement> measurements() {
      return measurements;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Result)) return false;
      Result result = (Result) o;
      return commonTags.equals(result.commonTags)
          && measurements.equals(result.measurements);
    }

    @Override
    public int hashCode() {
      return Objects.hash(commonTags, measurements);
    }
  }
}
