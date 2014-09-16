/*  WordReaderUtils.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
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
    if (regex == null) {
      regex = DEFAULT_SILENCE_REGEX;
    }
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
      case RHAPSODIE:
        reader = new RhapsodieTextGridReader(filename, params.getOptionalParameter("words_tier_name", "syllabe"),
            "pseudo-ToBI", params.getOptionalParameter("charset"));
        break;
      case BURNC:
        reader = new BURNCReader(filename.replace(".ala", ""));
        break;
      case SIMPLE_WORD:
        if (params.hasParameter("ortho_idx") && params.hasParameter("start_idx") && params.hasParameter("end_idx")) {
          reader = new SimpleWordReader(filename, params.getOptionalParameter("charset"),
              Integer.parseInt(params.getOptionalParameter("ortho_idx")),
              Integer.parseInt(params.getOptionalParameter("start_idx")),
              Integer.parseInt(params.getOptionalParameter("end_idx")));
        } else {
          reader = new SimpleWordReader(filename, params.getOptionalParameter("charset"));
        }
        break;
      case SWB_NXT:
        reader = new SwitchboardNXTReader(filename.replace(".terminals.xml", ""));
        break;
      case BUCKEYE:
        reader = new BuckeyeReader(filename);
        break;
      case KOH:
        reader = new KOHReader(filename);
        break;
      case DUR:
        reader = new DURReader(filename);
        break;
      case CONS_GZ:
        reader = new ConsGZReader(filename, params.getOptionalParameter("charset"));
        break;
      case POSTING_LIST:
        try {
          reader = new PostingListReader(filename, params.getParameter("target_stem"));
        } catch (AuToBIException e) {
          AuToBIUtils.error(e.getMessage());
          return null;
        }
        break;
      default:
        reader = new TextGridReader(filename);
    }
    return reader;
  }
}
