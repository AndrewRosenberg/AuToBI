/* CPromTextGridReader.java

  Copyright 2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.List;

/**
 * A TextGridReader to process the CProm format of prominence annotations.
 */
public class CPromTextGridReader extends TextGridReader {

  private String prominence_tier_name; // the name of the prominence tier.
  private Tier prominence_tier;  // A tier to store prominence annotations
  private Boolean include_secondary;
  // A flag dictating whether the "secondary" prominence should be considered as accented.

  public CPromTextGridReader(String filename, String words_tier, String prominence_tier, String charsetName,
                             Boolean include_secondary) {
    super(filename, words_tier, null, null, charsetName);
    this.prominence_tier_name = prominence_tier;
    this.include_secondary = include_secondary;
  }

  /**
   * Generates a list of words from the associated TextGrid file.
   * <p/>
   * A list of words is generated, available ToBI information is aligned to them, and checked for consistency with the
   * standard.
   * <p/>
   * This is the main entry point for this class.
   * <p/>
   * Typical Usage:
   * <p/>
   * TextGridReader reader = new TextGridReader(filename) List<Words> data_points = reader.readWords();
   *
   * @return A list of words with from the TextGrid
   * @throws java.io.IOException if there is a reader problem
   * @throws AuToBIException     if there is an alignment problem
   */
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader file_reader;
    if (charsetName == null) {
      file_reader = new AuToBIFileReader(filename, "UTF16");
    } else {
      file_reader = new AuToBIFileReader(filename, charsetName);
    }

    Tier tier;
    readTextGridTier(file_reader);  // Remove TextGrid header
    tier = readTextGridTier(file_reader);
    while (tier != null && tier.getName() != null) {
      if (words_tier_name != null) {
        if (tier.getName().equals(words_tier_name)) {
          words_tier = tier;
        }
      } else if (tier.getName() != null && tier.getName().equals("words")) {
        words_tier = tier;
      }

      if (prominence_tier_name != null) {
        if (tier.getName().equals(prominence_tier_name)) {
          prominence_tier = tier;
        }
      } else if (tier.getName() != null && tier.getName().equals("delivery")) {
        prominence_tier = tier;
      }
      tier = readTextGridTier(file_reader);
    }

    List<Word> words = generateWordList(words_tier.getRegions());

    if (prominence_tier != null) {
      copyProminenceAnnotations(words, prominence_tier.getRegions());
    }

    return words;
  }

  /**
   * Copies prominence annotations from regions with prominence labels to words.
   *
   * @param words      the word regions
   * @param prominence the prominence regions.
   */
  protected void copyProminenceAnnotations(List<Word> words, List<Region> prominence) {
    int j = 0;
    for (Word word : words) {
      while (j < prominence.size() && prominence.get(j).getEnd() < word.getStart()) {
        ++j;
      }

      String pattern = "P";
      if (include_secondary) {
        pattern = "(P|p)";
      }
      word.setAccent(null);
      while (j < prominence.size() && prominence.get(j).getEnd() <= word.getEnd()) {
        if (prominence.get(j).getLabel() != null && prominence.get(j).getLabel().matches(pattern)) {
          word.setAccent("X*?");
        }
        j++;
      }
    }
  }
}
