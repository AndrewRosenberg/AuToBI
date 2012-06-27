/*  WordReaderUtils.java

    Copyright (c) 2009-2012 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIParameters;
import edu.cuny.qc.speech.AuToBI.io.*;

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
    if (label != null && label.length() > 0 && !label.matches(regex)) {
      return false;
    }
    return true;
  }

  /**
   * Retrieves an appropriate AuToBIWordReader based on the format of the specified file.
   *
   * @param file The formatted file
   * @return A reader capable of reading this file.
   */
  public static AuToBIWordReader getAppropriateReader(FormattedFile file, AuToBIParameters params) {
    String filename = file.getFilename();
    AuToBIWordReader reader;
    switch (file.getFormat()) {
      case TEXTGRID:
        reader = new TextGridReader(filename, params.getOptionalParameter("words_tier_name"),
            params.getOptionalParameter("tones_tier_name"), params.getOptionalParameter("breaks_tier_name"),
            params.getOptionalParameter("charset"));

        break;
      case CPROM:
        reader = new CPromTextGridReader(filename, "words", "delivery", "UTF16", params.booleanParameter(
            "cprom_include_secondary", true));
        break;
      case BURNC:
        reader = new BURNCReader(filename.replace(".ala", ""));
        break;
      case SIMPLE_WORD:
        reader = new SimpleWordReader(filename);
        break;
      case SWB_NXT:
        reader = new SwitchboardNXTReader(filename.replace(".terminals.xml", ""));
        break;
      case BUCKEYE:
        reader = new BuckeyeReader(filename);
        break;
      case DUR:
        reader = new DURReader(filename);
        break;
      default:
        reader = new TextGridReader(filename);
    }
    return reader;
  }
}
