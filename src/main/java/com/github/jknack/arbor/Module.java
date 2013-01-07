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
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Module {

  private ModuleId id;

  private Set<Module> dependencies = new LinkedHashSet<Module>();

  private File home;

  private String main;

  public Module(final ModuleId moduleId, final File home, final String main) {
    id = notNull(moduleId, "The moduleId is required.");
    this.home = notNull(home, "The moduleHome is required.");
    this.main = notEmpty(main, "The mainJs is required.");
  }

  protected Module() {
  }

  public ModuleId getId() {
    return id;
  }

  public File getLocalFile() {
    return new File(home, main);
  }

  public void add(final Module dependency) {
    dependencies.add(dependency);
  }

  public void traverse(final ModuleVisitor visitor) throws IOException {
    visitor.visit(this);
    for (Module dependency : dependencies) {
      dependency.traverse(visitor);
    }
    visitor.endvisit(this);
  }

  public void copyTo(final File baseDir) throws IOException {
    traverse(new ModuleVisitor() {
      private Map<String, Module> modules = new HashMap<String, Module>();

      @Override
      public void visit(final Module module) throws IOException {
        ModuleId moduleId = module.getId();
        if (!modules.containsKey(moduleId.getName())) {
          final String filename;
          if (URI.create(moduleId.getRevision()).isAbsolute()) {
            filename = getName(moduleId.getRevision());
          } else {
            filename = moduleId.getName() + "-" + moduleId.getRevision() + ".js";
          }
          FileUtils.copyFile(module.getLocalFile(), new File(baseDir, filename));
          modules.put(moduleId.getName(), module);
        }
      }

      @Override
      public void endvisit(final Module module) {
      }
    });
  }

  @Override
  public String toString() {
    final Module root = this;
    ModuleVisitor visitor = new ModuleVisitor() {
      private StringBuilder buffer = new StringBuilder();
      private int level = 0;
      private Map<String, Module> modules = new HashMap<String, Module>();

      @Override
      public void visit(final Module module) {
        Module existing = modules.get(module.getId().getName());
        if (root != module) {
          if (level > 2) {
            buffer.append(leftPad("", level));
          }
          buffer.append("|-- ");
          buffer.append(module.id);
          if (existing != null && !module.id.equals(existing.id)) {
            buffer.append(" (resolved as ").append(existing.id).append(")");
          }
          buffer.append("\n");
        } else {
          buffer.append(module.id).append("\n");
        }
        if (existing == null) {
          modules.put(module.getId().getName(), module);
        }
        level += 2;
      }

      @Override
      public void endvisit(final Module module) {
        level -= 2;
      }

      @Override
      public String toString() {
        return buffer.toString().trim();
      }
    };
    try {
      traverse(visitor);
    } catch (IOException e) {
      // shouldn't happen
      throw new IllegalStateException(e);
    }
    return visitor.toString();
  }

  public void validate() throws UnresolvedDependencyException {
    if (!getLocalFile().exists()) {
      // corrupted package!!!
      // 1) package.json exists but the 'main' entry points to unexisting file or
      // 2) there is no package json at all
      // So, fallback and try to locate a package in the home directory using the module name
      Collection<File> files = FileUtils.listFiles(home, new NameFileFilter(id.getName() + ".js"),
          TrueFileFilter.INSTANCE);
      if (files.size() == 0) {
        // still not found, try with version number.
        files = FileUtils.listFiles(home, new NameFileFilter(id.getName() + "-" + id.getRevision()
            + ".js"), TrueFileFilter.INSTANCE);
      }
      if (files.size() == 0) {
        throw new UnresolvedDependencyException("Unable to resolve " + id + " to a file");
      }
      // override main property
      File candidate = files.iterator().next();
      main = candidate.getAbsolutePath().substring(home.getAbsolutePath().length());
      if (!getLocalFile().exists()) {
        throw new UnresolvedDependencyException("Unable to resolve " + id + " to a file");
      }
    }
    for (Module dependency : dependencies) {
      dependency.validate();
    }
  }

  public void save() throws IOException {
    validate();
    JsonParser.write(new File(home, "module.json"), this);
  }
}
