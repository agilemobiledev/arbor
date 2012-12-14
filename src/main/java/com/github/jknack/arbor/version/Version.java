/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.github.jknack.arbor.version;

import java.util.Arrays;
import java.util.List;

/**
 * A CommonJS Version representation.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
class Version extends BaseExpression implements Expression {

  /**
   * The major qualifier.
   */
  private int major;

  /**
   * The minor qualifier.
   */
  private int minor;

  /**
   * The incremental qualifier.
   */
  private int incremental;

  /**
   * The build qualifier.
   */
  private int build;

  /**
   * The tag qualifier.
   */
  private String tag;

  /**
   * The text.
   */
  private String text;

  /**
   * Creates a new version.
   *
   * @param text The text.
   * @param major The major qualifier.
   * @param minor The minor qualifier.
   * @param incremental The incremental qualifier.
   */
  public Version(final String text, final int major, final int minor,
      final int incremental) {
    this.text = text;
    this.major = major;
    this.minor = minor;
    this.incremental = incremental;
  }

  /**
   * Default constructor.
   */
  public Version() {
  }

  @Override
  public int compareTo(final Expression expr) {
    return compareTo((Version) expr);
  }

  /**
   * Compare this version.
   *
   * @param that The other version.
   * @return If this > that, 1. If that > this -1. If this == that, 0.
   */
  public int compareTo(final Version that) {
    int result = 0;
    if (this == that) {
      return result;
    }
    result = major - that.major;
    if (result != 0) {
      return result;
    }
    result = minor - that.minor;
    if (result != 0) {
      return result;
    }
    result = incremental - that.incremental;
    if (result != 0) {
      return result;
    }
    result = build - that.build;
    if (result != 0) {
      return result;
    }
    // tag
    if (tag == null) {
      return that.tag == null ? 0 : 1;
    }
    if (that.tag == null) {
      return -1;
    }
    return tag.compareToIgnoreCase(that.tag);
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    return that instanceof Version ? compareTo((Version) that) == 0 : false;
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public boolean matches(final Expression expr) {
    return this.equals(expr);
  }

  /**
   * Find the next major version of this one.
   *
   * @return The next major version.
   */
  public Version nextMajor() {
    int major = this.major;
    int minor = this.minor;
    int incremental = this.incremental;
    List<String> parts = Arrays.asList(text.split("\\."));
    int idx = parts.indexOf("x");
    if (idx == 1) {
      major += 1;
      minor = 0;
      incremental = 0;
    } else if (idx == 2) {
      minor += 1;
      incremental = 0;
    } else {
      if (incremental == 0) {
        minor = 0;
        major += 1;
      } else {
        incremental = 0;
        if (minor == 0) {
          major += 1;
        } else {
          minor += 1;
        }
      }
    }
    return new Version(major + "." + minor + "." + incremental, major, minor,
        incremental);
  }

  /**
   * The build qualifier.
   *
   * @return The build qualifier.
   */
  public int getBuild() {
    return build;
  }

  /**
   * The incremental qualifier.
   *
   * @return The incremental qualifier.
   */
  public int getIncremental() {
    return incremental;
  }

  /**
   * The major qualifier.
   *
   * @return The major qualifier.
   */
  public int getMajor() {
    return major;
  }

  /**
   * The minor qualifier.
   *
   * @return The minor qualifier.
   */
  public int getMinor() {
    return minor;
  }

  /**
   * The tag qualifier.
   *
   * @return The tag qualifier.
   */
  public String getTag() {
    return tag;
  }

  /**
   * The text qualifier.
   *
   * @return The text qualifier.
   */
  public String getText() {
    return text;
  }

  /**
   * Set the build qualifier.
   *
   * @param build the build qualifier.
   */
  public void setBuild(final int build) {
    this.build = build;
  }

  /**
   * Set the incremental qualifier.
   *
   * @param incremental the incremental qualifier.
   */
  public void setIncremental(final int incremental) {
    this.incremental = incremental;
  }

  /**
   * Set the major qualifier.
   *
   * @param major the major qualifier.
   */
  public void setMajor(final int major) {
    this.major = major;
  }

  /**
   * Set the minor qualifier.
   *
   * @param minor the minor qualifier.
   */
  public void setMinor(final int minor) {
    this.minor = minor;
  }

  /**
   * Set the tag qualifier.
   *
   * @param tag the tag qualifier.
   */
  public void setTag(final String tag) {
    this.tag = tag;
  }

  /**
   * Set the text qualifier.
   *
   * @param text the text qualifier.
   */
  public void setText(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

}
