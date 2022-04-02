package com.netflix.spectator.sidecar;

import java.io.IOException;
import java.io.PrintStream;

class PrintStreamWriter extends SidecarWriter {

  private final PrintStream stream;

  PrintStreamWriter(PrintStream stream) {
    super();
    this.stream = stream;
  }

  @Override
  public void writeImpl(String line) throws IOException {
    stream.println(line);
  }

  @Override
  public void close() throws Exception {
    stream.close();
  }
}
