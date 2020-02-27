package com.netflix.spectator.stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

final class UdpUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpUtils.class);

  private static final DatagramChannel CHANNEL = createChannel();

  private static final CharsetEncoder ENCODER = StandardCharsets.UTF_8.newEncoder();

  private static DatagramChannel createChannel() {
    try {
      DatagramChannel ch = DatagramChannel.open();
      ch.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 1234));
      return ch;
    } catch (IOException e) {
      LOGGER.warn("failed to open channel, metrics will be disabled", e);
      return null;
    }
  }

  static void send(StringBuilder msg) {
    try {
      if (CHANNEL != null) {
        LOGGER.debug("writing message: {}", msg);
        CHANNEL.write(ENCODER.encode(CharBuffer.wrap(msg)));
      }
    } catch (CharacterCodingException e) {
      LOGGER.warn("failed to encode message: {}", msg, e);
    } catch (IOException e) {
      LOGGER.warn("failed to send message: {}", msg, e);
    }
  }
}
