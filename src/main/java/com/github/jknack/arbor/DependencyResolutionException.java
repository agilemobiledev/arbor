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

import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;

/**
 * Raised when a dependency problem is found.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
@SuppressWarnings("serial")
public class DependencyResolutionException extends IOException {

  /**
   * Creates a new {@link DependencyResolutionException}.
   *
   * @param dependencyPath The dependency path. Required.
   * @param cause The cause. Required.
   */
  public DependencyResolutionException(final Iterable<String> dependencyPath,
      final Throwable cause) {
    super("Can't resolve: " + join(dependencyPath, "/") + ". Reason: " + cause.getMessage(),
        cause);
  }
}
