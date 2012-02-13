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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
  public static void copyToBITonesByTime(List<Word> words, List<Region> tones) {
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
        
        if ((tone_data[0] == null) && (tone_data[1] == null) && (tone_data[2] == null)) {
			AuToBIUtils.warn("Label, " + tone.getLabel()
					+ ", doesn't match any pattern (accent_pattern, phrase_accent_pattern, boundary_tone_pattern).");
		}

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
   * @param words  The list of words
   * @param breaks The list of breaks
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          If there is an unqual number of breaks and words
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

  /**
   * Copies a list of ToBI tones to words based on their index.
   * <p/>
   * That is, the i-th phrase ending tone is aligned to the i-th phrase, regardless of the annotation time.
   * <p/>
   * Accents are aligned to words based on time.
   *
   * @param words the list of words
   * @param tones the list of tones
   * @throws AuToBIException If there is an alignment problem
   */
  public static void copyToBITonesByIndex(List<Word> words, List<Region> tones) throws AuToBIException {
    List<String> phrase_accents = new ArrayList<String>();
    List<String> boundary_tones = new ArrayList<String>();

    ListIterator<Region> toneIter = tones.listIterator();
    for (Word word : words) {
      // Gets the next tone region within the end point of the word.
      Region toneRegion = getNextRegionBeforeTime(word.getEnd(), toneIter);

      while (toneRegion != null) {
        if (toneRegion.getLabel().equals(""))
          throw new AuToBIException(toneRegion + " contains an empty tone.");

        // Common idiosyncracies in the Boston University Radio News Corpus, that are not in the ToBI standard.
        if (toneRegion.getLabel().equals("X%?") || toneRegion.getLabel().equals("%?")) {
          toneRegion.setLabel("X-?X%?");
        }
        if (toneRegion.getLabel().equals("-X?")) {
          toneRegion.setLabel("X-?");
        }

        String tone = ToBIUtils.getPitchAccent(toneRegion.getLabel());
        if (tone == null) {
          String phraseAccent = ToBIUtils.getPhraseAccent(toneRegion.getLabel());
          String boundaryTone = ToBIUtils.getBoundaryTone(toneRegion.getLabel());

          if (phraseAccent != null) {
            phrase_accents.add(phraseAccent);
          }
          if (boundaryTone != null) {
            boundary_tones.add(boundaryTone);
          }
        } else {
          word.setAccent(tone);
          word.setAccentTime(toneRegion.getStart());
        }

        toneRegion = getNextRegionBeforeTime(word.getEnd(), toneIter);
      }
    }

    // Phrase ending tones do not align in time, but there are the right amount of them
    // So align by index rather than by time.

    // Retrieve any remaining phrase final tones
    while (toneIter.hasNext()) {
      Region toneRegion = toneIter.next();
      String phraseAccent = ToBIUtils.getPhraseAccent(toneRegion.getLabel());
      String boundaryTone = ToBIUtils.getBoundaryTone(toneRegion.getLabel());

      if (phraseAccent != null) {
        phrase_accents.add(phraseAccent);
      }
      if (boundaryTone != null) {
        boundary_tones.add(boundaryTone);
      }
    }

    ListIterator<String> phrase_accent_iter = phrase_accents.listIterator();
    ListIterator<String> boundary_tone_iter = boundary_tones.listIterator();

    for (Word word : words) {
      if (word.getBreakAfter().matches("(3|3-|3p|4|4-|4p)")) {
        if (!phrase_accent_iter.hasNext()) {
          throw new AuToBIException("No available phrase accent for phrase final word: " + word);
        } else {
          word.setPhraseAccent(phrase_accent_iter.next());
        }
      }
      if (word.getBreakAfter().matches("(4|4-|4p)")) {
        if (!boundary_tone_iter.hasNext()) {
          throw new AuToBIException("No available boundary tone for phrase final word: " + word);
        } else {
          word.setBoundaryTone(boundary_tone_iter.next());
        }
      }
    }
  }


  /**
   * Retrieves the next region in the list following a particular time.
   * <p/>
   * Returns null if there is no additional region before the time.
   *
   * @param time the time
   * @param iter the list iterator
   * @return the first region that starts after the time.
   */
  protected static Region getNextRegionBeforeTime(double time, ListIterator<Region> iter) {
    if (!iter.hasNext()) return null;

    Double epsilon = 0.005;// To deal with Double precision errors.
    Region region = iter.next();
    if (region.getStart() > time + epsilon) {
      iter.previous();
      return null;
    }

    return region;
  }
}
