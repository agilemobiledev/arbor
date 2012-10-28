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

/**
 * A version expression.
 * <ul>
 * <li><code>version</code> Must match version exactly</li>
 * <li><code>=version</code> Same as just version</li>
 * <li><code>>version</code> Must be greater than version</li>
 * <li><code>>=version</code> etc</li>
 * <li><code>&lt;version</code></li>
 * <li><code><=version</code></li>
 * <li><code>~version</code> See 'Tilde Version Ranges' below</li>
 * <li><code>1.2.x</code> See 'X Version Ranges' below</li>
 * <li><code>http://...</code> See 'URLs as Dependencies' below</li>
 * <li><code>*</code>Matches any version</li>
 * <li><code>""</code> (just an empty string) Same as *</li>
 * <li><code>version1 - version2</code> Same as >=version1 <=version2.</li>
 * <li><code>range1 || range2</code> Passes if either range1 or range2 are
 * satisfied.</li>
 * <li><code>git...</code>See 'Git URLs as Dependencies' below</li>
 * </ul>
 *
 * @author edgar.espina
 * @since 0.0.1
 */
public interface Expression extends Comparable<Expression> {

  /**
   * Match any version.
   */
  Expression ANY = new BaseExpression() {
    @Override
    public boolean matches(final Expression expr) {
      return true;
    }

    @Override
    public int compareTo(final Expression expr) {
      return 0;
    }

    @Override
    public String toString() {
      return "*";
    }
  };

  /**
   * True if the given expression matches.
   *
   * @param expr The candidate expression.
   * @return True if the given expression matches.
   */
  boolean matches(Expression expr);

  /**
   * True if the given expression matches.
   *
   * @param expr The candidate expression.
   * @return True if the given expression matches.
   */
  boolean matches(String expr);

  @Override
  int compareTo(Expression expr);

  @Override
  String toString();
}
