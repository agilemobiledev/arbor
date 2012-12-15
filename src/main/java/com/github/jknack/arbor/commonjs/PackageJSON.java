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
package com.github.jknack.arbor.commonjs;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.arbor.Dependency;
import com.github.jknack.arbor.version.Expression;
import com.github.jknack.arbor.version.ExpressionParser;

/**
 * A package.json object. For simplicity and performance reason this class only take care of: name,
 * version, main, dist and dependencies entries.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class PackageJSON {

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
   * The module's name.
   */
  @JsonProperty
  private String name;

  /**
   * The module's version.
   */
  @JsonProperty
  private String version;

  /**
   * The module's main js file.
   */
  @JsonProperty
  private String main;

  /**
   * The module's dependency.
   */
  @JsonProperty
  private Map<String, String> dependencies;

  /**
   * The module's distribution entry.
   */
  @JsonProperty
  private Distribution dist;

  /**
   * A jam entry. Optional.
   */
  @JsonProperty
  private Map<String, Object> jam;

  /**
   * Required by Jackson.
   *
   * @see #from(File)
   * @see #from(String)
   */
  protected PackageJSON() {
  }

  /**
   * The module's name.
   *
   * @return The module's name.
   */
  public String getName() {
    return name;
  }

  /**
   * The module's version.
   *
   * @return The module's version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * The module main entry.
   *
   * @return The module main entry.
   */
  public String getMain() {
    String main = this.main;
    if (isEmpty(main)) {
      // try jam
      if (jam != null) {
        main = (String) jam.get("main");
      }
    }
    if (isEmpty(main)) {
      // TODO: check if index.js is the expected result for missing main entries.
      main = "index.js";
    }
    if (!main.endsWith(".js")) {
      main += ".js";
    }
    if (main.startsWith(".")) {
      main = main.substring(1);
    }
    if (main.startsWith("/")) {
      main = main.substring(1);
    }
    return main;
  }

  /**
   * The module dependencies.
   *
   * @return The module dependencies.
   */
  public Map<String, String> getDependencies() {
    if (dependencies == null) {
      return Collections.emptyMap();
    }
    return new LinkedHashMap<String, String>(dependencies);
  }

  /**
   * The module tarball URI.
   *
   * @return The module tarball URI.
   */
  public URI getTarball() {
    return dist.getTarball();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("name", name)
        .append("version", version)
        .append("dist", dist)
        .append("dependencies", dependencies)
        .build();
  }

  /**
   * Creates a {@link PackageJSON} from a file location.
   *
   * @param input The file location.
   * @return A new {@link PackageJSON}.
   * @throws IOException If the package json file cannot be read it.
   */
  public static PackageJSON from(final File input) throws IOException {
    return objectMapper.readValue(input, PackageJSON.class);
  }

  /**
   * Creates a {@link PackageJSON} from a file location.
   *
   * @param input The file location.
   * @return A new {@link PackageJSON}.
   * @throws IOException If the package json file cannot be read it.
   */
  public static PackageJSON from(final String input) throws IOException {
    return objectMapper.readValue(input, PackageJSON.class);
  }

  /**
   * Find the best matching version of a library.
   *
   * @param versions The available versions in the local repository in descending order.
   * @param name The module's name.
   * @param version A version expression.
   * @return The version number.
   */
  private String resolveVersion(final List<Expression> versions, final String name,
      final String version) {
    if (Expression.LATEST.equals(version)) {
      return versions.get(0).toString();
    }

    if (versions != null && versions.size() > 0) {
      Expression expr = ExpressionParser.parse(version);
      for (Expression expression : versions) {
        if (expr.matches(expression)) {
          return expression.toString();
        }
      }
    }
    String versionsStr = versions == null ? "[]" : "[" + join(versions, ", ") + "]";
    throw new IllegalArgumentException("No matches found for: " + name + "@" + version + ", in: "
        + versionsStr);
  }

  /**
   * Resolve this package as a {@link Dependency}.
   *
   * @param baseDir The root of the local repository. Must exists.
   * @param registry The available versions in the local repository.
   * @return A new {@link Dependency} graph.
   * @throws IOException If something goes wrong.
   */
  public Dependency resolve(final File baseDir, final Map<String, List<Expression>> registry)
      throws IOException {
    File location = new File(baseDir, name + File.separator + version + File.separator + getMain());
    if (!location.exists()) {
      throw new PackageIntegrityException(name + "@" + version, location);
    }
    Dependency root = new Dependency(name, version, location);
    Map<String, String> dependencies = getDependencies();
    for (Entry<String, String> entry : dependencies.entrySet()) {
      String name = entry.getKey();
      String version = entry.getValue();
      List<Expression> versions = registry.get(name);
      version = resolveVersion(versions, name, version);

      String depLocation = name + File.separator + version + File.separator + "package.json";
      File packageJSONFile = new File(baseDir, depLocation);
      PackageJSON packageJSON = PackageJSON.from(packageJSONFile);
      root.add(packageJSON.resolve(baseDir, registry));
    }
    return root;
  }

}
