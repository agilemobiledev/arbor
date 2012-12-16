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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.arbor.commonjs.PackageIntegrityException;
import com.github.jknack.arbor.commonjs.PackageNotFoundException;
import com.github.jknack.arbor.version.Expression;
import com.github.jknack.arbor.version.ExpressionParser;

/**
 * Base class for Arbor dependency resolvers.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class DependencyResolver {

  /**
   * A dependency descriptor.
   *
   * @author edgar.espina
   */
  public static class DependencyDescriptor {
    /**
     * The dependency's name. Optional.
     */
    private String name;

    /**
     * The dependency's version. Optional.
     */
    private String version;

    /**
     * The dependency's path.
     */
    private String path;

    /**
     * The dependency's id.
     *
     * @return The dependency's id.
     */
    public String getId() {
      return name + "@" + version;
    }

    /**
     * Set the dependency's name.
     *
     * @param name The dependency's name.
     */
    public void setName(final String name) {
      this.name = name;
    }

    /**
     * Set the dependency's path.
     *
     * @param path The dependency's path.
     */
    public void setPath(final String path) {
      this.path = path;
    }

    /**
     * The dependency's path.
     *
     * @return The dependency's path.
     */
    public String getPath() {
      return path;
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
     * Set the dependency's version.
     *
     * @param version The dependency's version.
     */
    public void setVersion(final String version) {
      this.version = version;
    }

    @Override
    public String toString() {
      return getId();
    }
  }

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The home location of the dependencies. Required.
   */
  protected final File homeDir;

  /**
   * Creates a new dependency resolver.
   *
   * @param homeDir The home location of the dependencies. Required.
   */
  public DependencyResolver(final File homeDir) {
    this.homeDir = notNull(homeDir, "The homeDir is required.");
  }

  /**
   * The resolver's name.
   *
   * @return The resolver's name.
   */
  public abstract String name();

  /**
   * The home location of the dependencies. Required.
   *
   * @return The home location of the dependencies. Required.
   */
  public final File homeDir() {
    return homeDir;
  }

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param path The dependency path.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  public Dependency resolve(final String path) throws IOException {
    notEmpty(path, "The path is required.");
    try {
      Dependency dependency = null;
      if (canResolve(path)) {
        DependencyDescriptor descriptor = newDependencyDescriptor(path);
        dependency = resolveLocal(descriptor);
        if (!dependency.exists()) {
          dependency = doResolve(descriptor);
        }
      }
      return dependency;
    } catch (PackageIntegrityException ex) {
      logger.debug("Invalid package", ex);
      // recover from a package not found and move to the next resolver (if any)
      return null;
    } catch (PackageNotFoundException ex) {
      logger.debug("Package not found", ex);
      // recover from a package not found and move to the next resolver (if any)
      return null;
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        logger.debug("Package not found", ex);
        // recover from a package not found and move to the next resolver (if any)
        return null;
      }
      throw ex;
    }
  }

  /**
   * Creates a new {@link DependencyDescriptor}.
   * @param path The dependency's path.
   * @return A new {@link DependencyDescriptor}.
   * @throws IOException If descriptor creation goes wrong.
   */
  protected abstract DependencyDescriptor newDependencyDescriptor(String path) throws IOException;

  /**
   * True, if the path can be resolved by the dependency resolver.
   *
   * @param path The dependency path. Required.
   * @return True, if the path can be resolved by the dependency resolver.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract boolean canResolve(String path) throws IOException;

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param descriptor The dependency descriptor.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract Dependency doResolve(DependencyDescriptor descriptor) throws IOException;

  /**
   * Resolve the dependency path to a {@link Dependency}. The dependency path format depends on a
   * specify dependency resolver.
   *
   * @param descriptor The dependency descriptor.
   * @return A dependency for the given path.
   * @throws IOException If the dependency fail to be resolved.
   */
  protected abstract Dependency resolveLocal(DependencyDescriptor descriptor) throws IOException;

  /**
   * Build a module home from a module descriptor.
   *
   * @param name The module's name.
   * @return A module home directory.
   */
  protected File moduleHome(final String name) {
    return new File(homeDir, name);
  }

  /**
   * List all the modules available in the local repository. If there is more than one version per
   * module, they are ordered by most recently created.
   *
   * @return All the modules available in the local repository. If there is more than one version
   *         per module, they are ordered by most recently created.
   */
  protected Map<String, List<Expression>> jsModules() {
    Map<String, List<Expression>> modules = new HashMap<String, List<Expression>>();
    File[] moduleDirs = homeDir.listFiles();
    for (File moduleDir : moduleDirs) {
      if (moduleDir.isDirectory()) {
        List<Expression> versions = new ArrayList<Expression>();
        File[] versionDirs = moduleDir.listFiles(new FileFilter() {
          @Override
          public boolean accept(final File pathname) {
            return !pathname.getName().equals("git");
          }
        });
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

}
