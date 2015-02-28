package com.netflix.spectator.tdigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by brharrington on 2/24/15.
 */
public class FileTDigestWriter extends TDigestWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileTDigestWriter.class);

  private final File file;
  private final byte[] buf = new byte[BUFFER_SIZE];

  public FileTDigestWriter(File file) {
    this.file = file;
  }

  @Override void write(ByteBuffer data) throws IOException {
    try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file, true))) {
      int len = data.limit();
      data.get(buf, 0, len);
      out.writeInt(data.limit());
      out.write(buf, 0, len);
    }
  }

  @Override public void close() {

  }
}
