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

/**
 * AuToBIReaderUtils is a utility class to store static methods to support reading files in AuToBI.
 */
public class AuToBIReaderUtils {
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
}
