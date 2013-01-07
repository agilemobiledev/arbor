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

import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.jknack.arbor.UnresolvedDependencyException;
import com.github.jknack.semver.Semver;

class JamEntry {

  private Map<String, JamPackage> versions;

  private String name;

  public JamPackage getVersion(final String version) throws IOException {
    JamPackage jamPackage = versions.get(version);
    if (jamPackage == null) {
      throw new UnresolvedDependencyException("No matches found for: " + name + "@" + version
          + " in: [" + join(versions.keySet(), ", ") + "]");
    }
    return jamPackage;
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
    List<String> versionList = new ArrayList<String>(versions.keySet());
    Collections.sort(versionList, Semver.DESC);
    if (Semver.LATEST.text().equals(version)) {
      return versionList.get(0);
    }
    Semver expr = Semver.create(version);
    for (String candidate : versionList) {
      if (expr.matches(candidate)) {
        return candidate;
      }
    }
    throw new UnresolvedDependencyException("No matches found for: " + name + "@" + version
        + " in: [" + join(versionList, ", ") + "]");
  }
}
