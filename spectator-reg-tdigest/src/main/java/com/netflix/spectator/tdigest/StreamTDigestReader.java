package com.netflix.spectator.tdigest;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by brharrington on 2/28/15.
 */
public class StreamTDigestReader implements TDigestReader {

  private final DataInputStream in;

  private final byte[] buf = new byte[TDigestWriter.BUFFER_SIZE];

  public StreamTDigestReader(InputStream in) {
    this.in = new DataInputStream(in);
  }

  public StreamTDigestReader(DataInputStream in) {
    this.in = in;
  }

  @Override
  public List<TDigestMeasurement> read() throws IOException {
    if (in.available() == 0) {
      return Collections.emptyList();
    } else {
      int size = in.readInt();
      if (size > buf.length) {
        throw new IOException("buffer exceeds max size (" + size + " > " + buf.length + ")");
      }
      int length = in.read(buf, 0, size);
      if (length != size) {
        throw new IOException("unexpected end of stream");
      }
      return Json.decode(buf, 0, length);
    }
  }

  @Override public void close() throws IOException {
    in.close();
  }
}
