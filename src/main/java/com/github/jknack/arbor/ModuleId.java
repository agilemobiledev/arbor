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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ModuleId {

  private String name;

  private String revision;

  public ModuleId(final String name, final String revision) {
    this.name = name;
    this.revision = revision.replace(" ", "");
  }

  protected ModuleId() {
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ModuleId) {
      ModuleId that = (ModuleId) obj;
      return new EqualsBuilder().append(name, that.name).append(revision, that.revision).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).append(revision).build();
  }

  public String getName() {
    return name;
  }

  public String getRevision() {
    return revision;
  }

  @Override
  public String toString() {
    return name + "@" + revision;
  }
}
