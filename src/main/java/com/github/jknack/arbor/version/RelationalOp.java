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
package com.github.jknack.arbor.version;

/**
 * Relational operators.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
abstract class RelationalOp extends BaseExpression implements
    PrefixOperator {

  /**
   * Equals.
   *
   * @author edgar.espina
   */
  public static class EqualsTo extends RelationalOp {
    @Override
    public boolean matches(final Expression expr) {
      return compareTo(expr) == 0;
    }

    @Override
    public String toString() {
      return "=" + expression;
    }
  }

  /**
   * Less Than.
   *
   * @author edgar.espina
   */
  public static class LessThan extends RelationalOp {
    @Override
    public boolean matches(final Expression expr) {
      return compareTo(expr) < 0;
    }

    @Override
    public String toString() {
      return "<" + expression;
    }
  }

  /**
   * Less than or equals to.
   *
   * @author edgar.espina
   */
  public static class LessThanEqualsTo extends RelationalOp {
    @Override
    public boolean matches(final Expression expr) {
      return compareTo(expr) <= 0;
    }

    @Override
    public String toString() {
      return "<=" + expression;
    }
  }

  /**
   * Greater than.
   *
   * @author edgar.espina
   */
  public static class GreaterThan extends RelationalOp {
    @Override
    public boolean matches(final Expression expr) {
      return compareTo(expr) > 0;
    }

    @Override
    public String toString() {
      return ">" + expression;
    }
  }

  /**
   * Greater than or equals to.
   *
   * @author edgar.espina
   */
  public static class GreatherThanEqualsTo extends RelationalOp {
    @Override
    public boolean matches(final Expression expr) {
      return compareTo(expr) >= 0;
    }

    @Override
    public String toString() {
      return ">=" + expression;
    }
  }

  /**
   * The expression.
   */
  protected Expression expression;

  @Override
  public void setExpression(final Expression expr) {
    expression = expr;
  }

  @Override
  public int compareTo(final Expression expr) {
    return expr.compareTo(expression);
  }

  /**
   * Creates a new less than operator.
   *
   * @return A new less than operator.
   */
  public static RelationalOp lt() {
    return new LessThan();
  }

  /**
   * Creates a new less than or equals to operator.
   *
   * @return A new less than or equals to operator.
   */
  public static RelationalOp ltEq() {
    return new LessThanEqualsTo();
  }

  /**
   * Creates a new greater than operator.
   *
   * @return A new greater than operator.
   */
  public static RelationalOp gt() {
    return new GreaterThan();
  }

  /**
   * Creates a new greater than or equals to operator.
   *
   * @return A new greater than or equals to operator.
   */
  public static RelationalOp gtEq() {
    return new GreatherThanEqualsTo();
  }

  /**
   * Creates a new equals to operator.
   *
   * @return A new equals to operator.
   */
  public static RelationalOp eq() {
    return new EqualsTo();
  }
}
