package com.github.jknack.arbor.io;

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

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.codehaus.plexus.archiver.AbstractUnArchiver;
import org.codehaus.plexus.archiver.bzip2.BZip2UnArchiver;
import org.codehaus.plexus.archiver.gzip.GZipUnArchiver;
import org.codehaus.plexus.archiver.tar.TarBZip2UnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating tar.gz file extractor.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public final class FileExtractor {

  /**
   * A bridge log over slf4j.
   *
   * @author edgar.espina
   * @since 0.1.0
   */
  private static class PlexusLogger implements org.codehaus.plexus.logging.Logger {
    @Override
    public void warn(final String message, final Throwable throwable) {
      logger.warn(message, throwable);
    }

    @Override
    public void warn(final String message) {
      logger.warn(message);
    }

    @Override
    public boolean isWarnEnabled() {
      return logger.isWarnEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
      return logger.isInfoEnabled();
    }

    @Override
    public boolean isFatalErrorEnabled() {
      return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
      return logger.isErrorEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
      return logger.isDebugEnabled();
    }

    @Override
    public void info(final String message, final Throwable throwable) {
      logger.trace(message, throwable);
    }

    @Override
    public void info(final String message) {
      logger.trace(message);
    }

    @Override
    public int getThreshold() {
      return 0;
    }

    @Override
    public String getName() {
      return logger.getName();
    }

    @Override
    public org.codehaus.plexus.logging.Logger getChildLogger(final String name) {
      return this;
    }

    @Override
    public void fatalError(final String message, final Throwable throwable) {
      logger.error(message, throwable);
    }

    @Override
    public void fatalError(final String message) {
      logger.error(message);
    }

    @Override
    public void error(final String message, final Throwable throwable) {
      logger.error(message, throwable);
    }

    @Override
    public void error(final String message) {
      logger.error(message);
    }

    @Override
    public void debug(final String message, final Throwable throwable) {
      logger.trace(message, throwable);
    }

    @Override
    public void debug(final String message) {
      logger.trace(message);
    }
  }

  private enum UnArchiverFactory {
    ZIP {
      @Override
      public String[] extensions() {
        return new String[]{".zip" };
      }

      @Override
      public AbstractUnArchiver create(final File source, final File destination,
          final FileSelector... selectors) {
        ZipUnArchiver zip = new ZipUnArchiver(source) {
          @Override
          protected void extractFile(final File srcF, final File dir,
              final InputStream compressedInputStream,
              final String entryName, final Date entryDate, final boolean isDirectory,
              final Integer mode) throws IOException {
            final FileInfo fileInfo = createFileInfo(compressedInputStream, entryName,
                isDirectory, selectors);
            if (fileInfo == null) {
              return;
            }
            super.extractFile(srcF, dir, compressedInputStream, fileInfo.getName(), entryDate,
                isDirectory, mode);
          }
        };
        zip.setDestDirectory(destination);
        return zip;
      }
    },

    TAR_BZ2 {

      @Override
      public String[] extensions() {
        return new String[]{".tbz", ".tbz2", ".tb2", ".tar.bz2" };
      }

      @Override
      public AbstractUnArchiver create(final File source, final File destination,
          final FileSelector... selectors) {
        TarBZip2UnArchiver tarbz2 = new TarBZip2UnArchiver(source) {
          @Override
          protected void extractFile(final File srcF, final File dir,
              final InputStream compressedInputStream,
              final String entryName, final Date entryDate, final boolean isDirectory,
              final Integer mode) throws IOException {
            final FileInfo fileInfo = createFileInfo(compressedInputStream, entryName,
                isDirectory, selectors);
            if (fileInfo == null) {
              return;
            }
            super.extractFile(srcF, dir, compressedInputStream, fileInfo.getName(), entryDate,
                isDirectory, mode);
          }
        };
        tarbz2.setDestDirectory(destination);
        return tarbz2;
      }
    },

    TAR_GZ {

      @Override
      public String[] extensions() {
        return new String[]{".tgz", ".tar.gz" };
      }

      @Override
      public AbstractUnArchiver create(final File source, final File destination,
          final FileSelector... selectors) {
        TarGZipUnArchiver targz = new TarGZipUnArchiver(source) {
          @Override
          protected void extractFile(final File srcF, final File dir,
              final InputStream compressedInputStream,
              final String entryName, final Date entryDate, final boolean isDirectory,
              final Integer mode) throws IOException {
            final FileInfo fileInfo = createFileInfo(compressedInputStream, entryName,
                isDirectory, selectors);
            if (fileInfo == null) {
              return;
            }
            super.extractFile(srcF, dir, compressedInputStream, fileInfo.getName(), entryDate,
                isDirectory, mode);
          }
        };
        targz.setDestDirectory(destination);
        return targz;
      }
    },

    BZIP2 {
      @Override
      public String[] extensions() {
        return new String[]{".bz2" };
      }

      @Override
      public AbstractUnArchiver create(final File source, final File destination,
          final FileSelector... selectors) {
        BZip2UnArchiver bz2 = new BZip2UnArchiver(source);
        bz2.setDestFile(destination);
        return bz2;
      }
    },

    GZIP {
      @Override
      public String[] extensions() {
        return new String[]{".gz" };
      }

      @Override
      public AbstractUnArchiver create(final File source, final File destination,
          final FileSelector... selectors) {
        GZipUnArchiver bz2 = new GZipUnArchiver(source);
        bz2.setDestFile(destination);
        return bz2;
      }
    };

    public final boolean apply(final File archiver) {
      String[] extensions = extensions();
      String filename = archiver.getName();
      for (String ext : extensions) {
        if (filename.endsWith(ext)) {
          return true;
        }
      }
      return false;
    }

    public abstract String[] extensions();

    public abstract AbstractUnArchiver create(final File source, File destination,
        final FileSelector... selectors);

    public static AbstractUnArchiver newUnArchiver(final File source, final File destination,
        final FileSelector... selectors) {
      for (UnArchiverFactory unArchiverFactory : values()) {
        if (unArchiverFactory.apply(source)) {
          return unArchiverFactory.create(source, destination, selectors);
        }
      }
      throw new UnsupportedOperationException("File extension: " + getExtension(source.getName()));
    }

    protected static FileInfo createFileInfo(final InputStream compressedInputStream,
        final String entryName, final boolean isDirectory, final FileSelector... selectors)
        throws IOException {
      String prefix = "package/";
      final String newEntryName = entryName.startsWith(prefix) ? entryName.substring(prefix
          .length()) : entryName;

      FileInfo fileInfo = new FileInfo() {

        @Override
        public boolean isFile() {
          return !isDirectory;
        }

        @Override
        public boolean isDirectory() {
          return isDirectory;
        }

        @Override
        public String getName() {
          return newEntryName;
        }

        @Override
        public InputStream getContents() throws IOException {
          return compressedInputStream;
        }
      };

      for (FileSelector selector : selectors) {
        if (!selector.isSelected(fileInfo)) {
          return null;
        }
      }
      return fileInfo;
    }
  }

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(FileExtractor.class);

  /**
   * Not alloed.
   */
  private FileExtractor() {
  }

  public static void extract(final File source, final File destination,
      final FileSelector... selectors) {
    notNull(source, "The source file is required.");
    notNull(destination, "The destination file is required.");

    AbstractUnArchiver archiver = UnArchiverFactory.newUnArchiver(source, destination, selectors);
    archiver.enableLogging(new PlexusLogger());
    archiver.setFileSelectors(selectors);
    archiver.extract();
  }

}
