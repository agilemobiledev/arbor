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
package com.github.jknack.semver;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Deal with range expressions: tilde and 'x'.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
abstract class Range extends BaseExpression implements PrefixOperator {

  /**
   * The left side expression.
   */
  protected Version left;

  /**
   * The right side expression.
   */
  protected Version right;

  /**
   * Creates a new range.
   *
   * @param expr The left side expression.
   */
  public Range(final Semver expr) {
    left = notNull((Version) expr, "The left side expression is required.");
  }

  /**
   * Default constructor.
   */
  public Range() {
  }

  /**
   * Creates a tilde range expression.
   *
   * @return A tilde range expression.
   */
  public static Range tilde() {
    return new Range() {
      @Override
      public String text() {
        return "~" + left;
      }

      @Override
      public Type type() {
        return Type.TILDE;
      }
    };
  }

  /**
   * Creates an 'x' range expression.
   *
   * @param expr The candidate expression.
   * @return An 'x' range expression.
   */
  public static Range x(final Semver expr) {
    return new Range(expr) {
      @Override
      public String text() {
        return left.toString();
      }

      @Override
      public Type type() {
        return Type.X_RANGE;
      }
    };
  }

  /**
   * Creates a new range expression.
   *
   * @param left The left side expression.
   * @param right The right side expression.
   * @return A new range expression.
   */
  public static Range range(final Version left, final Version right) {
    Range range = new Range() {
      @Override
      public String text() {
        return left + " - " + right;
      }

      @Override
      public Type type() {
        return Type.RANGE;
      }
    };
    range.setLeft(left);
    range.setRight(right);
    return range;
  }

  @Override
  public boolean matches(final Semver expr) {
    return compareTo(expr) == 0;
  }

  @Override
  public int compareTo(final Semver expr) {
    boolean left = expr.compareTo(this.left) >= 0;
    boolean right = this.right == null
        ? expr.compareTo(this.left.nextMajor()) < 0
        : expr.compareTo(this.right) <= 0;
    return left && right ? 0 : left ? -1 : 1;
  }

  @Override
  public void setExpression(final Semver expression) {
    setLeft((Version) expression);
  }

  /**
   * Set the left side expression.
   *
   * @param left The left side expression.
   */
  public void setLeft(final Version left) {
    this.left = left;
  }

  /**
   * Set the right side expression.
   *
   * @param right The right side expression.
   */
  public void setRight(final Version right) {
    this.right = right;
  }

}
