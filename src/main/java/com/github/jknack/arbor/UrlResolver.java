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

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class UrlResolver extends AbstractResolver<URI> {

  @Override
  public String getName() {
    return "url";
  }

  @Override
  protected ResolvedRevision<URI> doResolve(final ModuleId moduleId) throws IOException {
    try {
      URI uri = URI.create(moduleId.getRevision());
      if (uri.isAbsolute()) {
        String host = uri.getHost();
        if (host.equals("raw.github.com")) {
          String[] parts = StringUtils.split(uri.toString(), "/");
          return new ResolvedRevision<URI>(uri, new ModuleId(moduleId.getName(), parts[4]));
        } else {
          String path = removeExtension(FilenameUtils.getName(uri.toString()));
          // match things like http://host/[module]-[version]
          String[] parts = StringUtils.split(path, "-");
          if (parts.length == 2) {
            return new ResolvedRevision<URI>(uri, new ModuleId(moduleId.getName(), parts[1]));
          }
        }
        // fallback
        return new ResolvedRevision<URI>(uri, moduleId);
      }
    } catch (IllegalArgumentException ex) {
      // fail later
    }
    throw new UnresolvedDependencyException(moduleId);
  }

  @Override
  protected Module newModule(final DependencyContext context, final URI uri,
      final ModuleId moduleId) throws IOException {
    String filename = FilenameUtils.getName(uri.toString());
    List<String> location = new ArrayList<String>(Arrays.asList(StringUtils.split(uri.getHost(),
        ".")));
    Collections.reverse(location);
    File moduleHome = moduleId.getRevision().equals(uri.toString()) ? new File(
        context.getBaseDir(), join(location, File.separator)) : context.moduleHome(moduleId);
    moduleHome.mkdirs();
    File file = new File(moduleHome, filename);
    logger.debug("GET {}", uri);
    Get(uri).execute().saveContent(file);
    return new Module(moduleId, file.getParentFile(), filename);
  }

}
