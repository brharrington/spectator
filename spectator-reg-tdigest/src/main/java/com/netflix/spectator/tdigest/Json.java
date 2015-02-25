package com.netflix.spectator.tdigest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.netflix.spectator.api.DefaultId;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Tag;
import com.tdunning.math.stats.AVLTreeDigest;
import com.tdunning.math.stats.TDigest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brharrington on 2/24/15.
 */
final class Json {

  private static final JsonFactory FACTORY = new SmileFactory();

  private Json() {
  }

  static void encode(TDigestMeasurement m, JsonGenerator gen) throws IOException {
    TDigest digest = m.value();
    digest.compress();
    ByteBuffer buf = ByteBuffer.allocate(digest.byteSize());
    digest.asBytes(buf);

    gen.writeStartArray();
    gen.writeStartObject();
    gen.writeStringField("name", m.id().name());
    for (Tag t : m.id().tags()) {
      gen.writeStringField(t.key(), t.value());
    }
    gen.writeEndObject();
    gen.writeNumber(m.timestamp());
    gen.writeBinary(buf.array());
    gen.writeEndArray();
  }

  static void encode(List<TDigestMeasurement> ms, JsonGenerator gen) throws IOException {
    gen.writeStartArray();
    for (TDigestMeasurement m : ms) {
      encode(m, gen);
    }
    gen.writeEndArray();
  }

  static byte[] encode(List<TDigestMeasurement> ms) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (JsonGenerator gen = FACTORY.createGenerator(baos)) {
      encode(ms, gen);
    }
    return baos.toByteArray();
  }

  private static void require(boolean condition, String msg) {
    if (!condition) {
      throw new IllegalArgumentException(msg);
    }
  }

  private static void expect(JsonParser parser, JsonToken expected) throws IOException {
    JsonToken t = parser.nextToken();
    if (t != expected) {
      String msg = String.format("expected %s, but found %s", expected, t);
      throw new IllegalArgumentException(msg);
    }
  }

  static TDigestMeasurement decode(JsonParser parser) throws IOException {
    //System.err.println("###### here");
    expect(parser, JsonToken.START_OBJECT);
    require("name".equals(parser.nextFieldName()), "expected name");
    DefaultId id = new DefaultId(parser.nextTextValue());
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      id = id.withTag(parser.getText(), parser.nextTextValue());
    }
    long t = parser.nextLongValue(-1L);
    expect(parser, JsonToken.VALUE_EMBEDDED_OBJECT);
    TDigest v = AVLTreeDigest.fromBytes(ByteBuffer.wrap(parser.getBinaryValue()));
    expect(parser, JsonToken.END_ARRAY);
    return new TDigestMeasurement(id, t, v);
  }

  static List<TDigestMeasurement> decode(byte[] data) throws IOException {
    JsonParser parser = FACTORY.createParser(data);
    List<TDigestMeasurement> ms = new ArrayList<>();
    expect(parser, JsonToken.START_ARRAY);
    while (parser.nextToken() == JsonToken.START_ARRAY) {
      ms.add(decode(parser));
    }
    return ms;
  }
}
