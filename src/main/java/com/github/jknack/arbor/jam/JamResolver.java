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
package com.github.jknack.arbor.jam;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import com.github.jknack.arbor.commonjs.PackageEntry;
import com.github.jknack.arbor.commonjs.PackageJSON;
import com.github.jknack.arbor.commonjs.PackageResolver;
import com.github.jknack.arbor.version.Expression;

/**
 * The jamjs resolver.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class JamResolver extends PackageResolver {

  /**
   * Creates a new {@link JamResolver}.
   *
   * @param homeDir The home directory of the resolver.
   */
  public JamResolver(final File homeDir) {
    super(new File(homeDir, "jam"), URI.create("http://jamjs.org/repository"));
  }

  @Override
  protected PackageJSON packageJSON(final String path) throws IOException {
    String[] moduleInfo = moduleInfo(path);
    String name = moduleInfo[0];
    String version = moduleInfo[1];
    version = Expression.LATEST.equals(version) ? findLatest(name) : version;
    String uri = registry + name;
    PackageEntry entry = packageEntry(uri);
    Map<String, PackageJSON> versions = entry.getVersions();
    PackageJSON packageJSON = versions.get(version);
    if (packageJSON == null) {
      throw new HttpResponseException(HttpStatus.SC_NOT_FOUND, registry + name);
    }
    logger.info("GET {}", uri);
    return packageJSON;
  }

  @Override
  protected URI tarball(final PackageJSON packageJSON) {
    String tarball = registry + packageJSON.getName() + "/" + packageJSON.getName() + "-"
        + packageJSON.getVersion() + ".tar.gz";
    return URI.create(tarball);
  }

  @Override
  public String name() {
    return "jam";
  }
}
