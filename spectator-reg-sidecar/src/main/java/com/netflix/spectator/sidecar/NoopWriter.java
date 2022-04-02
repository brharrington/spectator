package com.netflix.spectator.sidecar;

import java.io.IOException;

class NoopWriter extends SidecarWriter {
  @Override
  void writeImpl(String line) throws IOException {
  }

  @Override
  public void close() throws Exception {
  }
}
