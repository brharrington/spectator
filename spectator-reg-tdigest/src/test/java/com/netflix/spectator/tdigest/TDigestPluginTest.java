/**
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Statistic;
import com.netflix.spectator.api.Timer;
import com.netflix.spectator.api.Utils;
import com.tdunning.math.stats.AVLTreeDigest;
import com.tdunning.math.stats.AbstractTDigest;
import com.tdunning.math.stats.TDigest;
import com.tdunning.math.stats.TreeDigest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class TDigestPluginTest {

  private final ManualClock clock = new ManualClock();

  @Before
  public void before() {
    clock.setWallTime(0L);
    clock.setMonotonicTime(0L);
  }

  private List<List<TDigestMeasurement>> readFromFile(File f) throws IOException {
    List<List<TDigestMeasurement>> ms = new ArrayList<>();
    try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
      int size = in.readInt();
      byte[] buf = new byte[size];
      in.read(buf);
      ms.add(Json.decode(buf));
    } catch (EOFException e) {

    }
    return ms;
  }

  @Test
  public void writeData() throws Exception {
    final File f = new File("build/TDigestPlugin_writeData.out");
    f.getParentFile().mkdirs();
    if (f.exists()) f.delete();
    System.err.println(f.getCanonicalPath());
    final TDigestRegistry r = new TDigestRegistry(clock);
    final TDigestPlugin p = new TDigestPlugin(r, new FileTDigestWriter(f));

    Id one = r.createId("one");
    Id many = r.createId("many");
    for (int i = 0; i < 10000; ++i) {
      r.timer(one).record(i, TimeUnit.MILLISECONDS);
      r.timer(many.withTag("i", "" + (i / 100))).record(i, TimeUnit.MILLISECONDS);
    }

    clock.setWallTime(61000);
    p.writeData();

    clock.setWallTime(121000);
    p.writeData();

    List<List<TDigestMeasurement>> ms = readFromFile(f);
    for (List<TDigestMeasurement> m : ms) {
      checkRecord(r, m);
    }
  }

  private void checkRecord(Registry r, List<TDigestMeasurement> ms) {
    Random random = new Random();
    TDigest one = null;
    TDigest many = TDigest.createDigest(100.0);
    for (TDigestMeasurement m : ms) {
      if ("one".equals(m.id().name())) {
        one = m.value();
      } else {
        List<TDigest> vs = new ArrayList<>(2);
        vs.add(many);
        vs.add(m.value());
        many = TreeDigest.merge(100.0, vs, random);
      }
    }
    for (int i = 0; i < 1000; ++i) {
      double q = i / 1000.0;
      //System.err.printf("%f == %f%n", one.quantile(q), many.quantile(q));
      Assert.assertEquals(one.quantile(q), many.quantile(q), 1e-1);
    }
  }
}
