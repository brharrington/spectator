package com.netflix.spectator.tdigest;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

/**
 * Created by brharrington on 2/24/15.
 */
public class KinesisTDigestWriter implements TDigestWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(KinesisTDigestWriter.class);

  // TODO: fill up 50k buffer. This assumes a ~12k worst case per measurement.
  private static final int MAX_PER_RECORD = 4;

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

  @Override public void write(List<TDigestMeasurement> measurements) {
    try {
      for (int i = 0; i < measurements.size(); i += MAX_PER_RECORD) {
        byte[] data = Json.encode(measurements.subList(i, i + MAX_PER_RECORD));
        client.putRecord(stream, ByteBuffer.wrap(data), partitionKey());
      }
    } catch (Exception e) {
      LOGGER.error("failed to write to kinesis", e);
    }
  }

  @Override public void close() {
  }
}
