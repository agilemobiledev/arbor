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

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Logical OR.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
class OrExpression extends BaseExpression {

  /**
   * The left side expression.
   */
  private Expression left;

  /**
   * The right side expression.
   */
  private Expression right;

  /**
   * Creates a new expression.
   * @param left The left side expression.
   * @param right The right side expression.
   */
  public OrExpression(final Expression left, final Expression right) {
    this.left = notNull(left, "The left side expression is required.");
    this.right = notNull(right, "The right side expression is required.");
  }

  @Override
  public boolean matches(final Expression expr) {
    boolean left = this.left.matches(expr);
    boolean right = this.right.matches(expr);
    return left || right;
  }

  @Override
  public int compareTo(final Expression expr) {
    boolean left = this.left.compareTo(expr) >= 0;
    boolean right = expr.compareTo(this.right) <= 0;
    return left && right ? 0 : left ? -1 : 1;
  }

  @Override
  public String toString() {
    return left + " || " + right;
  }
}
