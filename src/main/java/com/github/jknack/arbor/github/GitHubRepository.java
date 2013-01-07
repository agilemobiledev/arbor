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

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.arbor.UnresolvedDependencyException;
import com.github.jknack.semver.Semver;

public class GitHubRepository {
  /**
   * The logging system.
   */

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @JsonProperty
  private String name;

  @JsonProperty
  private String owner;

  private List<GitHubTag> tags;

  public GitHubRepository(final String owner, final String name) {
    this.owner = owner;
    this.name = name;
  }

  public GitHubRepository() {
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return owner;
  }

  public String getId() {
    return owner + "/" + name;
  }

  @Override
  public String toString() {
    return getId();
  }

  public File tarball(final String version) throws IOException {
    String uri = String.format("https://github.com/%s/%s/archive/%s.tar.gz", owner, name, version);
    logger.debug("GET {}", uri);
    File tarball = File.createTempFile(name + "-" + version, ".tar.gz");
    Get(uri).execute().saveContent(tarball);
    return tarball;
  }

  public String get(final String version, final String path) throws IOException {
    String uri = String.format("https://raw.github.com/%s/%s/%s/%s", owner, name, version, path);
    logger.debug("GET {}", uri);
    return Get(uri).execute().handleResponse(new ResponseHandler<String>() {
      @Override
      public String handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
        if (response.getStatusLine().getStatusCode() >= 300) {
          return null;
        }
        return EntityUtils.toString(response.getEntity());
      }
    });
  }

  public String resolveVersion(final String version) throws IOException {
    try {
      Semver expression = Semver.create(version);
      if (expression.isStatic()) {
        return version;
      }
    } catch (IllegalArgumentException ex) {
      // ignore this error
    }
    if (Semver.LATEST.text().equals(version)) {
      return tags.size() == 0 ? "master" : tags.get(0).getName();
    }
    Semver expr = Semver.create(version);
    for (GitHubTag candidate : tags) {
      try {
        if (expr.matches(candidate.getName())) {
          return candidate.getName();
        }
      } catch (IllegalArgumentException ex) {
        break;
      }
    }
    throw new UnresolvedDependencyException("No matches found for: " + name + "@" + version
        + " in: [" + join(tags, ", ") + "]");
  }

  public List<GitHubTag> getTags() {
    return tags;
  }

  /* public */void setTags(final List<GitHubTag> tags) {
    this.tags = tags;
  }
}
