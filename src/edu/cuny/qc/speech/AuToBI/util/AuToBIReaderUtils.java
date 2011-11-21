/*  AuToBIReaderUtils.java

    Copyright 2009-2010 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.io.FormattedFile;

import java.util.ArrayList;
import java.util.List;

/**
 * AuToBIReaderUtils is a utility class to store static methods to support reading files in AuToBI.
 */
public class AuToBIReaderUtils {

  // Utility Classes cannot be initialized.
  private AuToBIReaderUtils() {
    throw new AssertionError();
  }

  /**
   * Replaces all tabs with spaces and trims left and right whitespace from a string.
   *
   * @param line the string
   * @return a cleaned string.
   */
  public static String removeTabsAndTrim(String line) {
    line = line.replace("\t", " ").trim();
    return line;
  }

  /**
   * Globs a file pattern and generates FormattedFiles based on the filename extensions.
   *
   * @param pattern The file pattern.
   * @return a list of FormattedFile objects with associated formats.
   */
  public static List<FormattedFile> globFormattedFiles(String pattern) {
    List<String> filenames = AuToBIUtils.glob(pattern);
    List<FormattedFile> files = new ArrayList<FormattedFile>();
    for (String filename : filenames) {
      files.add(new FormattedFile(filename));
    }
    return files;
  }

  /**
   * Globs a file pattern and generates FormattedFiles with a user speficied format
   *
   * @param pattern The file pattern.
   * @param format  The file format
   * @return a list of FormattedFile objects with associated formats.
   */

  public static List<FormattedFile> globFormattedFiles(String pattern, FormattedFile.Format format) {
    List<String> filenames = AuToBIUtils.glob(pattern);
    List<FormattedFile> files = new ArrayList<FormattedFile>();
    for (String filename : filenames) {
      files.add(new FormattedFile(filename, format));
    }
    return files;
  }

}
