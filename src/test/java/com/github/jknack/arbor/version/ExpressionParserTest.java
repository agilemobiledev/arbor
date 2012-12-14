package com.github.jknack.arbor.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpressionParserTest {

  @Test
  public void simple() {
    assertEquals("0.1.2", ExpressionParser.parse("0.1.2").toString());
    assertEquals("0.1.2-7", ExpressionParser.parse("0.1.2-7").toString());
    assertEquals("0.1.2-beta", ExpressionParser.parse("0.1.2-beta").toString());
    assertEquals("0.1.2-7-beta", ExpressionParser.parse("0.1.2-7-beta")
        .toString());

    assertTrue(ExpressionParser.parse("0.1.2").matches("0.1.2"));

    ExpressionParser.parse("0.4.3");
  }

  @Test
  public void eq() {
    assertEquals("=0.1.2", ExpressionParser.parse("=0.1.2").toString());
    assertEquals("=0.1.2-7", ExpressionParser.parse("=0.1.2-7").toString());
    assertEquals("=0.1.2-beta", ExpressionParser.parse("=0.1.2-beta")
        .toString());
    assertEquals("=0.1.2-7-beta", ExpressionParser.parse("=0.1.2-7-beta")
        .toString());

    assertTrue(ExpressionParser.parse("=0.1.2").matches("0.1.2"));
    assertFalse(ExpressionParser.parse("=0.1.2").matches("0.1.3"));
  }

  @Test
  public void gt() {
//    assertTrue(ExpressionParser.parse(">0.1.2").matches("0.1.3"));
//
//    assertFalse(ExpressionParser.parse(">0.1.2").matches("0.1.2"));
//
//    assertTrue(ExpressionParser.parse(">0.1.2-beta").matches("0.1.2"));

    assertTrue(ExpressionParser.parse(">0.1.2-7-beta").matches("0.1.2-7"));

//    assertTrue(ExpressionParser.parse(">0.1.2-6").matches("0.1.2-7-beta"));
//
//    assertTrue(ExpressionParser.parse(">0.1.2beta").matches("0.1.2-6"));
  }

  @Test
  public void gtEq() {
    assertTrue(ExpressionParser.parse(">=0.1.1").matches("0.1.2"));

    assertTrue(ExpressionParser.parse(">=0.1.2").matches("0.1.2"));

    assertTrue(ExpressionParser.parse(">=0.1.2-beta").matches("0.1.2"));
  }

  @Test
  public void lt() {
    assertTrue(ExpressionParser.parse("<1").matches("0.1.2"));

    assertFalse(ExpressionParser.parse("<0.1.2").matches("0.1.2"));

    assertTrue(ExpressionParser.parse("<0.1.3").matches("0.1.2"));

    assertTrue(ExpressionParser.parse("<0.2").matches("0.1.2"));
  }

  @Test
  public void ltEq() {
    assertTrue(ExpressionParser.parse("<=1").matches("0.1.2"));

    assertTrue(ExpressionParser.parse("<=0.1.2").matches("0.1.2"));

    assertTrue(ExpressionParser.parse("<=0.1.3").matches("0.1.3"));

    assertTrue(ExpressionParser.parse("<=0.2").matches("0.1.1"));
  }

  @Test
  public void tilde() {
    assertTrue(ExpressionParser.parse("~1.2.3").matches("1.2.3"));

    assertFalse(ExpressionParser.parse("~1.2.3").matches("1.2.2"));

    assertTrue(ExpressionParser.parse("~1.2.3").matches("1.2.9"));

    assertFalse(ExpressionParser.parse("~1.2.3").matches("1.3"));

    assertTrue(ExpressionParser.parse("~1.2").matches("1.2.0"));

    assertTrue(ExpressionParser.parse("~1.2").matches("1.2.3"));

    assertTrue(ExpressionParser.parse("~1.2").matches("1.9"));

    assertFalse(ExpressionParser.parse("~1.2").matches("2.0"));
  }

  @Test
  public void x() {
    assertTrue(ExpressionParser.parse("1.2.x").matches("1.2.3"));

    assertFalse(ExpressionParser.parse("1.2.x").matches("1.3"));

    assertTrue(ExpressionParser.parse("1.x.x").matches("1"));

    assertTrue(ExpressionParser.parse("1.x.x").matches("1.0"));

    assertTrue(ExpressionParser.parse("1.x.x").matches("1.0.0"));
    assertTrue(ExpressionParser.parse("1.x.x").matches("1.1.0"));

    assertTrue(ExpressionParser.parse("1.x.x").matches("1.2.4"));

    assertFalse(ExpressionParser.parse("1.x.x").matches("2.0"));
  }

  @Test
  public void uri() {
    assertEquals("http://asdf.com/asdf.tar.gz",
        ExpressionParser.parse("http://asdf.com/asdf.tar.gz").toString());

    assertEquals("git://github.com/user/project.git#commit-ish",
        ExpressionParser.parse("git://github.com/user/project.git#commit-ish")
            .toString());
  }

  @Test
  public void any() {
    assertEquals("*", ExpressionParser.parse("*").toString());

    assertEquals("*", ExpressionParser.parse("").toString());

    assertTrue(ExpressionParser.parse("*").matches("1.2.3"));
    assertTrue(ExpressionParser.parse("").matches("1.2.3"));
  }

  @Test
  public void range() {
    assertEquals("1.0.0 - 2.9999.9999",
        ExpressionParser.parse("1.0.0 - 2.9999.9999").toString());

    assertTrue(ExpressionParser.parse("1.0.0- 2.9999.9999").matches("1.0.0"));
    assertTrue(ExpressionParser.parse("1.0.0 - 2.9999.9999").matches("1.5"));
    assertTrue(ExpressionParser.parse("1.0.0 - 2.9999.9999").matches(
        "2.9999.9999"));

    assertFalse(ExpressionParser.parse("1.0.0 - 2.9999.9999").matches("3"));

    assertEquals(">=1.0.2 <2.1.2",
        ExpressionParser.parse(">=1.0.2 <2.1.2").toString());

    assertTrue(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("1.0.2"));
    assertTrue(ExpressionParser.parse(">=1.0.2 <=2.1.2").matches("2.1.2"));
    assertFalse(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("2.1.2"));

    assertTrue(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("1.0.3"));
    assertTrue(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("1.1"));
    assertTrue(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("2.1.1"));

    assertTrue(ExpressionParser.parse(">=1.0.2 <2.1.2").matches("2.1.1"));

    assertFalse(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("1.0.2"));
    assertTrue(ExpressionParser.parse(">=1.0.2 <=2.3.4").matches("1.0.2"));
    assertTrue(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("1.0.3"));
    assertTrue(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("2.3.4"));
    assertTrue(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("1.5"));
    assertTrue(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("2.3.3"));
    assertFalse(ExpressionParser.parse(">1.0.2 <=2.3.4").matches("2.3.5"));
  }

  @Test
  public void or() {
    assertEquals("1.3.4 || 1.3.5",
        ExpressionParser.parse("1.3.4 || 1.3.5").toString());

    assertTrue(ExpressionParser.parse("1.3.4 || 1.3.5").matches("1.3.4"));
    assertTrue(ExpressionParser.parse("1.3.4 || 1.3.5").matches("1.3.5"));

    assertFalse(ExpressionParser.parse("1.3.4 || 1.3.5").matches("1.3.6"));
    assertFalse(ExpressionParser.parse("1.3.4 || 1.3.5").matches("1.3"));
  }

  @Test
  public void complex() {
    assertEquals("<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0",
        ExpressionParser.parse("<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0")
            .toString());

    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("0.5"));

    assertFalse(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("1.0.5"));

    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.3.1"));

    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.4.4"));

    assertFalse(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.3.0"));
    assertFalse(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.4.6"));
    assertFalse(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.5.0"));

    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.5.2"));
    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.5.5"));
    assertTrue(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("2.9"));
    assertFalse(ExpressionParser.parse(
        "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0").matches("3"));
  }
}
