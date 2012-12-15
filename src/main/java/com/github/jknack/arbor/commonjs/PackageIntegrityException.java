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

import java.io.File;
import java.io.IOException;

/**
 * Raised when a corrupted package is detected.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
@SuppressWarnings("serial")
public class PackageIntegrityException extends IOException {

  /**
   * Creates a new {@link PackageIntegrityException}.
   *
   * @param id The package's id.
   * @param location The location of the package.
   */
  public PackageIntegrityException(final String id, final File location) {
    super("Invalid 'main' entry for module: " + id + ". Reason: file not found: " + location);
  }

}
