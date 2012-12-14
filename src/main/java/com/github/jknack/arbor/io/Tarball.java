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
package com.github.jknack.arbor.io;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
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
public final class Tarball {

  /**
   * A bridge log over slf4j.
   *
   * @author edgar.espina
   * @since 0.1.0
   */
  private static class PlexusLogger implements org.codehaus.plexus.logging.Logger {
    @Override
    public void warn(final String message, final Throwable throwable) {
      logger.debug(message, throwable);
    }

    @Override
    public void warn(final String message) {
      logger.debug(message);
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
      logger.debug(message, throwable);
    }

    @Override
    public void info(final String message) {
      logger.debug(message);
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
      logger.debug(message, throwable);
    }

    @Override
    public void debug(final String message) {
      logger.debug(message);
    }
  }

  /**
   * Not alloed.
   */
  private Tarball() {
  }

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(Tarball.class);

  /**
   * Creates a new file extractor. The generated extractor remove the first path of a tar entry
   * (usually, 'package').
   *
   * @param tarFile The tar.gz file. Required.
   * @param fileSelectors A list of files selector. Required.
   * @return A new file extractor.
   */
  public static TarGZipUnArchiver newTarGZipUnArchiver(final File tarFile,
      final FileSelector... fileSelectors) {
    notNull(tarFile, "The tar file is required.");

    TarGZipUnArchiver tarGZipUnArchiver = new TarGZipUnArchiver() {
      @Override
      protected void extractFile(final File srcF, final File dir,
          final InputStream compressedInputStream,
          final String entryName, final Date entryDate, final boolean isDirectory,
          final Integer mode) throws IOException {
        String prefix = entryName.split("/")[0];
        final String newEntryName = prefix.length() > 0
            ? entryName.substring(prefix.length() + 1)
            : entryName;

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

        for (FileSelector fileSelector : fileSelectors) {
          if (!fileSelector.isSelected(fileInfo)) {
            return;
          }
        }
        super.extractFile(srcF, dir, compressedInputStream, newEntryName, entryDate, isDirectory,
            mode);
      }
    };
    tarGZipUnArchiver.setSourceFile(tarFile);
    tarGZipUnArchiver.enableLogging(new PlexusLogger());
    tarGZipUnArchiver.setFileSelectors(fileSelectors);
    return tarGZipUnArchiver;
  }
}
