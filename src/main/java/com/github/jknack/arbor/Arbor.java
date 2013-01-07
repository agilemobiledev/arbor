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

import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.arbor.bower.BowerResolver;
import com.github.jknack.arbor.jam.JamResolver;
import com.github.jknack.arbor.volo.VoloResolver;

public class Arbor {

  static {
    DependencyResolver[] resolvers = {new UrlResolver(), new JamResolver(), new VoloResolver(),
        new BowerResolver() };
    for (DependencyResolver resolver : resolvers) {
      ResolverRegistry.register(resolver);
    }
  }

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private File baseDir;

  private final List<DependencyResolver> resolvers = new ArrayList<DependencyResolver>();

  public Arbor(final File baseDir) {
    this.baseDir = notNull(baseDir, "The baseDir is required.");
    setResolvers("url", "jam", "bower", "volo");
  }

  public void setResolvers(final String... resolvers) {
    this.resolvers.clear();
    for (String resolver : resolvers) {
      this.resolvers.add(ResolverRegistry.get(resolver));
    }
  }

  public Module resolve(final File packageJsonFile) throws IOException {
    final Map<ModuleId, Module> registry = loadModules(baseDir);
    DependencyContext context = newDependencyContext(baseDir, registry);
    PackageJson packageJson = JsonParser.read(FileUtils.readFileToString(packageJsonFile),
        PackageJson.class);
    Module root = new Module(new ModuleId(packageJson.getName(), packageJson.getVersion()),
        packageJsonFile.getParentFile(), getName(packageJsonFile.getAbsolutePath()));

    for (Entry<String, String> dependency : packageJson.getDependencies().entrySet()) {
      ModuleId moduleId = new ModuleId(dependency.getKey(), dependency.getValue());
      Module module = null;
      for (DependencyResolver resolver : resolvers) {
        try {
          module = resolver.resolve(context, moduleId);
          break;
        } catch (UnresolvedDependencyException ex) {
          logger.debug("{} fail to resolve: {}", resolver.getName(), moduleId);
          logger.debug("  reason:", ex);
        }
      }
      if (module == null) {
        throw new UnresolvedDependencyException(moduleId);
      }
      root.add(module);
    }
    return root;
  }

  public Module resolve(final ModuleId moduleId) throws IOException {
    final Map<ModuleId, Module> registry = loadModules(baseDir);
    Module cached = registry.get(moduleId);
    if (cached != null) {
      logger.info("{} found in cache", moduleId);
      return cached;
    }
    DependencyContext context = newDependencyContext(baseDir, registry);
    for (DependencyResolver resolver : resolvers) {
      try {
        Module module = resolver.resolve(context, moduleId);
        logger.info("  found it!!");
        return module;
      } catch (UnresolvedDependencyException ex) {
        logger.info("  not found");
      }
    }
    throw new UnresolvedDependencyException(moduleId + " tried " + resolvers);
  }

  private Map<ModuleId, Module> loadModules(final File baseDir) throws IOException {
    Map<ModuleId, Module> modules = new HashMap<ModuleId, Module>();
    Collection<File> files = FileUtils.listFiles(baseDir, new NameFileFilter("module.json"),
        TrueFileFilter.INSTANCE);
    for (File file : files) {
      Module module = JsonParser.read(FileUtils.readFileToString(file), Module.class);
      modules.put(module.getId(), module);
    }
    return modules;
  }

  private static DependencyContext newDependencyContext(final File baseDir,
      final Map<ModuleId, Module> registry) {
    return new DependencyContext() {
      @Override
      public void put(final ModuleId moduleId, final Module module) {
        registry.put(moduleId, module);
      }

      @Override
      public File moduleRoot(final ModuleId moduleId) {
        File moduleRoot = new File(baseDir, moduleId.getName());
        if (!moduleRoot.exists()) {
          moduleRoot.mkdirs();
        }
        return moduleRoot;
      }

      @Override
      public File moduleHome(final ModuleId moduleId) {
        File moduleHome = new File(moduleRoot(moduleId), moduleId.getRevision());
        if (!moduleHome.exists()) {
          moduleHome.mkdirs();
        }
        return moduleHome;
      }

      @Override
      public Module get(final ModuleId moduleId) {
        Module module = registry.get(moduleId);
        return module;
      }

      @Override
      public File getBaseDir() {
        return baseDir;
      }
    };
  }
}
