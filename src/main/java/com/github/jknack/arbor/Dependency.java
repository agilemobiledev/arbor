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

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * A dependency have a name, version, location and optional dependencies.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Dependency {

  /**
   * Generate a file's name for a dependency.
   *
   * @author edgar.espina
   * @since 0.0.1
   */
  public interface FileNameGenerator {

    /**
     * The default name's generator.
     */
    FileNameGenerator DEFAULT = new FileNameGenerator() {

      @Override
      public String generate(final Dependency dependency) {
        return dependency.getName() + "-" + dependency.getVersion() + ".js";
      }
    };

    /**
     * Generate a file's name for a dependency.
     *
     * @param dependency A dependency.
     * @return A file's name for a dependency.
     */
    String generate(Dependency dependency);
  }

  /**
   * The dependency's name.
   */
  private String name;

  /**
   * The dependency's version.
   */
  private String version;

  /**
   * A dependency set. Optional.
   */
  private Set<Dependency> dependencies = new LinkedHashSet<Dependency>();

  /**
   * A location for this dependency.
   */
  private File location;

  /**
   * Creates a new dependency.
   *
   * @param name The dependency's name.
   * @param version The dependency's version.
   * @param location The dependency's location.
   */
  public Dependency(final String name, final String version, final File location) {
    this.name = notEmpty(name, "The dependency's name is required.");
    this.version = notEmpty(version, "The dependency's version is required.");
    this.location = notNull(location, "The dependency's location is required.");
  }

  /**
   * Creates a new dependency.
   *
   * @param name The dependency's name.
   * @param location The dependency's location.
   */
  public Dependency(final String name, final File location) {
    this(name, "unknown", location);
  }

  /**
   * True, if the dependency exists in the local repository.
   *
   * @return True, if the dependency exists in the local repository.
   */
  public boolean exists() {
    return location != null && location.exists();
  }

  /**
   * The dependency's name.
   *
   * @return The dependency's name.
   */
  public String getName() {
    return name;
  }

  /**
   * The dependency's version.
   *
   * @return The dependency's version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * The dependency's location in the local repository.
   *
   * @return The dependency's location in the local repository.
   */
  public File getLocation() {
    return location;
  }

  /**
   * List all the dependencies (if any).
   *
   * @return List all the dependencies (if any).
   */
  public Iterator<Dependency> getDependencies() {
    return dependencies.iterator();
  }

  /**
   * Add a new dependency.
   *
   * @param dependency A dependency. Required.
   */
  public void add(final Dependency dependency) {
    dependencies.add(notNull(dependency, "A dependency is required."));
  }

  /**
   * Remove a new dependency.
   *
   * @param dependency A dependency. Required.
   */
  public void remove(final Dependency dependency) {
    dependencies.remove(notNull(dependency, "A dependency is required."));
  }

  /**
   * The dependency's id.
   *
   * @return The dependency's id.
   */
  public String getId() {
    return name + "@" + version;
  }

  /**
   * Copy all the dependency graph to the output directory.
   *
   * @param outputDir The output directory. Must exists.
   * @throws IOException If the dependency graph cannot be copy.
   */
  public void copy(final File outputDir) throws IOException {
    copy(outputDir, FileNameGenerator.DEFAULT);
  }

  /**
   * Copy all the dependency graph to the output directory.
   *
   * @param outputDir The output directory. Must exists.
   * @param nameGenerator A file name generator. Required.
   * @throws IOException If the dependency graph cannot be copy.
   */
  public void copy(final File outputDir, final FileNameGenerator nameGenerator)
      throws IOException {
    notNull(outputDir, "The outputDir is required.");
    isTrue(outputDir.exists(), "File not found: " + outputDir);
    isTrue(outputDir.isDirectory(), "Not a directory: " + outputDir);
    notNull(nameGenerator, "The name generator is required.");

    File output = new File(outputDir, nameGenerator.generate(this));
    Iterator<Dependency> dependencies = getDependencies();
    while (dependencies.hasNext()) {
      Dependency dependency = dependencies.next();
      dependency.copy(outputDir, nameGenerator);
    }
    FileUtils.copyFile(location, output);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getId()).append(" ").append(location).append("\n");
    for (Dependency dependency : dependencies) {
      buffer.append("|--- ").append(dependency.getId());
      if (dependency.dependencies.size() > 0) {
        buffer.append(" (");
        String separator = ", ";
        for (Dependency transDep : dependency.dependencies) {
          buffer.append(transDep.getId()).append(separator);
        }
        buffer.setLength(buffer.length() - separator.length());
        buffer.append(")");
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

}
