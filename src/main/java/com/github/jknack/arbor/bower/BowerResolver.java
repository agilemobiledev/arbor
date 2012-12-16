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

import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.arbor.commonjs.PackageJSON;
import com.github.jknack.arbor.commonjs.PackageNotFoundException;
import com.github.jknack.arbor.commonjs.PackageResolver;
import com.github.jknack.arbor.version.Expression;
import com.github.jknack.arbor.version.ExpressionParser;

/**
 * Resolve dependencies using bower registry.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class BowerResolver extends PackageResolver {

  /**
   * The available bower packages.
   */
  protected final Map<String, String> packages = new HashMap<String, String>();

  /**
   * The Jackson Object Mapper.
   */
  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Creates a new {@link BowerResolver}.
   *
   * @param homeDir The local repository directory.
   * @param uri The bower URI registry.
   */
  public BowerResolver(final File homeDir, final URI uri) {
    super(new File(homeDir, "bower"), uri);
    loadPackages();
  }

  /**
   * Creates a new {@link BowerResolver}.
   *
   * @param homeDir The local repository directory.
   */
  public BowerResolver(final File homeDir) {
    this(homeDir, URI.create("http://bower.herokuapp.com/packages"));
  }

  /**
   * Load all the bower packages.
   */
  @SuppressWarnings("unchecked")
  private void loadPackages() {
    try {
      String uri = registry.substring(0, registry.length() - 1);
      logger.info("GET {}", uri);
      String json = Get(uri).execute().returnContent().asString();
      List<Map<String, String>> packageList = objectMapper.readValue(json, List.class);
      for (Map<String, String> packageEntry : packageList) {
        packages.put(packageEntry.get("name"), packageEntry.get("url"));
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to get bower packages", ex);
    }
  }

  @Override
  protected boolean canResolve(final String path) throws IOException {
    return packages.containsKey(StringUtils.split(path, "/")[0]);
  }

  @Override
  public String name() {
    return "bower";
  }

  /**
   * Resolve the module name to a git home.
   *
   * @param name The module's name.
   * @return The module's git home.
   */
  protected File gitHome(final String name) {
    return new File(moduleHome(name), "git");
  }

  /**
   * Clone a git repository.
   *
   * @param name The repository's name.
   * @return A git repository.
   * @throws IOException If the git repository cannot be cloned.
   */
  protected Git git(final String name) throws IOException {
    File moduleHome = gitHome(name);
    File gitDir = new File(moduleHome, ".git");
    if (!gitDir.exists()) {
      String url = packages.get(name);
      if (url == null) {
        throw new PackageNotFoundException(name);
      }
      try {
        logger.info("git clone {}", url);
        Git.cloneRepository()
            .setURI(url)
            .setDirectory(moduleHome)
            .call();
      } catch (Exception ex) {
        throw new IOException("git clone " + url, ex);
      }
    }
    return new Git(new FileRepository(gitDir));
  }

  @Override
  protected PackageJSON packageJSON(final DependencyDescriptor descriptor) throws IOException {
    String name = descriptor.getName();
    String version = descriptor.getVersion();
    File moduleHome = new File(moduleHome(name), version);
    File gitHome = gitHome(name);
    Git git = git(name);
    try {
      logger.info("git checkout {}", version);
      git.checkout().setName(version).call();
    } catch (RefNotFoundException ex) {
      try {
        logger.debug("branch not found: {}", version);
        logger.debug("git checkout master");
        git.checkout().setName("master").call();
      } catch (Exception e) {
        throw new IOException("git checkout master", ex);
      }
    } catch (Exception ex) {
      throw new IOException("git checkout " + version, ex);
    }
    FileUtils.copyDirectory(gitHome, moduleHome, new FileFilter() {
      @Override
      public boolean accept(final File file) {
        return !file.getPath().contains(".git");
      }
    });
    File packageJSONFile = new File(moduleHome, "component.json");
    if (!packageJSONFile.exists()) {
      packageJSONFile = new File(moduleHome, "package.json");
    }
    if (!packageJSONFile.exists()) {
      // This is a git repository without a package.json or component.json
      // report as un-resolved
      throw new HttpResponseException(HttpStatus.SC_NOT_FOUND, descriptor.getId());
    }
    return PackageJSON.from(packageJSONFile);
  }

  @Override
  protected void extract(final PackageJSON packageJSON, final File moduleHome) throws IOException {
    // do nothing
  }

  @Override
  protected String findLatest(final String path) throws IOException {
    Git git = git(path);
    try {
      List<Ref> tags = git.tagList().call();
      if (tags.size() > 0) {
        for (int i = tags.size() - 1; i >= 0; i--) {
          Ref tag = tags.get(i);
          String tagName = tagName(tag);
          if (ExpressionParser.valid(tagName)) {
            return tagName;
          }
        }
      } else {
        File gitHome = gitHome(path);
        File packageJSONFile = new File(gitHome, "component.json");
        if (!packageJSONFile.exists()) {
          packageJSONFile = new File(gitHome, "package.json");
        }
        return PackageJSON.from(packageJSONFile).getVersion();
      }
    } catch (GitAPIException ex) {
      throw new IOException("Unable to find latest version of: " + path, ex);
    }
    throw new IOException("Unable to find latest version of: " + path);
  }

  /**
   * Extract a tag's name.
   *
   * @param tag A git tag.
   * @return A tag's name.
   */
  private String tagName(final Ref tag) {
    String prefix = "refs/tags/";
    String tagName = tag.getName();
    if (tagName.startsWith(prefix)) {
      return tagName.substring(prefix.length());
    }
    return tagName;
  }

  @Override
  protected String resolveVersion(final String name, final String version) throws IOException {
    Git git = git(name);
    try {
      List<Ref> tags = git.tagList().call();
      Expression expr = ExpressionParser.parse(version);
      for (int i = tags.size() - 1; i >= 0; i--) {
        Ref tag = tags.get(i);
        String tagName = tagName(tag);
        if (expr.matches(tagName)) {
          return tagName;
        }
      }
    } catch (GitAPIException ex) {
      throw new IOException("Unable to resolve version: " + name + "@" + version, ex);
    }
    throw new IOException("Unable to resolve version: " + name + "@" + version);
  }
}
