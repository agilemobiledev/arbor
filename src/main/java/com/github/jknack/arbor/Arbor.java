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

import java.io.File;
import java.io.IOException;

import com.github.jknack.arbor.npm.NpmResolver;

/**
 * A facade over dependency resolvers.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Arbor {

  /**
   * The dependency resolvers.
   */
  private DependencyResolver[] resolvers;

  /**
   * Creates a new dependency resolver.
   *
   * @param homeDir The home directory.
   */
  public Arbor(final File homeDir) {
    this(new RemoteResolver(homeDir), new NpmResolver(homeDir));
  }

  /**
   * Creates a new dependency resolver.
   *
   * @param resolvers A list of dependency resolvers.
   */
  public Arbor(final DependencyResolver... resolvers) {
    this.resolvers = notEmpty(resolvers, "The resolvers is required.");
  }

  /**
   * Resolve a dependency path to a dependency graph.
   *
   * @param path The dependency's path.
   * @return A dependency graph or an {@link IllegalArgumentException} if the dependency path cannot
   *         be resolved.
   * @throws IOException If a dependency resolution fails.
   */
  public Dependency resolve(final String path) throws IOException {
    for (DependencyResolver resolver : resolvers) {
      Dependency resolved = resolver.resolve(path);
      if (resolved != null && resolved.exists()) {
        return resolved;
      }
    }
    throw new IllegalArgumentException("Cannot resolve: " + path);
  }
}
