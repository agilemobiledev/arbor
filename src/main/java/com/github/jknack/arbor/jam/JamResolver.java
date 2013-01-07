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

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.github.jknack.arbor.AbstractResolver;
import com.github.jknack.arbor.DependencyContext;
import com.github.jknack.arbor.JsonParser;
import com.github.jknack.arbor.Module;
import com.github.jknack.arbor.ModuleId;
import com.github.jknack.arbor.UnresolvedDependencyException;
import com.github.jknack.arbor.io.FileExtractor;

public class JamResolver extends AbstractResolver<JamPackage> {

  private URI uri;

  public JamResolver(final URI uri) {
    this.uri = notNull(uri, "The uri is required.");
  }

  public JamResolver() {
    this(URI.create("http://jamjs.org/repository/"));
  }

  @Override
  public String getName() {
    return "jam";
  }

  @Override
  protected Module newModule(final DependencyContext context, final JamPackage jamPackage,
      final ModuleId moduleId)
      throws IOException {
    // extract and create module
    File moduleHome = context.moduleHome(moduleId);
    extract(moduleId, moduleHome);
    Module module = new Module(moduleId, moduleHome, jamPackage.resolveMain(moduleHome));

    // mark as resolved
    context.put(moduleId, module);
    // find out dependencies
    for (Entry<String, String> entry : jamPackage.getDependencies().entrySet()) {
      Module dependency = resolve(context, new ModuleId(entry.getKey(), entry.getValue()));
      if (dependency != null) {
        module.add(dependency);
      }
    }
    return module;
  }

  @Override
  protected ResolvedRevision<JamPackage> doResolve(final ModuleId moduleId)
      throws IOException {
    String moduleURI = uri + moduleId.getName();
    logger.debug("GET {}", moduleURI);
    JamEntry jamEntry = Get(moduleURI).execute().handleResponse(new ResponseHandler<JamEntry>() {
      @Override
      public JamEntry handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
          throw new UnresolvedDependencyException(moduleId.toString(),
              new HttpResponseException(
                  statusLine.getStatusCode(), statusLine.getReasonPhrase()));
        }
        String json = EntityUtils.toString(response.getEntity());
        JamEntry jamEntry = JsonParser.read(json, JamEntry.class);
        return jamEntry;
      }
    });
    String revisionId = jamEntry.resolveVersion(moduleId.getRevision());
    JamPackage jamPackage = jamEntry.getVersion(revisionId);
    return new ResolvedRevision<JamPackage>(jamPackage,
        new ModuleId(moduleId.getName(), revisionId));
  }

  private void extract(final ModuleId moduleId, final File moduleHome) throws IOException {
    String tarName = moduleId.getName() + "-" + moduleId.getRevision() + ".tar.gz";
    String tarballURI = String.format("%s%s/%s", uri, moduleId.getName(), tarName);
    logger.debug("GET {}", tarballURI);
    File tarball = new File(moduleHome, tarName);
    Get(tarballURI).execute().saveContent(tarball);
    FileExtractor.extract(tarball, moduleHome);
    tarball.delete();
  }
}
