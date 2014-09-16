/*  ToBIUtils.java

    Copyright 2009-2014 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * ToBIUtils is a utility class to store static methods used in the processing and interpretation of ToBI annotations,
 * specifically breaks and tones.
 * <p/>
 * Note: It may be preferred to fold many of these functions into the FeatureExtractor framework.  Currently they remain
 * as static methods, some of which contain FeatureExtractor wrappers.  This is a likely future modification.
 */
public class ToBIUtils {
  /**
   * Constructs an intermediate phrase boundary annotation, either NONBOUNDARY, INTERMEDIATE_BOUNDARY or
   * INTONATIONAL_BOUNDARY based on analysis of associated breaks information
   *
   * @param data_points    the words to analyse
   * @param attribute_name the destination attribute
   */
  public static void setIntermediatePhraseBoundary(List<Word> data_points, String attribute_name) {
    for (Word word : data_points) {
      String label = "NONBOUNDARY";
      if (word.getBreakAfter() != null && (
          word.getBreakAfter().equals("3") || word.getBreakAfter().equals("3p") ||
              word.getBreakAfter().equals("3-"))) {
        label = "INTERMEDIATE_BOUNDARY";
      }
      if ((word.getBreakAfter() != null) && (
          word.getBreakAfter().equals("4-") || word.getBreakAfter().equals("4"))) {
        label = "INTONATIONAL_BOUNDARY";
      }
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Constructs an intonational phrase boundary annotation, either NONBOUNDARY or INTONATIONAL_BOUNDARY based on
   * analysis of associated breaks information
   *
   * @param data_points    the words to analyse
   * @param attribute_name the destination attribute
   */

  public static void setIntonationalPhraseBoundary(List<Word> data_points, String attribute_name) {
    for (Word word : data_points) {
      String label = "NONBOUNDARY";
      if ((word.getBreakAfter() != null) && (
          word.getBreakAfter().equals("4-") || word.getBreakAfter().equals("4"))) {
        label = "INTONATIONAL_BOUNDARY";
      }
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Inserts annotated phrase accent and boundary tone pair as an attribute.
   *
   * @param data_points    The regions to set attributes on
   * @param attribute_name The assigned attribute name
   */
  public static void setPhraseAccentBoundaryTone(List<Word> data_points, String attribute_name) {
    for (Word word : data_points) {
      String label = "NOTONE";
      if (word.getBoundaryTone() != null) label = word.getPhraseAccent() + word.getBoundaryTone().replace('%', 'x');
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Assigns the annotated phrase accent as an attribute.
   *
   * @param words          the words to analyse.
   * @param attribute_name the destination attribute name
   */
  public static void setPhraseAccent(List<Word> words, String attribute_name) {
    for (Word word : words) {
      String label = "NOTONE";
      if (word.getPhraseAccent() != null) label = word.getPhraseAccent();
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Assigns the annotated pitch accent type as an attribute.
   *
   * @param words          the words to analyse.
   * @param attribute_name the destination attribute name
   */
  public static void setPitchAccentType(List<Word> words, String attribute_name) {
    for (Word word : words) {
      String label = "NOACCENT";
      if (word.getAccent() != null) label = word.getAccent();
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Insert a boolean attribute to each Region corresponding to whether it is accented or not.
   *
   * @param data_points    The regions to set attributes on
   * @param attribute_name The assigned attribute name
   */
  public static void setPitchAccent(List<Word> data_points, String attribute_name) {
    for (Word word : data_points) {
      String label = "DEACCENTED";
      if (word.getAccent() != null) if (word.getAccent().length() > 0) label = "ACCENTED";
      word.setAttribute(attribute_name, label);
    }
  }

  /**
   * Generates break information from tones.
   * <p/>
   * Assumes that the tones have aready been assigned.
   *
   * @param words the set of words
   */
  public static void generateBreaksFromTones(List<Word> words) {
    String previous_break = "-1";
    for (Word word : words) {
      word.setBreakBefore(previous_break);
      if (word.hasBoundaryTone()) {
        word.setBreakAfter("4");
        previous_break = "4";
      } else if (word.hasPhraseAccent()) {
        word.setBreakAfter("3");
        previous_break = "3";
      } else {
        word.setBreakAfter("1");
        previous_break = "1";
      }
    }
  }

  /**
   * Generates default tonesinformation from breaks.
   * <p/>
   * Assumes that the breaks have already been assigned.  If some phrase ending tones have been assigned, these will not
   * be overwritten. However, inappropriate tones will be deleted -- for example, if the break is 1, and there is a
   * phrase accent, it will be deleted.
   *
   * @param words the set of words
   */
  public static void generateDefaultTonesFromBreaks(List<Word> words) {
    for (Word word : words) {
      if (word.getBreakAfter().startsWith("4")) {
        if (!word.hasBoundaryTone()) {
          word.setBoundaryTone("X%?");
        }
        if (!word.hasPhraseAccent()) {
          word.setPhraseAccent("X%-");
        }
      } else if (word.getBreakAfter().startsWith("3")) {
        if (word.hasBoundaryTone()) {
          word.setBoundaryTone(null);
        }
        if (!word.hasPhraseAccent()) {
          word.setPhraseAccent("X%-");
        }
      } else {
        if (word.hasBoundaryTone()) {
          word.setBoundaryTone(null);
        }
        if (word.hasPhraseAccent()) {
          word.setPhraseAccent(null);
        }
      }
    }
  }


  /**
   * Confirms that ToBI annotations are consistent with the ToBI standard.
   * <p/>
   * Checks that 1) every intermediate phrase has a phrase accent. 2) every intermediate phrase contains an accented
   * word. 3) every intonational phrase has a boundary tone
   *
   * @param words The words to check
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException when word annotations to not match the ToBI standard
   */
  public static void checkToBIAnnotations(List<Word> words) throws AuToBIException {
    Word starting_word = null;
    boolean end_of_phrase = false;
    boolean has_accented_word = false;
    for (Word word : words) {
      if (end_of_phrase) {
        end_of_phrase = false;
        starting_word = word;
        has_accented_word = false;
      }
      if (word.isIntermediatePhraseFinal() && !word.hasPhraseAccent()) {
        word.setPhraseAccent("X-?");
        AuToBIUtils.warn(
            "Word, " + word + ", has an intermediate phrase ending break, " + word.getBreakAfter() +
                ", but no phrase accent");
      }
      if (word.isIntonationalPhraseFinal() && !word.hasBoundaryTone()) {
        word.setBoundaryTone("X%?");
        AuToBIUtils.warn(
            "Word, " + word + ", has an intonational phrase ending break, " + word.getBreakAfter() +
                ", but no boundary tone");
      }

      if (!word.isIntermediatePhraseFinal() && word.hasPhraseAccent()) {
        throw new AuToBIException(
            "Word, " + word + ", has a phrase accent, " + word.getPhraseAccent() +
                ", but no intermediate phrase ending break, " + word.getBreakAfter());
      }
      if (!word.isIntonationalPhraseFinal() && word.hasBoundaryTone()) {
        throw new AuToBIException(
            "Word, " + word + ", has a boundary tone, " + word.getBoundaryTone() +
                ", but no intonational phrase ending break, " + word.getBreakAfter());
      }

      if (word.isAccented()) {
        has_accented_word = true;
      }
      if (word.isIntermediatePhraseFinal()) {
        end_of_phrase = true;
      }

      if (end_of_phrase && !has_accented_word) {
        AuToBIUtils.warn(
            "The intermediate phrase that started at the word, " + starting_word + ", and ended at the word, " + word +
                ", contains no pitch accent bearing word.");
      }
    }

  }

  /**
   * Parses available tone components from a tone label
   * <p/>
   * These are returned as an array containing 3 strings for the pitch accent, phrase acent, and boutary tone present
   * int helabel
   *
   * @param label the tone label to parse
   * @return an array with three pottential tone elements
   */
  public static String[] parseToneString(String label) {
    String[] tones = new String[3];

    Pattern accent_pattern =
        Pattern.compile(
            "(L\\+H\\*|L\\*\\+H|L\\*|H\\*|!H\\*|L\\+!H\\*|L\\*\\+!H|H\\+!H\\*|X\\*|\\(H\\+L\\)\\*|H\\+L\\*|H+L\\*|" +
                ".+\\+|\\+.+)");
    Matcher accent_matcher = accent_pattern.matcher(label);
    if (accent_matcher.find()) {
      tones[0] = accent_matcher.group();
    } else {
      tones[0] = null;
    }

    Pattern phrase_accent_pattern = Pattern.compile("!?[LHX]-\\??");
    Matcher phrase_accent_matcher = phrase_accent_pattern.matcher(label);
    if (phrase_accent_matcher.find()) {
      tones[1] = phrase_accent_matcher.group();
    } else {
      tones[1] = null;
    }

    Pattern boundary_tone_pattern = Pattern.compile("!?[LHX]%\\??");
    Matcher boundary_tone_matcher = boundary_tone_pattern.matcher(label);
    if (boundary_tone_matcher.find()) {
      tones[2] = boundary_tone_matcher.group();
    } else {
      tones[2] = null;
    }

    return tones;
  }

  /**
   * Parses a valid phrase accent from a tone label.
   *
   * @param label the tone label
   * @return the phrase accent or null if none exists
   */
  public static String getPhraseAccent(String label) {
    if (label.startsWith("L-") || label.startsWith("H-") || label.startsWith("!H-") || label.startsWith("X-?")) {
      return label.replaceAll("(.+?-\\??).*", "$1");
    }
    return null;
  }

  /**
   * Parses a valid boundary tone from a tone label.
   *
   * @param label the tone label
   * @return the boundary tone or null if none exists
   */
  public static String getBoundaryTone(String label) {
    if (label.endsWith("H%") || label.endsWith("L%") || label.endsWith("!H%") || label.endsWith("X%?")) {
      return label.replaceAll("(.*?)-\\??(.+?%)", "$2");
    }
    return null;
  }

  /**
   * Parses a valid pitch accent from a tone label.
   *
   * @param label the tone label
   * @return the pitch accent or null if none exists
   */
  public static String getPitchAccent(String label) {
    // Repair common non-standard accents.
    if (label.equals("X*") || label.equals("*?") || label.equals("*")) {
      label = ("X*?");
    }

    label = label.replace(";", "");
    label = label.replace(")", "");
    label = label.replace("(", "");

    if (label.contains("*")) {
      return label;
    } else {
      return null;
    }
  }
}
