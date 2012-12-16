package com.github.jknack.arbor.bower;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jknack.arbor.BaseTest;
import com.github.jknack.arbor.Dependency;

public class BowerTest extends BaseTest {

  private static BowerResolver bower;

  @BeforeClass
  public static void setup() throws IOException {
    bower = new BowerResolver(homeDir);
  }

  @Test
  public void jquery() throws IOException {
    Dependency resolved = bower.resolve("jquery/latest");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jquery182() throws IOException {
    Dependency resolved = bower.resolve("jquery/1.8.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void jquery181() throws IOException {
    Dependency resolved = bower.resolve("jquery/1.8.1");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void backbone() throws IOException {
    Dependency resolved = bower.resolve("backbone/0.9.9");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void almond() throws IOException {
    Dependency resolved = bower.resolve("almond/0.2.3");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void angular() throws IOException {
    Dependency resolved = bower.resolve("angular/1.0.3");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }

  @Test
  public void dojo() throws IOException {
    Dependency resolved = bower.resolve("dojo/1.8.2");
    assertNotNull(resolved);
    assertTrue("File not found", resolved.exists());
    logger.info("{}", resolved);
  }
}
