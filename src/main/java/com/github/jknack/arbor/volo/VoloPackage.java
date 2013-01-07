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
package com.github.jknack.arbor.volo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

class VoloPackage {

  private Map<String, Object> volo = new HashMap<String, Object>();

  public String getUrl() {
    return (String) volo.get("url");
  }

  @JsonIgnore
  public Map<String, String> getDependencies() {
    @SuppressWarnings("unchecked")
    Map<String, String> dependencies = (Map<String, String>) volo.get("dependencies");
    if (dependencies == null) {
      return Collections.emptyMap();
    }
    return dependencies;
  }
}
