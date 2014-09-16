/* KOHReader.java

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
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KOHReader extends AuToBIWordReader {

  private String filename;

  public KOHReader(String filename) {
    this.filename = filename;
  }

  /**
   * Reads word boundaries and tobi labels from a koh formatted file.
   * <p/>
   * (1)occ_start_time (2)leaf_name (3)occ_index (4)context (5)vec_num_of_start (6)vec_num_of_end (7)occ_steps
   * (8)start_pitch (9)end_pitch (10)dur_leaf_name (11)en_leaf_name (12)lexeme (13)syllable (14)pitch accent
   * (15)HiF0_val (16)phrase accent and boundary tone (17)break index
   *
   * @return a list of words
   * @throws IOException     if there is a problem with the file reading
   * @throws AuToBIException if there is a problem with the tone alignment
   */
  @Override
  public List<Word> readWords() throws IOException, AuToBIException {

    AuToBIFileReader reader = new AuToBIFileReader(filename);

    List<Word> words = new ArrayList<Word>();
    String line;
    String prev_break_idx = null;
    while ((line = reader.readLine()) != null) {
      String data[] = line.trim().split(";");

      if (data.length == 17) {
        if (!data[11].trim().startsWith(".") && !data[11].trim().startsWith("~")) {
          String accent = data[13].trim();
          if (!accent.startsWith(".")) {
            accent = null;
          }
          String phrase_accent = data[15].trim();
          String break_idx = data[16].trim();
          //  strip the (\d+) out of the ortho label.
          String word = data[11].trim();
          word = word.substring(0, word.indexOf("("));
          Word w = new Word(0.0, 0.0, word, accent, filename);
          if (!phrase_accent.equals(".") && phrase_accent.contains("-")) {
            String phrase_accent_label = phrase_accent.substring(0, phrase_accent.indexOf("-") + 1);
            w.setPhraseAccent(phrase_accent_label.trim());

            if (phrase_accent.contains("%")) {
              String boundary_tone = phrase_accent.substring(phrase_accent.indexOf("-") + 1, phrase_accent.length());
              if (boundary_tone.trim().length() > 0) {
                w.setBoundaryTone(boundary_tone.trim());
              }
            }
          }
          w.setBreakAfter(break_idx);
          w.setBreakBefore(prev_break_idx);
          words.add(w);
          prev_break_idx = break_idx;
        }
      }
    }

    return words;
  }
}
