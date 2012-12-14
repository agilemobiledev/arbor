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
package com.github.jknack.arbor.commonjs;

import java.net.URI;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The distribution entry from a package.json file.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
class Distribution {

  /**
   * The sha1 of the tarball.
   */
  @JsonProperty
  private String shasum;

  /**
   * The tarball URI.
   */
  @JsonProperty
  private URI tarball;

  /**
   * The sha1 of the tarball.
   *
   * @return The sha1 of the tarball.
   */
  public String getShasum() {
    return shasum;
  }

  /**
   * The tarball URI.
   *
   * @return The tarball URI.
   */
  public URI getTarball() {
    return tarball;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("tarball", tarball).append("shasum", shasum).build();
  }
}
