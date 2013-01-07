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
package com.github.jknack.arbor.jam;

import java.util.Map;

import com.github.jknack.arbor.PackageJson;

class JamPackage extends PackageJson {

  private JamPackage jam;

  @Override
  public Map<String, String> getDependencies() {
    return jam == null ? super.getDependencies() : jam.getDependencies();
  }

  @Override
  public Object getMain() {
    return jam == null ? super.getMain() : jam.getMain();
  }
}
