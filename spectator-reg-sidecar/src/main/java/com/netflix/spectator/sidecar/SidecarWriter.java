package com.netflix.spectator.sidecar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

abstract class SidecarWriter implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SidecarWriter.class);

  public static SidecarWriter create(String location) throws IOException {
    if ("none".equals(location)) {
      return new NoopWriter();
    } if ("stderr".equals(location)) {
      return new PrintStreamWriter(System.err);
    } else if ("stdout".equals(location)) {
      return new PrintStreamWriter(System.out);
    } else if (location.startsWith("file://")) {
      FileOutputStream out = new FileOutputStream(URI.create(location).getPath());
      return new PrintStreamWriter(new PrintStream(out));
    } else if (location.startsWith("udp://")) {
      URI uri = URI.create(location);
      String host = uri.getHost();
      int port = uri.getPort();
      SocketAddress address = new InetSocketAddress(host, port);
      return new UdpWriter(address);
    } else {
      throw new IllegalArgumentException("unsupported location: " + location);
    }
  }

  abstract void writeImpl(String line) throws IOException;

  void write(String line) {
    try {
      LOGGER.trace("writing: {}", line);
      writeImpl(line);
    } catch (IOException e) {
      LOGGER.warn("write failed: {}", line, e);
    }
  }

  void write(String prefix, long value) {
    write(prefix + value);
  }

  void write(String prefix, double value) {
    write(prefix + value);
  }
}
