package com.netflix.spectator.tdigest;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by brharrington on 2/27/15.
 */
public class ByteBufferOutputStream extends OutputStream implements DataOutput {

  private final ByteBuffer buf;
  private final int reserve;
  private boolean overflow;

  public ByteBufferOutputStream(ByteBuffer buf, int reserve) {
    this.buf = buf;
    this.reserve = reserve;
    reset();
  }

  ByteBuffer buffer() {
    return buf;
  }

  void reset() {
    buf.clear();
    overflow = false;
  }

  boolean overflow() {
    return overflow;
  }

  private boolean checkCapacity(int size) {
    if (buf.remaining() < size + reserve) {
      overflow = true;
    }
    return !overflow;
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
    if (checkCapacity(1)) {
      buf.put((byte) (v ? 1 : 0));
    }
  }

  @Override
  public void writeByte(int v) throws IOException {
    if (checkCapacity(1)) {
      buf.put((byte) v);
    }
  }

  @Override
  public void writeShort(int v) throws IOException {
    if (checkCapacity(2)) {
      buf.putShort((short) v);
    }
  }

  @Override
  public void writeChar(int v) throws IOException {
    if (checkCapacity(2)) {
      buf.putChar((char) v);
    }
  }

  @Override
  public void writeInt(int v) throws IOException {
    if (checkCapacity(4)) {
      buf.putInt((int) v);
    }
  }

  @Override
  public void writeLong(long v) throws IOException {
    if (checkCapacity(8)) {
      buf.putLong((long) v);
    }
  }

  @Override
  public void writeFloat(float v) throws IOException {
    if (checkCapacity(4)) {
      buf.putFloat((float) v);
    }
  }

  @Override
  public void writeDouble(double v) throws IOException {
    if (checkCapacity(8)) {
      buf.putDouble((double) v);
    }
  }

  @Override
  public void writeBytes(String s) throws IOException {
    writeUTF(s);
  }

  @Override
  public void writeChars(String s) throws IOException {
    writeUTF(s);
  }

  @Override
  public void writeUTF(String s) throws IOException {
    byte[] data = s.getBytes("UTF-8");
    if (checkCapacity(4 + data.length)) {
      buf.putInt(data.length);
      buf.put(data);
    }
  }

  @Override
  public void write(int b) throws IOException {
    if (checkCapacity(1)) {
      buf.put((byte) b);
    }
  }

  @Override public void close() throws IOException {
    buf.flip();
  }
}
