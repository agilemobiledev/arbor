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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.semver.Semver;

public abstract class AbstractResolver<T> implements DependencyResolver {

  protected static class ResolvedRevision<T> {
    public final T descritpor;

    public final ModuleId moduleId;

    public ResolvedRevision(final T descritpor, final ModuleId moduleId) {
      this.descritpor = descritpor;
      this.moduleId = moduleId;
    }

    @Override
    public String toString() {
      return moduleId.toString();
    }
  }

  /**
   * The logging system.
   */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public final Module resolve(final DependencyContext context, final ModuleId moduleId)
      throws IOException {
    try {
      Semver version = Semver.create(moduleId.getRevision());
      if (version.isStatic()) {
        Module existing = context.get(moduleId);
        if (existing != null) {
          logger.info("{} found in cache", moduleId);
          return existing;
        }
      }
    } catch (IllegalArgumentException ex) {
      // it should a 'latest' or something invalid
      logger.trace("Can't parse: {}", moduleId.getRevision());
    }
    boolean success = false;
    logger.info("resolving {} using {}", moduleId, getName());
    ResolvedRevision<T> revision = doResolve(moduleId);
    File moduleHome = context.moduleHome(revision.moduleId);
    try {
      Module existing = context.get(revision.moduleId);
      if (existing != null) {
        logger.info("{} resolved from cache", revision.moduleId);
        success = true;
        return existing;
      }
      logger.info("downloading {} using {}", revision.moduleId, getName());
      Module resolved = newModule(context, revision.descritpor, revision.moduleId);
      resolved.save();
      success = true;
      return resolved;
    } finally {
      if (!success) {
        logger.info("{} fail to resolve: {}", getName(), revision.moduleId);
        // clean up context
        context.put(revision.moduleId, null);
        logger.debug("Cleaning up: {} at {}", revision.moduleId, moduleHome);
        File moduleRoot = moduleHome.getParentFile();
        if (moduleRoot.listFiles().length == 1) {
          FileUtils.deleteDirectory(moduleRoot);
        } else {
          FileUtils.deleteDirectory(moduleHome);
        }
      }
    }
  }

  protected abstract ResolvedRevision<T> doResolve(ModuleId moduleId) throws IOException;

  protected abstract Module newModule(final DependencyContext context, T descriptor,
      final ModuleId moduleId) throws IOException;

  @Override
  public String toString() {
    return getName();
  }
}
