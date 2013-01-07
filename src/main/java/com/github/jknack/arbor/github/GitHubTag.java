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
package com.github.jknack.arbor.github;

import java.net.URI;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.semver.Semver;

public class GitHubTag {

  public static final Comparator<GitHubTag> DESC = new Comparator<GitHubTag>() {
    @Override
    public int compare(final GitHubTag t1, final GitHubTag t2) {
      return Semver.DESC.compare(t1.name, t2.name);
    }
  };

  @JsonProperty
  private String name;

  @JsonProperty
  private URI tarball_url;

  public String getName() {
    return name;
  }

  public URI getTarball() {
    return tarball_url;
  }

  @Override
  public String toString() {
    return name;
  }
}
