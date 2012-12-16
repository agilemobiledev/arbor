package com.github.jknack.arbor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ArborTest extends BaseTest {

  private static Arbor arbor = new Arbor(homeDir);
  @Test
  public void jquery() throws IOException {
    Dependency resolved = arbor.resolve("http://code.jquery.com/jquery-1.8.3.js");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void backbone() throws IOException {
    Dependency resolved = arbor.resolve("backbone/0.9.9");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void backboneMarionette() throws IOException {
    Dependency resolved = arbor.resolve("Backbone.Marionette/1.0.0-beta6");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void underscore() throws IOException {
    Dependency resolved = arbor.resolve("underscore/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void knockout() throws IOException {
    Dependency resolved = arbor.resolve("knockout/2.2.0");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void crossroads() throws IOException {
    Dependency resolved = arbor.resolve("crossroads/0.10.0");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void i18next() throws IOException {
    Dependency resolved = arbor.resolve("i18next/1.5.6");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void qunit() throws IOException {
    Dependency resolved = arbor.resolve("qunit/0.5.14");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jshint() throws IOException {
    Dependency resolved = arbor.resolve("jshint/0.9.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jqueryComponent() throws IOException {
    Dependency resolved = arbor.resolve("jquery/1.8.3");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void handlebars() throws IOException {
    Dependency resolved = arbor.resolve("handlebars/1.0.7");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void request() throws IOException {
    Dependency resolved = arbor.resolve("request/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void async() throws IOException {
    Dependency resolved = arbor.resolve("async/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void coffeeScript() throws IOException {
    Dependency resolved = arbor.resolve("npm@coffee-script/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jade() throws IOException {
    Dependency resolved = arbor.resolve("jade/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void socketIO() throws IOException {
    Dependency resolved = arbor.resolve("socket.io/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void express() throws IOException {
    Dependency resolved = arbor.resolve("express/3.0.4");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void less() throws IOException {
    Dependency resolved = arbor.resolve("less/1.3.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jsdom() throws IOException {
    Dependency resolved = arbor.resolve("jsdom/0.3.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void mongoose() throws IOException {
    Dependency resolved = arbor.resolve("mongoose/3.5.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void mongodb() throws IOException {
    Dependency resolved = arbor.resolve("mongodb/1.2.5");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void requirejs() throws IOException {
    Dependency resolved = arbor.resolve("requirejs/2.1.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void browserify() throws IOException {
    Dependency resolved = arbor.resolve("browserify/1.16.6");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jam() throws IOException {
    Dependency resolved = arbor.resolve("jam/0.2.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void volo() throws IOException {
    Dependency resolved = arbor.resolve("volo/0.2.6");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

}
