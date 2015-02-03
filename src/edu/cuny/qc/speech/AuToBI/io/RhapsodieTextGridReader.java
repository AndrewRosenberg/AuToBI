/* RhapsodieTextGridReader.java

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
 * A TextGridReader to process the Rhapsodie format of ToBI-like intonation.
 */
public class RhapsodieTextGridReader extends TextGridReader {

  private String tone_tier_name; // the name of the tone tier.
  private Tier tone_tier;        // A tier to store tone annotations
  private Tier rhap_words_tier;  // the words tier from rhapsodie annotation

  public RhapsodieTextGridReader(String filename, String words_tier, String tone_tier, String charset) {
    super(filename, words_tier, null, null, charset);
    this.tone_tier_name = tone_tier;
  }

  /**
   * Generates a list of syllables from the associated TextGrid file.
   * <p/>
   * A list of syllables is generated, available ToBI information is aligned to them,
   * and checked for consistency with the
   * standard.
   * <p/>
   * This is the main entry point for this class.
   * <p/>
   * Typical Usage:
   * <p/>
   * TextGridReader reader = new TextGridReader(filename) List<Words> data_points = reader.readWords();
   *
   * @return A list of syllables with from the TextGrid
   * @throws java.io.IOException                            if there is a reader problem
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException if there is an alignment problem
   */
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader file_reader;
    if (charsetName == null) {
      file_reader = new AuToBIFileReader(filename);
    } else {
      file_reader = new AuToBIFileReader(filename, charsetName);
    }

    readTiersWithReader(file_reader);

    if (words_tier == null) {  // Didn't find a words tier try a different file encoding
      if (charsetName == null) {  // tried ASCII last time, try UTF16 now
        file_reader = new AuToBIFileReader(filename, "UTF16");
      } else {  // a non-ascii charset was specified, but maybe the file is ascii
        file_reader = new AuToBIFileReader(filename);
      }
      readTiersWithReader(file_reader);
    }

    if (words_tier == null) {
      throw new AuToBIException(filename + " - Didn't find a words tier named: " + words_tier_name);
    }

    List<Word> syllables = generateWordList(words_tier.getRegions());

    if (tone_tier != null) {
      constructToBILikeAnnotations(syllables, tone_tier.getRegions());
    }

    // Annotate word final state
    annotateWordFinal(syllables, rhap_words_tier.getRegions());

    // Merge word final schwas with previous syllables (make sure to take the union of the tone annotations
    // expid: SchwaCollapse
//    mergeFinalSchwas(syllables);

    // expid: SchwaIgnore
//    ignoreFinalSchwas(syllables);

    return syllables;
  }

  /**
   * When identifying the final syllable in a word, ignore any final schwa, considering the penultimate syllable to
   * be 'final'.
   *
   * @param syllables the syllables to analyze.
   */
  private void ignoreFinalSchwas(List<Word> syllables) {
    int si = 1;
    while (si < syllables.size()) {
      Word s = syllables.get(si);
      if (s.getLabel().endsWith("@") && s.getAttribute("word_final").equals("true")) {
        Word s_prev = syllables.get(si - 1);

        s_prev.setAttribute("word_final", "true");
        s.setAttribute("word_final", "false");

        s_prev.setAccent(s_prev.getAccent() + s.getAccent());

        if (s.getBoundaryTone() == null) {
          s.setBoundaryTone("");
        }
        if (s_prev.getBoundaryTone() == null) {
          s_prev.setBoundaryTone("");
        }
        s_prev.setBoundaryTone(s_prev.getBoundaryTone().replace("%", "") + s.getBoundaryTone().replace("%", "") + "%");

        s.setAccent("");
        s.setBoundaryTone("");
        s.setAttribute("__ignore__", true);
      }
      si++;
    }
  }

  /**
   * Merge syllables that end a word with a schwa to the previous syllable.  These are prosodically neutral,
   * serving mostly to displace the boundary of the word from prosodic boundary.
   *
   * @param syllables the syllables to merge.
   */
  private void mergeFinalSchwas(List<Word> syllables) {
    int si = 0;
    while (si < syllables.size()) {
      Word s = syllables.get(si);
      if (s.getLabel().endsWith("@") && s.getAttribute("word_final").equals("true")) {
        Word s_prev = syllables.get(si - 1);

        // copy label and annotation.
        s_prev.setLabel(s_prev.getLabel() + "_" + s.getLabel());
        s_prev.setAttribute("word_final", "true");
        s_prev.setAccent(s_prev.getAccent() + s.getAccent());

        s.setBoundaryTone("");
        s_prev.setBoundaryTone("");
        s_prev.setBoundaryTone(s_prev.getBoundaryTone().replace("%", "") + s.getBoundaryTone().replace("%", "") + "%");

        // delete si
        syllables.remove(si);
      } else {
        si++;
      }
    }
  }

  private void annotateWordFinal(List<Word> syllables, List<Region> words) {
    double eps = 0.00001;
    int wi = 0;
    int si = 0;

    while (wi < words.size()) {
      Region r = words.get(wi);

      while (si < syllables.size() && syllables.get(si).getEnd() < r.getEnd()) {
        syllables.get(si).setAttribute("word_final", "false");
        si++;
      }

      // Approximate equality
      if (si < syllables.size() && Math.abs(syllables.get(si).getEnd() - r.getEnd()) < eps) {
        syllables.get(si).setAttribute("word_final", "true");
        si++;
      }
      wi++;
    }
  }

  /**
   * Reads words_tier and tones_tier from an initialized AuToBIFileReader
   *
   * @param file_reader the reader
   * @throws IOException                  if there's a problem with the specified file
   * @throws TextGridSyntaxErrorException if there is a textgrid syntax problem
   */
  private void readTiersWithReader(AuToBIFileReader file_reader) throws IOException, TextGridSyntaxErrorException {
    Tier tier;
    readTextGridTier(file_reader);  // Remove TextGrid header
    tier = readTextGridTier(file_reader);
    while (tier != null && tier.getName() != null) {
      if (words_tier_name != null) {
        if (tier.getName().equals(words_tier_name)) {
          words_tier = tier;
        }
      } else if (tier.getName().equals("syllabe")) {
        words_tier = tier;
      }

      if (tier.getName().equals("word")) {
        rhap_words_tier = tier;
      }

      if (tone_tier_name != null) {
        if (tier.getName().equals(tone_tier_name)) {
          tone_tier = tier;
        }
      } else if (tier.getName() != null && tier.getName().equals("pseudo-ToBI")) {
        tone_tier = tier;
      }
      tier = readTextGridTier(file_reader);
    }
  }

  /**
   * Copies annotations from regions with prominence labels to words.
   *
   * @param words the word regions
   * @param tones the tobi-like regions.
   */
  protected void constructToBILikeAnnotations(List<Word> words, List<Region> tones) throws AuToBIException {
    int j = 0;
    for (Word word : words) {
      while (j < tones.size() && tones.get(j).getEnd() < word.getStart()) {
        ++j;
      }

      String tone = tones.get(j).getLabel();
      if (tone.contains("i")) {
        word.setAccent(tone);
      } else {

        if (tone.equals("%") || tone.equals("EX")) {
          word.setAttribute("__ignore__", true);
        }

        if (tone.contains("*")) {
          word.setBoundaryTone(tone);
          word.setBreakAfter("4");

          if (!tone.contains("%")) {  // displaced phrase ending. Make sure that the ending boundary is found.
            word.setBoundaryTone(tone + "%");
            String next_tone = "";
            int i = j + 1;
            while (i < tones.size() && next_tone.isEmpty()) {
              next_tone = tones.get(i++).getLabel();
            }
            if (i == tones.size()) {
              throw new AuToBIException("Phrase tone not ended before the end of the file.");
            }
            if (!next_tone.equals("%")) {
              throw new AuToBIException(
                  filename + ": Phrase tone, " + tone + " (" + i + "), not ended by \"%\" token.");
            }
          }
        }
      }
    }
  }
}
