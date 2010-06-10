/*  AlignmentUtils.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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

import java.util.List;

/**
 * Utility class for aligning information to Regions.
 * <p/>
 * This is used for aligning annotations as well as acoustic and other information to regions.
 */
public class AlignmentUtils {

  /**
   * Copies the ToBI tones to the words.
   *
   * @param words tones
   * @param tones words
   */
  public static void copyToBITones(List<Word> words, List<Region> tones) {
    int word_idx = 0;
    int tone_idx = 0;

    while (tone_idx < tones.size() && word_idx < words.size()) {

      Region tone = tones.get(tone_idx);
      Word word = words.get(word_idx);

      if (tone.getEnd() < word.getStart()) {
        tone_idx++;
      } else if (word.getEnd() < tone.getStart()) {
        word_idx++;
      } else {
        // Assign tones to word
        String[] tone_data = ToBIUtils.parseToneString(tone.getLabel());

        if (tone_data[0] != null) {
          if (word.isAccented()) {
            AuToBIUtils.warn("Multiply accented word, " + word + ". Only keeping the first accent.");
          } else {
            word.setAccent(tone_data[0]);
            word.setAccentTime(tone.getStart());
          }
        }

        if (tone_data[1] != null) {
          if (word.hasPhraseAccent()) {
            AuToBIUtils.warn("Word, " + word + ", contains two phrase accents. Only keeping the first accent.");
          } else {
            word.setPhraseAccent(tone_data[1]);
          }
        }

        if (tone_data[2] != null) {
          if (word.hasBoundaryTone()) {
            AuToBIUtils.warn("Word, " + word + ", contains two boundary tones. Only keeping the first accent.");
          } else {
            word.setBoundaryTone(tone_data[2]);
          }
        }
        tone_idx++;
      }
    }

    if (tone_idx != tones.size()) {
      AuToBIUtils.warn("Tones were present after the end of the words. These have not been aligned to any data.");
    }

  }

  /**
   * Copies a list of breaks to associated words.
   * <p/>
   * Requires that the number of breaks and words are equal for alignment.  This can cause a problem for some
   * annotations which (erroneously) label silence with a break index.
   *
   * @param words The list of words
   * @param breaks The list of breaks
   * @throws AuToBIException If there is an unqual number of breaks and words 
   */
  public static void copyToBIBreaks(List<Word> words, List<Region> breaks) throws AuToBIException {
    String previous_break = null;

    if (words.size() != breaks.size()) {
      throw new AuToBIException("Unequal number of breaks and words.");
    }
    for (int i = 0; i < words.size(); ++i) {
      Word w = words.get(i);
      w.setBreakBefore(previous_break);
      String current_break = breaks.get(i).getLabel();
      w.setBreakAfter(current_break);
      previous_break = current_break;
    }
  }
}
