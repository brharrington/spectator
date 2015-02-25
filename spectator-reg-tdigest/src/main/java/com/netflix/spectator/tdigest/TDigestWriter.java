package com.netflix.spectator.tdigest;

import java.util.List;

/**
 * Created by brharrington on 2/24/15.
 */
public interface TDigestWriter {
  void write(List<TDigestMeasurement> measurements);
  void close();
}
