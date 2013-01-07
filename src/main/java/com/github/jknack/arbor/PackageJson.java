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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageJson {

  private String name;

  private String version;

  private Object main;

  private Map<String, String> dependencies = new HashMap<String, String>();

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String resolveMain(final File baseDir) {
    String main = null;
    if (this.main instanceof List) {
      @SuppressWarnings("unchecked")
      List<String> candidates = (List<String>) this.main;
      if (candidates.size() == 1) {
        main = candidates.get(0);
      } else {
        for (String candidate : candidates) {
          if (candidate.endsWith(".js")) {
            main = candidate;
            break;
          }
        }
      }
    } else {
      main = this.getMain() == null ? null : getMain().toString();
    }
    if (isEmpty(main)) {
      // fallback and use module's name.
      main = name + ".js";
    }
    if (!main.endsWith(".js")) {
      main += ".js";
    }
    if (main.startsWith(".")) {
      main = main.substring(1);
    }
    if (main.startsWith("/")) {
      main = main.substring(1);
    }
    return main;
  }

  public Object getMain() {
    return main;
  }

  public void setMain(final String main) {
    this.main = main;
  }

  public Map<String, String> getDependencies() {
    return dependencies;
  }

  /* package */void setDependencies(final Map<String, String> dependencies) {
    this.dependencies = dependencies;
  }

}
