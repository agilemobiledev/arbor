package com.github.jknack.arbor;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArborTest {

  static File baseDir = new File("target/arbor");

  static Arbor resolver = new Arbor(baseDir);

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @BeforeClass
  public static void cleanup() throws IOException {
//    resolver.setResolvers("volo");
//    FileUtils.deleteDirectory(baseDir);
    baseDir.mkdirs();
  }

  @Test
  public void _jquery() throws IOException {
    Module module = resolver.resolve(new ModuleId("jquery", "1.8.3"));
    assertNotNull(module);
  }

  @Test
  public void jqueryScrollTo() throws IOException {
    Module module = resolver.resolve(new ModuleId("jquery.scrollTo", "1.4.4"));
    assertNotNull(module);
  }

  @Test
  public void packageJson() throws IOException {
    Module module = resolver.resolve(new File("src/test/resources/package.json"));
    assertNotNull(module);
    logger.info("{}", module);
    File output = new File("target/webapp");
    output.mkdirs();
    module.copyTo(output);
  }

  @Test
  public void backbone() throws IOException {
    Module module = resolver.resolve(new ModuleId("backbone", "0.9.9"));
    assertNotNull(module);
  }

  @Test
  public void requirejs() throws IOException {
    Module module = resolver.resolve(new ModuleId("requirejs", "2.1.2"));
    assertNotNull(module);
  }

  @Test
  public void amdefine() throws IOException {
    Module module = resolver.resolve(new ModuleId("amdefine", "0.0.4"));
    assertNotNull(module);
  }

  @Test
  public void jqueryuiamd() throws IOException {
    Module module = resolver.resolve(new ModuleId("jqueryui-amd", "latest"));
    assertNotNull(module);
  }

  @Test
  public void almond() throws IOException {
    Module module = resolver.resolve(new ModuleId("almond", "0.2.3"));
    assertNotNull(module);
  }

  @Test
  public void handlebars() throws IOException {
    Module module = resolver.resolve(new ModuleId("handlebars.js", "latest"));
    assertNotNull(module);
  }

  @Test
  public void turbinejs() throws IOException {
    Module module = resolver.resolve(new ModuleId("turbine.js", "latest"));
    assertNotNull(module);
  }

  @Test
  public void zepto() throws IOException {
    Module module = resolver.resolve(new ModuleId("zepto", "latest"));
    assertNotNull(module);
  }

}
