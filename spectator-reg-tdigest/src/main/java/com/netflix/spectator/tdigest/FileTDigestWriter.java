package com.netflix.spectator.tdigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by brharrington on 2/24/15.
 */
public class FileTDigestWriter implements TDigestWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileTDigestWriter.class);

  private final File file;

  public FileTDigestWriter(File file) {
    this.file = file;
  }

  @Override public void write(List<TDigestMeasurement> measurements) {
    System.err.println("start");
    try {
      try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file, true))) {
        byte[] data = Json.encode(measurements);
        out.writeInt(data.length);
        out.write(data, 0, data.length);
      }
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.error("failed to write measurements to file " + file, e);
    }
    System.err.println("stop");
  }

  @Override public void close() {

  }
}
