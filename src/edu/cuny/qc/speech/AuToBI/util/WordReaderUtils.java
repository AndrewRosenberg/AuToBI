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
package edu.cuny.qc.speech.AuToBI.util;

/**
 * A Utility Class for functions used by AuToBIWordReaders.
 */
public class WordReaderUtils {
  public static String DEFAULT_SILENCE_REGEX = "(#|>brth|brth|}sil|endsil|sil|_|_\\*_|\\*_|_\\*)";

  /**
   * Returns true if the label indicates that the region represents silence.
   * <p/>
   * Matches the strings "#", ">brth", "}sil", "endsil", "sil", as well as, null and empty strings
   * <p/>
   *
   * @param label the region to check
   * @return true if r is a silent region
   */
  public static boolean isSilentRegion(String label) {
    return isSilentRegion(label, DEFAULT_SILENCE_REGEX);
  }

  /**
   * Returns true if the label indicates that the region represents silence.
   * <p/>
   * Silent regions are indicated by the regular expression passed as a parameter.
   * <p/>
   *
   * @param label the region to check
   * @param regex the regular expression to match silence
   * @return true if r is a silent region
   */
  public static boolean isSilentRegion(String label, String regex) {
    if (regex == null)
      regex = DEFAULT_SILENCE_REGEX;
    if (label.length() > 0 && !label.matches(regex)) {
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
