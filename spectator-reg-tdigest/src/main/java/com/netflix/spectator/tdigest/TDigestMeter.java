package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.Meter;

/**
 * Created by brharrington on 2/24/15.
 */
public interface TDigestMeter extends Meter {
  TDigestMeasurement measureDigest();
}
