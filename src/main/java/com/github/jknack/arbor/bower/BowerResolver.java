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
package com.github.jknack.arbor.bower;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.github.jknack.arbor.AbstractResolver;
import com.github.jknack.arbor.DependencyContext;
import com.github.jknack.arbor.JsonParser;
import com.github.jknack.arbor.Module;
import com.github.jknack.arbor.ModuleId;
import com.github.jknack.arbor.PackageJson;
import com.github.jknack.arbor.UnresolvedDependencyException;
import com.github.jknack.arbor.github.GitHub;
import com.github.jknack.arbor.github.GitHubRepository;

public class BowerResolver extends AbstractResolver<GitHubRepository> {

  private GitHub gitHub = new GitHub();

  @Override
  public String getName() {
    return "bower";
  }

  @Override
  protected Module newModule(final DependencyContext context, final GitHubRepository repository,
      final ModuleId moduleId) throws IOException {
    String descriptorName = "component.json";
    String descriptor = repository.get(moduleId.getRevision(), descriptorName);
    if (isEmpty(descriptor)) {
      // fallback to package.json
      descriptorName = "package.json";
      descriptor = repository.get(moduleId.getRevision(), descriptorName);
    }
    if (isEmpty(descriptor)) {
      throw new UnresolvedDependencyException(moduleId);
    }
    PackageJson packageJson = JsonParser.read(descriptor, PackageJson.class);
    File moduleHome = context.moduleHome(moduleId);
    String mainJs = packageJson.resolveMain(moduleHome);
    String mainJsContent = repository.get(moduleId.getRevision(), mainJs);
    if (isEmpty(mainJsContent)) {
      throw new UnresolvedDependencyException(moduleId);
    }
    // save descriptor and content
    FileUtils.write(new File(moduleHome, descriptorName), descriptor);
    FileUtils.write(new File(moduleHome, mainJs), mainJsContent);

    Module module = new Module(moduleId, moduleHome, mainJs);
    // find out dependencies
    for (Entry<String, String> entry : packageJson.getDependencies().entrySet()) {
      Module dependency = resolve(context, new ModuleId(entry.getKey(), entry.getValue()));
      module.add(dependency);
    }
    return module;
  }

  @Override
  protected ResolvedRevision<GitHubRepository> doResolve(final ModuleId moduleId)
      throws IOException {
    Map<String, String> packages = getPackages();
    String url = packages.get(moduleId.getName());
    if (isEmpty(url)) {
      throw new UnresolvedDependencyException(moduleId);
    }
    GitHubRepository repository = gitHub.createRepository(URI.create(url));
    String revisionId = repository.resolveVersion(moduleId.getRevision());
    return new ResolvedRevision<GitHubRepository>(repository, new ModuleId(moduleId.getName(),
        revisionId));
  }

  /**
   * Load all the bower packages.
   *
   * @return
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> getPackages() throws IOException {
    String uri = "http://bower.herokuapp.com/packages";
    logger.debug("GET {}", uri);
    String json = Get(uri).execute().returnContent().asString();
    List<Map<String, String>> packageList = JsonParser.read(json, List.class);
    Map<String, String> packages = new HashMap<String, String>();
    for (Map<String, String> packageEntry : packageList) {
      packages.put(packageEntry.get("name"), packageEntry.get("url"));
    }
    return packages;
  }
}
