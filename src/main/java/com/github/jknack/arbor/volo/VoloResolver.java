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
package com.github.jknack.arbor.volo;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.arbor.AbstractResolver;
import com.github.jknack.arbor.DependencyContext;
import com.github.jknack.arbor.JsonParser;
import com.github.jknack.arbor.Module;
import com.github.jknack.arbor.ModuleId;
import com.github.jknack.arbor.PackageJson;
import com.github.jknack.arbor.UnresolvedDependencyException;
import com.github.jknack.arbor.github.GitHub;
import com.github.jknack.arbor.github.GitHubRepository;
import com.github.jknack.arbor.io.FileExtractor;
import com.github.jknack.semver.Semver;

public class VoloResolver extends AbstractResolver<GitHubRepository> {

  private GitHub gitHub = new GitHub();

  private GitHubRepository voloRepo = new GitHubRepository("volojs", "repos");

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public String getName() {
    return "volo";
  }

  @Override
  protected Module newModule(final DependencyContext context, final GitHubRepository repository,
      final ModuleId moduleId) throws IOException {
    logger.debug("Using github repo \"{}\" for \"{}\"...", repository, moduleId.getName());
    // 1st try
    VoloPackage volo = volo(repository, moduleId.getRevision(), "package.json");
    if (volo == null) {
      // 2nd try
      volo = volo(voloRepo, "master", repository.getId() + "/package.json");
    }
    File moduleHome = context.moduleHome(moduleId);
    if (volo == null || isEmpty(volo.getUrl())) {
      // no luck! unzip it!
      return extract(repository, moduleId, moduleHome);
    }
    String voloUrl = volo.getUrl();
    voloUrl = voloUrl.replace("{version}", moduleId.getRevision());
    // fetch it!
    Module module = fetch(moduleId, moduleHome, voloUrl);

    // mark as resolved
    context.put(moduleId, module);

    // find out dependencies
    for (Entry<String, String> entry : volo.getDependencies().entrySet()) {
      String depRevId = entry.getValue();
      if (depRevId.startsWith("github:")) {
        depRevId = depRevId.substring("github:".length());
      }
      String[] depDescritor = depRevId.split("/");
      if (depDescritor.length == 3) {
        depRevId = depDescritor[depDescritor.length - 1];
      } else {
        depRevId = Semver.LATEST.text();
      }
      Module dependency = resolve(context, new ModuleId(entry.getKey(), depRevId));
      if (dependency != null) {
        module.add(dependency);
      }
    }
    return module;
  }

  @Override
  protected ResolvedRevision<GitHubRepository> doResolve(final ModuleId moduleId)
      throws IOException {
    GitHubRepository repository = gitHub.findRepository(moduleId.getName());
    if (repository == null) {
      throw new UnresolvedDependencyException(moduleId);
    }
    String revisionId = repository.resolveVersion(moduleId.getRevision());
    return new ResolvedRevision<GitHubRepository>(repository, new ModuleId(moduleId.getName(),
        revisionId));
  }

  private Module extract(final GitHubRepository repository, final ModuleId moduleId,
      final File moduleHome) throws IOException {
    File tarball = repository.tarball(moduleId.getRevision());
    FileExtractor.extract(tarball, moduleHome);
    File packageJsonFile = new File(moduleHome, "package.json");
    final String main;
    if (packageJsonFile.exists()) {
      PackageJson packageJson = JsonParser.read(FileUtils.readFileToString(packageJsonFile),
          PackageJson.class);
      main = packageJson.resolveMain(moduleHome);
    } else {
      main = repository.getName() + ".js";
    }
    Module module = new Module(moduleId, moduleHome, main);
    return module;
  }

  private VoloPackage volo(final GitHubRepository repository, final String revisionId,
      final String path)
      throws IOException {
    String packageJsonStr = repository.get(revisionId, path);
    if (isEmpty(packageJsonStr)) {
      return null;
    }
    VoloPackage volo = JsonParser.read(packageJsonStr, VoloPackage.class);
    if (isEmpty(volo.getUrl())) {
      return null;
    }
    return volo;
  }

  private Module fetch(final ModuleId moduleId, final File moduleHome, final String voloUrl)
      throws IOException {
    String main = FilenameUtils.getName(voloUrl);
    File localFile = new File(moduleHome, main);
    logger.debug("GET {}", voloUrl);
    Get(voloUrl).execute().saveContent(localFile);
    return new Module(moduleId, moduleHome, main);
  }
}
