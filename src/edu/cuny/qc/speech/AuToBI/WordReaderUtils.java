/*  WordReaderUtils.java

    Copyright (c) 20092010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI;

/**
 * A Utility Class for functions used by AuToBIWordReaders.
 */
public class WordReaderUtils {
  /**
   * Returns true if the label indicates that the region represents silence.
   * <p/>
   * Currently this matches the strings "#", ">brth", "}sil", "endsil", "sil", as well as, null and empty strings
   * <p/>
   * TODO Allow the list of silent labels to be set by the user or through command line parameters.
   *
   * @param label the region to check
   * @return true if r is a silent region
   */
  public static boolean isSilentRegion(String label) {
    if (label.length() > 0 && !label.matches("(#|>brth|brth|}sil|endsil|sil|_|_\\*_|\\*_|_\\*)")) {
      return false;
    }
    return true;
  }

  /**
   * Replaces tabs with space characters and trims the string.
   *
   * @param line the input string
   * @return a trimmed, and cleaned string.
   */
  public static String removeTabsAndTrim(String line) {
    line = line.replace("\t", " ").trim();
    return line;
  }
}
