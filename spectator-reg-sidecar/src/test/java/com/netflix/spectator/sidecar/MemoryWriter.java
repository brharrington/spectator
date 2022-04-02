package com.netflix.spectator.sidecar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MemoryWriter extends SidecarWriter {

  private final List<String> messages;

  MemoryWriter() {
    messages = new ArrayList<>();
  }

  List<String> messages() {
    return messages;
  }

  @Override
  void writeImpl(String line) throws IOException {
    messages.add(line);
  }

  @Override
  public void close() throws Exception {
    messages.clear();
  }
}
