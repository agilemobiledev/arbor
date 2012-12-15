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

import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.github.jknack.arbor.commonjs.PackageJSON;
import com.github.jknack.arbor.commonjs.PackageResolver;

/**
 * A NPM dependency resolver.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class NpmResolver extends PackageResolver {

  /**
   * The npm registry URI.
   */
  private static final String REGISTRY = "http://registry.npmjs.org/";

  /**
   * Creates a new {@link NpmResolver}.
   *
   * @param homeDir The local repository location. Required.
   * @param uri The URI of the npm registry.
   */
  public NpmResolver(final File homeDir, final URI uri) {
    super(new File(homeDir, "node"), uri);
  }

  /**
   * Creates a new {@link NpmResolver}.
   *
   * @param homeDir The local repository location. Required.
   */
  public NpmResolver(final File homeDir) {
    this(homeDir, URI.create(REGISTRY));
  }

  @Override
  protected PackageJSON packageJSON(final String path) throws IOException {
    String uri = registry + path;
    logger.info("GET {}", uri);
    String json = Get(uri)
        .execute().returnContent().asString();
    PackageJSON packageJSON = PackageJSON.from(json);
    return packageJSON;
  }

  @Override
  public String name() {
    return "npm";
  }

}
