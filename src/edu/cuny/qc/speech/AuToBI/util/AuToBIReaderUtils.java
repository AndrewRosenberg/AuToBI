/*  AuToBIReaderUtils.java

    Copyright 2009-2014 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
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
   * Globs a file pattern and generates FormattedFiles with a user speficied format.
   * <p/>
   * If the format parameter is null, this defaults to the extension based formatting.
   *
   * @param pattern The file pattern.
   * @param format  The file format
   * @return a list of FormattedFile objects with associated formats.
   * @see edu.cuny.qc.speech.AuToBI.io.FormattedFile
   */

  public static List<FormattedFile> globFormattedFiles(String pattern, FormattedFile.Format format) {
    if (format == null) {
      return globFormattedFiles(pattern);
    }
    List<String> filenames = AuToBIUtils.glob(pattern);
    List<FormattedFile> files = new ArrayList<FormattedFile>();
    for (String filename : filenames) {
      files.add(new FormattedFile(filename, format));
    }
    return files;
  }

}
