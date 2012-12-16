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

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.errors.ActionException;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

/**
 * Parse version expression.
 *
 * @author edgar.espina
 * @since 0.0.1
 */
public class ExpressionParser extends BaseParser<Expression> {

  /**
   * Parse a version expression.
   *
   * @param version A version expression.
   * @return An expression.
   */
  public static Expression parse(final String version) {
    if (version.length() == 0) {
      return Expression.ANY;
    }
    ExpressionParser parser = Parboiled.createParser(ExpressionParser.class);

    ParseRunner<Expression> runner =
        new ReportingParseRunner<Expression>(parser.expression());

    ParsingResult<Expression> result = runner.run(version);

    if (result.hasErrors()) {
      ParseError error = result.parseErrors.get(0);
      throw new IllegalArgumentException(ErrorUtils.printParseError(error));
    }
    return result.resultValue;
  }

  /**
   * Parse a version expression.
   *
   * @param version A version expression.
   * @return An expression.
   */
  public static Expression simpleParse(final String version) {
    if (version.length() == 0) {
      return Expression.ANY;
    }
    ExpressionParser parser = Parboiled.createParser(ExpressionParser.class);

    ParseRunner<Expression> runner =
        new ReportingParseRunner<Expression>(parser.version());

    ParsingResult<Expression> result = runner.run(version);

    if (result.hasErrors()) {
      ParseError error = result.parseErrors.get(0);
      throw new IllegalArgumentException(ErrorUtils.printParseError(error));
    }
    return result.resultValue;
  }

  /**
   * True if the expression version matches a simple version expression.
   *
   * @param version The expression version.
   * @return True if the expression version matches a simple version expression.
   */
  public static boolean valid(final String version) {
    try {
      simpleParse(version);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  /**
   * Deal with version expression.
   *
   * @return A version expression rule.
   */
  Rule expression() {
    return Sequence(factor(), ZeroOrMore(or()), EOI);
  }

  /**
   * Matches ranges or version.
   *
   * @return A rule.
   */
  Rule factor() {
    return FirstOf(
        range(),
        term());
  }

  /**
   * Matches version, uri or '*'.
   *
   * @return A rule.
   */
  Rule term() {
    return FirstOf(
        version(),
        uri(),
        any());
  }

  /**
   * Matches ranges of versions.
   *
   * @return A rule.
   */
  Rule range() {
    return Sequence(term(), ws(), Optional("-"), ws(), term(),
        new Action<Expression>() {
          @Override
          public boolean run(final Context<Expression> context) {
            Expression right = pop();
            Expression left = pop();
            if (left instanceof Version && right instanceof Version) {
              return push(Range.range((Version) left, (Version) right));
            }
            return push(new AndExpression(left, right));
          }
        });
  }

  /**
   * Matches a version or another.
   *
   * @return A rule.
   */
  Rule or() {
    return Sequence(ws(), "||", ws(), factor(),
        new Action<Expression>() {
          @Override
          public boolean run(final Context<Expression> context) {
            Expression right = pop();
            Expression left = pop();
            return push(new OrExpression(left, right));
          }
        });
  }

  /**
   * Matches '*'.
   *
   * @return A rule.
   */
  Rule any() {
    return Sequence('*', push(Expression.ANY));
  }

  /**
   * Matches URIs.
   *
   * @return A rule.
   */
  Rule uri() {
    final Var<Integer> idx = new Var<Integer>();
    return Sequence(idx.set(currentIndex()), protocol(), "://",
        OneOrMore(uripart()), new Action<UrlExpression>() {
          @Override
          public boolean run(final Context<UrlExpression> context) {
            String text =
                context.getInputBuffer().extract(idx.get(), currentIndex());
            return push(new UrlExpression(text));
          }
        });
  }

  /**
   * Matches HTTP protocols.
   *
   * @return A rule.
   */
  Rule protocol() {
    return FirstOf("https", "http", "git+shh", "git+https", "git+http", "git");
  }

  /**
   * Matches an URI part.
   *
   * @return A rule.
   */
  Rule uripart() {
    return FirstOf(
        '-',
        '+',
        '&',
        '@',
        '/',
        '%',
        '#',
        '?',
        '=',
        '~',
        '_',
        '|',
        '!',
        ':',
        ',',
        '.',
        ';',
        CharRange('a', 'z'),
        CharRange('A', 'Z'),
        CharRange('0', '9'));
  }

  /**
   * Matches a version.
   *
   * @return A rule.
   */
  Rule version() {
    final Var<Version> version = new Var<Version>(new Version());
    final Var<PrefixOperator> operator = new Var<PrefixOperator>();
    final Var<Integer> idx = new Var<Integer>();
    return Sequence(
        Optional(operator(operator)),
        ws(),
        new Action<Expression>() {
          @Override
          public boolean run(final Context<Expression> context) {
            idx.set(context.getCurrentIndex());
            return true;
          }
        },
        Optional(Ch('v')), major(version),
        Optional(minor(version)), new Action<Expression>() {
          @Override
          public boolean run(final Context<Expression> context) {
            String text = context.getInputBuffer().extract(idx.get(),
                context.getCurrentIndex());
            Version v = version.get();
            v.setText(text);
            Expression expr = v;
            if (text.contains("x")) {
              expr = Range.x(expr);
            }
            PrefixOperator op = operator.get();
            if (op != null) {
              op.setExpression(expr);
              expr = op;
            }
            return push(expr);
          }
        });
  }

  /**
   * Matches whitespaces.
   *
   * @return A rule.
   */
  Rule ws() {
    return ZeroOrMore(AnyOf(" \t"));
  }

  /**
   * Matches a prefix operator.
   *
   * @param expr An expression.
   * @return A rule.
   */
  Rule operator(final Var<PrefixOperator> expr) {
    return FirstOf(
        Sequence("=", expr.set(RelationalOp.eq())),
        Sequence(">=", expr.set(RelationalOp.gtEq())),
        Sequence(">", expr.set(RelationalOp.gt())),
        Sequence("<=", expr.set(RelationalOp.ltEq())),
        Sequence("<", expr.set(RelationalOp.lt())),
        Sequence("~", expr.set(Range.tilde())));
  }

  /**
   * Matches a minor qualifier.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule minor(final Var<Version> version) {
    return Sequence(dot(), number(), setMinor(version),
        Optional(incremental(version)));
  }

  /**
   * Matches an incremental qualifier.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule incremental(final Var<Version> version) {
    return Sequence(dot(), number(), setIncremental(version),
        Optional(tail(version)));
  }

  /**
   * Matches a build or tag number.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule tail(final Var<Version> version) {
    return FirstOf(
        build(version),
        tag(version));
  }

  /**
   * Matches a tag.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule tag(final Var<Version> version) {
    return Sequence(tag(), setTag(version));
  }

  /**
   * Matches a tag.
   *
   * @return A rule.
   */
  Rule tag() {
    return OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '.',
        '-'));
  }

  /**
   * Matches a build qualifier.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule build(final Var<Version> version) {
    return Sequence('-', number(), setBuild(version), Optional(tag(version)));
  }

  /**
   * Matches a major qualifier.
   *
   * @param version A version.
   * @return A rule.
   */
  Rule major(final Var<Version> version) {
    return Sequence(number(), setMajor(version));
  }

  /**
   * Matches a number or 'x'.
   *
   * @return A rule.
   */
  @MemoMismatches
  Rule number() {
    return FirstOf(OneOrMore(CharRange('0', '9')), 'x', 'X');
  }

  /**
   * Matches a simple name.
   *
   * @return A rule.
   */
  Rule name() {
    return OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z')));
  }

  /**
   * Matches a '.'.
   *
   * @return A rule.
   */
  @MemoMismatches
  Rule dot() {
    return Ch('.');
  }

  /**
   * Convert the given text to a number. If text is 'x', zero will be returned.
   *
   * @param text The text.
   * @return A number.
   */
  int toNumber(final String text) {
    if ("x".equalsIgnoreCase(text)) {
      return 0;
    }
    return Integer.parseInt(text);
  }

  /**
   * Set the major qualifier.
   *
   * @param var A version.
   * @return True.
   */
  boolean setMajor(final Var<Version> var) {
    String text = match();
    if ("x".equals(text)) {
      throw new ActionException("[x] is not allowed for major");
    }
    var.get().setMajor(toNumber(text));
    return true;
  }

  /**
   * Set the minor qualifier.
   *
   * @param version A version.
   * @return True.
   */
  boolean setMinor(final Var<Version> version) {
    version.get().setMinor(toNumber(match()));
    return true;
  }

  /**
   * Set the incremental qualifier.
   *
   * @param version A version.
   * @return True.
   */
  boolean setIncremental(final Var<Version> version) {
    version.get().setIncremental(toNumber(match()));
    return true;
  }

  /**
   * Set the build qualifier.
   *
   * @param version A version.
   * @return True.
   */
  boolean setBuild(final Var<Version> version) {
    version.get().setBuild(toNumber(match()));
    return true;
  }

  /**
   * Set the tag qualifier.
   *
   * @param version A version.
   * @return True.
   */
  boolean setTag(final Var<Version> version) {
    version.get().setTag(match());
    return true;
  }

}
