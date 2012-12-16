package com.github.jknack.arbor;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {
  protected static File homeDir = new File("target/.m2/js");

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  static {
    homeDir.mkdirs();
  }
}
