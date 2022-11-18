package com.netflix.spectator.atlas;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayOutputStream;
import java.util.Random;

@State(Scope.Thread)
public class DecodeShortAsciiValues {

  private static int N = 100;

  private JsonFactory factory;
  private byte[] inputData;

  private static String randomShortAsciiString() {
    Random r = new Random();
    final int length = r.nextInt(64);
    final char[] buf = new char[length];
    for (int i = 0; i < length; ++i) {
      buf[i] = (char) (r.nextInt('~' - ' ') + ' ');
    }
    return new String(buf);
  }

  @Setup
  public void setup() throws Exception {
    factory = new SmileFactory();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (JsonGenerator gen = factory.createGenerator(baos)) {
      gen.writeStartArray();
      for (int i = 0; i < N; ++i) {
        gen.writeString(randomShortAsciiString());
      }
      gen.writeEndArray();
    }
    inputData = baos.toByteArray();
  }

  @Benchmark
  public void parse(Blackhole bh) throws Exception {
    String[] strings = new String[N];
    try (JsonParser parser = factory.createParser(inputData)) {
      parser.nextToken();
      int i = 0;
      while (parser.nextToken() != JsonToken.END_ARRAY) {
        strings[i++] = parser.getText();
      }
    }
    bh.consume(strings);
  }

  public static void main(String[] args) {
    System.out.println(randomShortAsciiString());
  }
}
