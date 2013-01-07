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

import java.io.File;

public interface DependencyContext {

  void put(final ModuleId moduleId, final Module module);

  File moduleRoot(final ModuleId moduleId);

  File moduleHome(final ModuleId moduleId);

  File getBaseDir();

  Module get(final ModuleId moduleId);
}
