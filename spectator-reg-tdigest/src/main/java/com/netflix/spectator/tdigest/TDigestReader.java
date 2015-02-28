package com.netflix.spectator.tdigest;

import java.io.IOException;
import java.util.List;

/**
 * Created by brharrington on 2/28/15.
 */
public interface TDigestReader extends AutoCloseable {
  List<TDigestMeasurement> read() throws IOException;
  void close() throws IOException;
}
