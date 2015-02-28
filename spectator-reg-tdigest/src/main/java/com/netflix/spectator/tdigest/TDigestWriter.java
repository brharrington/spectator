package com.netflix.spectator.tdigest;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by brharrington on 2/24/15.
 */
abstract class TDigestWriter implements AutoCloseable {

  // Kinesis has a 50k limit for the record size.
  static final int BUFFER_SIZE = 50000;

  // Minimum amount of free space for the buffer to try and write another measurement. If less
  // space is available, go ahead and flush the buffer.
  static final int MIN_FREE = 4096;

  abstract void write(ByteBuffer buf) throws IOException;

  void write(List<TDigestMeasurement> measurements) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
    ByteBufferOutputStream out = new ByteBufferOutputStream(buf, 2);
    JsonGenerator gen = Json.newGenerator(out);
    gen.writeStartArray();
    gen.flush();
    int pos = buf.position();
    for (TDigestMeasurement m : measurements) {
      Json.encode(m, gen);
      gen.flush();

      if (out.overflow()) {
        // Ignore the last entry written to the buffer
        buf.position(pos);
        gen.writeEndArray();
        gen.close();
        write(buf);

        // Reuse the buffer and write the current entry
        out.reset();
        gen = Json.newGenerator(out);
        gen.writeStartArray();
        Json.encode(m, gen);
        gen.flush();

        // If a single entry is too big, then drop it
        if (out.overflow()) {
          // TODO: log and drop
          out.reset();
          gen = Json.newGenerator(out);
          gen.writeStartArray();
          gen.flush();
        }

        pos = buf.position();
      } else if (buf.remaining() < MIN_FREE) {
        // Not enough free-space, go ahead and write
        gen.writeEndArray();
        gen.close();
        write(buf);

        // Reuse the buffer
        out.reset();
        gen = Json.newGenerator(out);
        gen.writeStartArray();
        gen.flush();
        pos = buf.position();
      }
    }

    // Write any data that is still in the buffer
    if (buf.position() > 1) {
      gen.writeEndArray();
      gen.close();
      write(buf);
    }
  }

  @Override public void close() throws IOException {
  }
}
