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

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.client.fluent.Request.Get;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.arbor.Dependency;
import com.github.jknack.arbor.DependencyResolutionException;
import com.github.jknack.arbor.DependencyResolver;
import com.github.jknack.arbor.commonjs.PackageJSON;
import com.github.jknack.arbor.io.Tarball;
import com.github.jknack.arbor.version.Expression;
import com.github.jknack.arbor.version.ExpressionParser;

/**
 * A NPM dependency resolver.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class NpmResolver extends DependencyResolver {

  /**
   * The jackson object mapper.
   */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Don't fail on missing properties.
   */
  static {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * The npm registry URI.
   */
  private static final String REGISTRY = "http://registry.npmjs.org/";

  /**
   * The URI of the npm registry.
   */
  private String registry;

  /**
   * Creates a new {@link NpmResolver}.
   *
   * @param homeDir The local repository location. Required.
   * @param uri The URI of the npm registry.
   */
  public NpmResolver(final File homeDir, final URI uri) {
    super(new File(homeDir, "node_modules"));
    registry = normalize(uri);
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
  protected boolean canResolve(final String path) throws IOException {
    String[] parts = StringUtils.split(path, "/");
    return parts.length == 2;
  }

  /**
   * Resolve the given version expression to a real version number.
   *
   * @param name The module's name.
   * @param version A module version expression.
   * @return A real version expression.
   * @throws IOException If the expression cannot be resolve it.
   */
  protected String resolveVersion(final String name, final String version) throws IOException {
    String uri = registry + name;

    String json = Get(uri).execute().returnContent().asString();

    NpmEntry entry = objectMapper.readValue(json, NpmEntry.class);
    return entry.resolveVersion(version);
  }

  /**
   * Find the latest version number of the given module.
   *
   * @param name The module's name.
   * @return The latest version number.
   * @throws IOException If latest version cannot be resolve it.
   */
  protected String findLatest(final String name) throws IOException {
    String uri = registry + name + "/latest";

    String json = Get(uri).execute().returnContent().asString();

    return PackageJSON.from(json).getVersion();
  }

  @Override
  protected Dependency doResolve(final String path) throws IOException {
    Set<String> dependencySet = new HashSet<String>();
    LinkedList<String> dependencyPath = new LinkedList<String>();
    Set<File> moduleDirs = new HashSet<File>();
    try {
      doResolve(path, moduleHome(moduleInfo(path)), dependencySet, dependencyPath, moduleDirs);
    } catch (Exception ex) {
      deleteDirectory(moduleDirs);
      throw new DependencyResolutionException(dependencyPath, ex);
    }
    return resolveLocal(path);
  }

  /**
   * Delete any previously created directory every time an error occurs.
   *
   * @param moduleDirs The directories to delete.
   * @throws IOException If a directory cannot be deleted.
   */
  private void deleteDirectory(final Set<File> moduleDirs) throws IOException {
    for (File moduleDir : moduleDirs) {
      FileUtils.deleteDirectory(moduleDir);
      File moduleRoot = moduleDir.getParentFile();
      File[] children = moduleRoot.listFiles();
      boolean isEmpty = children == null || children.length == 0;
      if (isEmpty) {
        // delete empty dir
        moduleRoot.delete();
      }
    }
  }

  /**
   * Recursively resolve a dependency graph.
   *
   * @param path The dependency to resolve.
   * @param moduleRoot The module root directory.
   * @param dependencySet The already processed dependency set.
   * @param dependencyPath The current dependency path. useful for debugging.
   * @param moduleDirs Modules directory need to be appended here. Useful in case of errors.
   * @throws IOException If a dependency cannot be resolved.
   */
  protected void doResolve(final String path, final File moduleRoot,
      final Set<String> dependencySet, final LinkedList<String> dependencyPath,
      final Set<File> moduleDirs)
      throws IOException {
    if (!dependencySet.add(path)) {
      return;
    }
    String[] moduleInfo = moduleInfo(path);
    dependencyPath.add(moduleInfo[0] + "@" + moduleInfo[1]);
    String packageName = registry + moduleInfo[0];
    logger.info("GET {}", packageName);
    String json = Get(registry + path)
        .execute().returnContent().asString();
    logger.info("200 {}", packageName);
    PackageJSON packageJSON = PackageJSON.from(json);
    File moduleHome = new File(moduleRoot, packageJSON.getVersion());
    moduleHome.mkdirs();
    moduleDirs.add(moduleHome);

    // get tarball
    URI tarballURI = packageJSON.getTarball();
    logger.info("GET {}", tarballURI);
    String tarName = FilenameUtils.getName(tarballURI.getPath());
    File tarball = new File(moduleHome, tarName);
    Get(tarballURI).execute().saveContent(tarball);

    // untar
    final TarGZipUnArchiver ua = Tarball.newTarGZipUnArchiver(tarball);
    ua.setDestDirectory(moduleHome);
    ua.extract();
    logger.info("200 {}", tarballURI);
    tarball.delete();

    // dependencies
    Set<Entry<String, String>> dependencies = packageJSON.getDependencies().entrySet();
    for (Entry<String, String> dependency : dependencies) {
      String name = dependency.getKey();
      String expr = dependency.getValue();
      String version = resolveVersion(name, expr);
      String depPath = name + "/" + version;
      Dependency localDep = resolveLocal(depPath);
      if (!localDep.exists()) {
        doResolve(depPath, moduleHome(moduleInfo(depPath)), dependencySet, dependencyPath,
            moduleDirs);
      }
    }
    dependencyPath.removeLast();
  }

  /**
   * Build a module home from a module descriptor.
   *
   * @param moduleInfo The module descriptor.
   * @return A module home directory.
   */
  protected File moduleHome(final String[] moduleInfo) {
    return new File(homeDir, moduleInfo[0]);
  }

  /**
   * List all the modules available in the local repository. If there is more than one version per
   * module, they are ordered by most recently created.
   *
   * @return All the modules available in the local repository. If there is more than one version
   *         per
   *         module, they are ordered by most recently created.
   */
  private Map<String, List<Expression>> nodeModules() {
    Map<String, List<Expression>> modules = new HashMap<String, List<Expression>>();
    File[] moduleDirs = homeDir.listFiles();
    for (File moduleDir : moduleDirs) {
      if (moduleDir.isDirectory()) {
        List<Expression> versions = new ArrayList<Expression>();
        File[] versionDirs = moduleDir.listFiles();
        for (File versionDir : versionDirs) {
          if (versionDir.isDirectory()) {
            versions.add(ExpressionParser.simpleParse(versionDir.getName()));
          }
          Collections.sort(versions, new Comparator<Expression>() {
            @Override
            public int compare(final Expression o1, final Expression o2) {
              return -o1.compareTo(o2);
            }
          });
          modules.put(moduleDir.getName(), versions);
        }
      }
    }
    return modules;
  }

  @Override
  protected Dependency resolveLocal(final String path) throws IOException {
    String[] moduleInfo = moduleInfo(path);
    if (Expression.LATEST.equals(moduleInfo[1])) {
      moduleInfo[1] = findLatest(moduleInfo[0]);
    }
    File moduleHome = new File(moduleHome(moduleInfo), moduleInfo[1]);
    File packageJSONFile = new File(moduleHome, "package.json");
    if (packageJSONFile.exists()) {
      PackageJSON packageJSON = PackageJSON.from(packageJSONFile);
      Dependency dependency = packageJSON.resolve(homeDir, nodeModules());
      return dependency;
    } else {
      String name = moduleInfo[0];
      String version = moduleInfo[1];
      File location = new File(moduleHome, name + ".js");
      return new Dependency(name, version, location);
    }
  }

  /**
   * Creates a module descriptor from the given path.
   *
   * @param path A dependency path.
   * @return a module descriptor from the given path.
   */
  protected String[] moduleInfo(final String path) {
    return StringUtils.split(path, "/");
  }

  /**
   * Make sure the URI has a '/' at the end.
   *
   * @param uri The candidate uri.
   * @return A new URI with a '/' at the end.
   */
  private static String normalize(final URI uri) {
    notNull(uri, "The uri is required.");
    String result = uri.toString();
    if (!result.endsWith("/")) {
      result += "/";
    }
    return result;
  }
}
