/*  SubregionUtils.java

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
package edu.cuny.qc.speech.AuToBI;

import java.util.List;

/**
 * SubregionUtils is a utility class to house static functions for the processing of subregions.
 */
public class SubregionUtils {

  /**
   * Aligns the subregion that is identified within a word to be the representative pseudosyllable.
   * <p/>
   * We say that the subregion that covers the longest duration of a word to be representative.
   * <p/>
   * This is an approximation of the accent bearing syllable, and may introduce errors.
   * <p/>
   * This is used for pitch accent type classification.
   * <p/>
   * NOTE: if pseudosyllables are very long relative to the length of words, the same pseudosyllable can be assigned to
   * the same word.
   *
   * @param words        the words
   * @param subregions   the pseudosyllables
   * @param feature_name the feature name for storing the selected pseudosyllable region
   */
  public static void alignLongestSubregionsToWords(List<Word> words, List<Region> subregions,
                                                   String feature_name) {
    int i = 0;
    for (Word w : words) {
      if (i >= subregions.size())
        i = subregions.size() - 1;
      Region current_pseudosyllable = subregions.get(i);
      if (current_pseudosyllable.getStart() > w.getStart() && i > 0) {
        current_pseudosyllable = subregions.get(--i);
      }

      while (current_pseudosyllable.getEnd() < w.getStart()) {
        ++i;
        current_pseudosyllable = subregions.get(i);
      }

      Region best_pseudosyllable = current_pseudosyllable;
      double max_overlap = -Double.MAX_VALUE;
      while (current_pseudosyllable.getStart() < w.getEnd() && i < subregions.size()) {
        // Calculate the amount of overlapping material
        double overlap = Math.min(w.getEnd(), current_pseudosyllable.getEnd()) -
                         Math.max(w.getStart(), current_pseudosyllable.getStart());
        if (overlap > max_overlap) {
          max_overlap = overlap;
          best_pseudosyllable = current_pseudosyllable;
        }
        ++i;
        if (i < subregions.size())
          current_pseudosyllable = subregions.get(i);
      }
      w.setAttribute(feature_name, best_pseudosyllable);
    }
  }

  /**
   * Given a string describing the subregion length, return the desired number of seconds.
   * <p/>
   * Currently only parses seconds and miliseconds.
   * <p/>
   * 400ms
   * 1s
   *
   * @param subregion_name the name of the subregion to parse.
   * @return the number of seconds described by the string
   * @throws FeatureExtractorException if the subregion label is unparseable
   */
  public static Double parseSubregionName(String subregion_name) throws FeatureExtractorException {

    if (!subregion_name.matches("^\\d+(ms|s)$")) {
      throw new FeatureExtractorException("Cannot parse the subregion: " + subregion_name);
    }

    boolean milliseconds = false;
    if (subregion_name.matches(".*ms$")) {
      milliseconds = true;
    }

    String number;
    if (milliseconds) {
      number = subregion_name.substring(0, subregion_name.indexOf('m'));
      return Double.parseDouble(number) / 1000;
    } else {
      number = subregion_name.substring(0, subregion_name.indexOf('s'));
      return Double.parseDouble(number);
    }
  }
}
