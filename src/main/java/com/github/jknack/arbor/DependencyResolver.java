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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Arbor dependency resolvers.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
public abstract class DependencyResolver {

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The home location of the dependencies. Required.
   */
  protected final File homeDir;

  /**
   * Creates a new dependency resolver.
   *
   * @param homeDir The home location of the dependencies. Required.
   */
  public DependencyResolver(final File homeDir) {
    this.homeDir = notNull(homeDir, "The homeDir is required.");
  }

  /**
   * The resolver's name.
   *
   * @return The resolver's name.
   */
  public abstract String name();

  /**
   * The home location of the dependencies. Required.
   *
   * @return The home location of the dependencies. Required.
   */
  public final File homeDir() {
    return homeDir;
  }

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param path The dependency path.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  public Dependency resolve(final String path) throws IOException {
    notEmpty(path, "The path is required.");
    try {
      Dependency dependency = null;
      if (canResolve(path)) {
        dependency = resolveLocal(path);
        if (!dependency.exists()) {

          dependency = doResolve(path);
        }
      }
      return dependency;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        return null;
      }
      throw ex;
    }
  }

  /**
   * True, if the path can be resolved by the dependency resolver.
   *
   * @param path The dependency path. Required.
   * @return True, if the path can be resolved by the dependency resolver.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract boolean canResolve(String path) throws IOException;

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param path The dependency path.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract Dependency doResolve(String path) throws IOException;

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param path The dependency path.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract Dependency resolveLocal(String path) throws IOException;

}
