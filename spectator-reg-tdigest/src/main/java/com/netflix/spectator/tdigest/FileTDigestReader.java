package com.netflix.spectator.tdigest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by brharrington on 2/28/15.
 */
public class FileTDigestReader extends StreamTDigestReader {

  public FileTDigestReader(File f) throws FileNotFoundException {
    super(new FileInputStream(f));
  }
}
