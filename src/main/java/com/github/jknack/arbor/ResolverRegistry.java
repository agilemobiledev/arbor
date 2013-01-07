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

import java.util.HashMap;
import java.util.Map;

public class ResolverRegistry {

  private static final Map<String, DependencyResolver> resolvers =
      new HashMap<String, DependencyResolver>();

  public static DependencyResolver get(final String name) {
    return resolvers.get(name);
  }

  public static void register(final DependencyResolver resolver) {
    notNull(resolver, "The resolver is required.");
    resolvers.put(resolver.getName(), resolver);
  }
}
