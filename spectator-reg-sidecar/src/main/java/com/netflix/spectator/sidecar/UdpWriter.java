package com.netflix.spectator.sidecar;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

class UdpWriter extends SidecarWriter {

  private final DatagramChannel channel;

  UdpWriter(SocketAddress address) throws IOException {
    super();
    this.channel = DatagramChannel.open();
    this.channel.connect(address);
  }

  @Override
  public void writeImpl(String line) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
    channel.write(buffer);
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }
}
