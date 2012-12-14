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
package com.github.jknack.arbor.npm;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.arbor.version.Expression;
import com.github.jknack.arbor.version.ExpressionParser;

/**
 * A npm entry.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class NpmEntry {

  /**
   * The entry's name.
   */
  @JsonProperty
  private String name;

  /**
   * The time set of the available modules.
   */
  @JsonProperty
  private Map<String, Date> time;

  /**
   * The entry's name.
   *
   * @return The entry's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Find the latest version of the module.
   *
   * @return The latest version of the module.
   */
  public String latest() {
    String latest = null;
    long date = Long.MIN_VALUE;
    for (Entry<String, Date> entry : time.entrySet()) {
      long time = entry.getValue().getTime();
      if (time > date) {
        latest = entry.getKey();
        date = time;
      }
    }
    return latest;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).build();
  }

  /**
   * Resolve the given version expression to a real module's version.
   *
   * @param version A version expression.
   * @return The best matching version.
   */
  public String resolveVersion(final String version) {
    if (isEmpty(version) || Expression.LATEST.equals(version)) {
      return latest();
    }
    List<Entry<String, Date>> timed = new ArrayList<Map.Entry<String, Date>>(time.entrySet());
    Collections.sort(timed, new Comparator<Entry<String, Date>>() {
      @Override
      public int compare(final Entry<String, Date> o1, final Entry<String, Date> o2) {
        Expression v1 = ExpressionParser.simpleParse(o1.getKey());
        Expression v2 = ExpressionParser.simpleParse(o2.getKey());
        return -v1.compareTo(v2);
      }
    });
    Expression expr = ExpressionParser.parse(version);
    for (Entry<String, Date> entry : timed) {
      String current = entry.getKey();
      if (expr.matches(current)) {
        return current;
      }
    }
    throw new IllegalArgumentException("No matches found for: " + name + "@" + version + ", in: ["
        + join(time.keySet(), ", ") + "]");
  }
}
