package com.github.jknack.arbor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArborTest {

  private static File homeDir = new File("target/.m2/js");

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(ArborTest.class);

  static {
    homeDir.mkdirs();
  }

  @Test
  public void jquery() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("http://code.jquery.com/jquery-1.8.3.js");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void backbone() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("backbone/0.9.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void qunit() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("qunit/0.5.14");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jshint() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("jshint/0.9.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jqueryComponent() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("jquery/1.8.3");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void handlebars() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("handlebars/1.0.7");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void request() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("request/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void async() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("async/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void coffeeScript() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("coffee-script/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jade() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("jade/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void socketIO() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("socket.io/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void express() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("express/3.0.4");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void less() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("less/1.3.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jsdom() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("jsdom/0.3.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void mongoose() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("mongoose/3.5.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void mongodb() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("mongodb/1.2.5");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void requirejs() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("requirejs/2.1.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void browserify() throws IOException {
    Dependency resolved = new Arbor(homeDir).resolve("browserify/1.16.6");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

}
