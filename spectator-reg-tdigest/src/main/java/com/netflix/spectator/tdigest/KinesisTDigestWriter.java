package com.netflix.spectator.tdigest;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by brharrington on 2/24/15.
 */
public class KinesisTDigestWriter extends TDigestWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(KinesisTDigestWriter.class);

  private final Random random = new Random();

  private final AmazonKinesisClient client;
  private final String stream;

  public KinesisTDigestWriter(AmazonKinesisClient client, String stream) {
    this.client = client;
    this.stream = stream;
  }

  private String partitionKey() {
    StringBuilder buf = new StringBuilder(8);
    for (int i = 0; i < 8; ++i) {
      buf.append((char) '0' + random.nextInt('z' - '0'));
    }
    return buf.toString();
  }

  @Override void write(ByteBuffer data) {
    client.putRecord(stream, data, partitionKey());
  }

  @Override public void close() {
  }
}
