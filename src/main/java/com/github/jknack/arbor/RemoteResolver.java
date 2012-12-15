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
package com.github.jknack.arbor;

import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;

/**
 * Get dependencies from a HTTP URI.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class RemoteResolver extends DependencyResolver {

  /**
   * Creates a new {@link RemoteResolver}.
   *
   * @param homeDir The repository home directory. Required.
   */
  public RemoteResolver(final File homeDir) {
    super(homeDir);
  }

  @Override
  protected Dependency doResolve(final String path) throws IOException {
    logger.info("GET: {}", path);
    Dependency dependency = resolveLocal(path);
    File location = dependency.getLocation();
    location.getParentFile().mkdirs();
    Get(path).execute().saveContent(location);
    logger.info("200: {}", path);
    return dependency;
  }

  @Override
  protected boolean canResolve(final String path) throws IOException {
    URI uri = URI.create(path);
    return "http".equals(uri.getScheme());
  }

  @Override
  protected Dependency resolveLocal(final String path) throws IOException {
    URI uri = URI.create(path);
    StringBuilder buffer = new StringBuilder();
    String[] host = uri.getHost().split("\\.");
    for (int i = host.length - 1; i >= 0; i--) {
      buffer.append(host[i]).append(File.separator);
    }
    String[] tail = StringUtils.split(uri.getPath(), "/");
    for (int i = 0; i < tail.length - 1; i++) {
      buffer.append(tail[i]).append(File.separator);
    }
    buffer.append(tail[tail.length - 1]);
    File location = new File(homeDir, buffer.toString());
    return new Dependency(path, location);
  }

  @Override
  public String name() {
    return "http";
  }
}
