/*  SyllableFeatureExtractor.java

    Copyright 2010-2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.AuToBIFileReader;
import edu.cuny.qc.speech.AuToBI.util.PhoneUtils;

import java.io.IOException;
import java.util.*;

/**
 * SyllableFeatureExtractor constructs syllable regions for each word based on phone regions and a pronunciation
 * dictionary.
 * <p/>
 * The pronunciation dictionary that is disseminated with the Boston University Radio News Corpus is inconsistent with
 * the forced alignment phones included with the corpus.  Therefore, the syllable regions are constructed by aligning
 * the pronunciation of a word with its phone string.  This alignment is incurs zero cost for alignment of identical
 * phone strings and alignment of vowels -- where vowels contain "A", "E", "I", "O", "U"/ If the pronunciation contains
 * the same phone set, this will be an exact match.  If they don't, this will give a best effort alignment between the
 * two.
 */
@SuppressWarnings("unchecked")
public class SyllableFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "syls";
  private String syllable_feature;         // The name of the feature to store syllables on.
  private String phone_feature;            // The name of the feature containing a list of phone regions.
  private HashMap<String, HashSet<String>> lexicon; // The pronunciation dictionary

  enum trace_symbol {
    INS, SUB, DEL
  }  // The symbols required for the dynamic programming trace.

  /**
   * Constructs a new SyllableFeatureExtractor, and reads an appropriate lexicon.
   * <p/>
   * The lexicon is expected to be a comma separated containing two fields, the word, and its pronunciation, where the
   * pronunciation is a whitespace separated list of phones, where syllable boundaries are indicated by whitespace
   * separated asterisks '*'.
   * <p/>
   * This is an example line from a properly formatted lexicon:
   * <p/>
   * abandonment, ax * b ae+1 n * d ax n * m ax n t
   *
   * @param syllable_feature the name of the feature to store the syllable
   * @param phone_feature    the name of the feature containing phone boundaries
   * @param lexicon_filename the filename containing the lexicon.
   * @throws java.io.IOException if there is a problem reading the lexicon
   */
  @Deprecated
  public SyllableFeatureExtractor(String syllable_feature, String phone_feature, String lexicon_filename)
      throws IOException {
    this.syllable_feature = syllable_feature;
    this.phone_feature = phone_feature;
    readLexicon(lexicon_filename);

    this.required_features.add(phone_feature);
    this.extracted_features.add(syllable_feature);
  }

  public SyllableFeatureExtractor(String phone_feature, String lexicon_filename)
      throws IOException {
    this.syllable_feature = moniker;
    this.phone_feature = phone_feature;
    readLexicon(lexicon_filename);

    this.required_features.add(phone_feature);
    this.extracted_features.add(syllable_feature);
  }

  /**
   * Loads a lexicon file into a hashmap associating a word to its possible pronunciations.
   *
   * @param filename the lexicon filename.
   * @throws IOException if there is a problem with reading the file.
   */
  private void readLexicon(String filename) throws IOException {
    AuToBIFileReader reader = new AuToBIFileReader(filename);

    lexicon = new HashMap<String, HashSet<String>>();
    String line;
    while ((line = reader.readLine()) != null) {
      String[] data = line.split(",");
      if (!lexicon.containsKey(data[0])) {
        lexicon.put(lexiconKey(data[0]), new HashSet<String>());
      }
      lexicon.get(lexiconKey(data[0])).add(data[1]);
    }
    reader.close();
  }

  /**
   * Constructs syllables for each word based on the syllabification described in the lexicon.
   * <p/>
   * If there is no available pronunciation for a word, this feature extraction routine defaults to C*V syllable
   * boundaries for word internal syllables and C*VC* for word final syllables.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem with the feature extraction
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Word w : (List<Word>) regions) {
      List<SubWord> syllables;
      if (lexicon.containsKey(lexiconKey(w.getLabel()))) {
        try {
          syllables =
              getBestSyllabification(lexicon.get(lexiconKey(w.getLabel())),
                  (List<Region>) w.getAttribute(phone_feature));
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      } else {
        // if we have no pronunciation of the word, just assume C*V syllable structure.
        syllables = constructCVSyllables((List<Region>) w.getAttribute(phone_feature));
      }
      w.setAttribute(syllable_feature, syllables);
    }
  }


  /**
   * Constructs a list of syllables based on a list of phones assuming a C*V syllable structure for word internal
   * syllables, and a C*VC* syllable structure for word final phones.
   *
   * @param phones a list of phones
   * @return a list of syllables
   */
  private List<SubWord> constructCVSyllables(List<Region> phones) {
    List<SubWord> syllables = new ArrayList<SubWord>();

    SubWord current_syllable = new SubWord(0.0, 0.0, "");
    List<Region> current_phones = new ArrayList<Region>();
    for (Region phone : phones) {
      current_phones.add(phone);

      if (PhoneUtils.isVowel(phone.getLabel())) {
        // Vowels end syllables. Finish the syllable and start a new one.
        current_syllable.setStart(current_phones.get(0).getStart());
        current_syllable.setEnd(current_phones.get(current_phones.size() - 1).getEnd());
        String syl_label = "";
        for (Region p : current_phones) {
          syl_label += p.getLabel();
        }
        current_syllable.setLabel(syl_label);
        current_syllable.setAttribute(phone_feature, current_phones);
        syllables.add(current_syllable);

        current_syllable = new SubWord(0.0, 0.0, "");
        current_phones = new ArrayList<Region>();
      }
    }

    if (current_phones.size() > 0) {
      // Add trailing consonants to the end of the last syllable.
      current_syllable = syllables.get(syllables.size() - 1);
      current_syllable.setEnd(current_phones.get(current_phones.size() - 1).getEnd());
      ((List<Region>) current_syllable.getAttribute(phone_feature)).addAll(current_phones);
      String syl_label = "";
      for (Region p : (List<Region>) current_syllable.getAttribute(phone_feature)) {
        syl_label += p.getLabel();
      }
      current_syllable.setLabel(syl_label);
    }
    return syllables;
  }


  /**
   * Given a list of phones and a set of syllabifications, construct syllable regions with appropriate durations and
   * labels.
   * <p/>
   * Precondition: syllabification represents a syllabification of the list of phones.
   * <p/>
   * The format of syllabification is:
   * <p/>
   * phone0 phone 1 * phone 2 ... * phoneN-1 phoneN
   * <p/>
   * phoneX is a phone label, the asterisk (*) character represents a syllable boundary.
   *
   * @param phones           The phones that make up the syllable string
   * @param syllabifications The candidate syllabifications of the phones
   * @return A list of syllable regions
   * @throws AuToBIException If the phone data does not align with the syllabification
   */
  private List<SubWord> getBestSyllabification(Set<String> syllabifications, List<Region> phones)
      throws AuToBIException {
    String[] best_syl_data = null;
    trace_symbol[][] best_trace = null;
    Double best_edit_distance = Double.MAX_VALUE;
    for (String syllabification : syllabifications) {
      String[] syl_data = syllabification.split("\\s");

      Double[][] dp = new Double[phones.size() + 1][syl_data.length + 1];
      trace_symbol[][] trace = new trace_symbol[phones.size() + 1][syl_data.length + 1];
      final int INS_COST = 1;
      final int DEL_COST = 1;
      for (int i = 0; i < phones.size() + 1; ++i) {
        for (int j = 0; j < syl_data.length + 1; ++j) {
          if ((i == 0) && (j == 0)) {
            dp[i][j] = 0.0;
            trace[i][j] = null;
          } else {
            Double SUB_COST = Double.MAX_VALUE;
            if (j > 0 && i > 0) {
              SUB_COST = subCost(syl_data[j - 1], phones.get(i - 1).getLabel());
            }
            if (j == 0) {
              dp[i][j] = dp[i - 1][j] + DEL_COST;
              trace[i][j] = trace_symbol.DEL;
            } else if (syl_data[j - 1].matches("(\\*|\\|)")) {
              dp[i][j] = dp[i][j - 1];
              trace[i][j] = trace_symbol.INS;
            } else if (i == 0) {
              dp[i][j] = dp[i][j - 1] + INS_COST;
              trace[i][j] = trace_symbol.INS;
            } else if ((dp[i - 1][j] + DEL_COST < dp[i - 1][j - 1] + SUB_COST) &&
                (dp[i - 1][j] + DEL_COST < dp[i][j - 1] + INS_COST)) {
              dp[i][j] = dp[i - 1][j] + DEL_COST;
              trace[i][j] = trace_symbol.DEL;
            } else if ((dp[i][j - 1] + INS_COST < dp[i - 1][j - 1] + SUB_COST) &&
                (dp[i][j - 1] + INS_COST < dp[i - 1][j] + DEL_COST)) {
              dp[i][j] = dp[i][j - 1] + INS_COST;
              trace[i][j] = trace_symbol.INS;
            } else {
              dp[i][j] = dp[i - 1][j - 1] + SUB_COST;
              trace[i][j] = trace_symbol.SUB;
            }
          }
        }
      }

      if (dp[phones.size()][syl_data.length] < best_edit_distance) {
        best_edit_distance = dp[phones.size()][syl_data.length];
        best_syl_data = syl_data;
        best_trace = trace;
      }
    }

    ArrayList<SubWord> syllables = new ArrayList<SubWord>();
    SubWord syllable = null;
    int i = phones.size();
    int j = best_syl_data.length;
    boolean stress_set = false;
    while (best_trace[i][j] != null) {
      if ((j > 0) && best_syl_data[j - 1].matches("(\\*|\\|)")) {
        if (syllable != null) {
          syllables.add(0, syllable);
        }
        syllable = null;
      } else {
        if (i > 0) {
          if (best_trace[i][j] == trace_symbol.SUB || best_trace[i][j] == trace_symbol.DEL) {
            Region p = phones.get(i - 1);
            if (syllable == null) {
              syllable = new SubWord(p.getStart(), p.getEnd(), p.getLabel());
              syllable.setAttribute(this.phone_feature, new ArrayList<Region>());
            } else {
              syllable.setStart(p.getStart());
              syllable.setLabel(p.getLabel() + syllable.getLabel());
              ((List<Region>) syllable.getAttribute(this.phone_feature)).add(0, p);
            }
          }
          if ((j > 0) && (best_syl_data[j - 1].endsWith("+1") && syllable != null) && !stress_set) {
            syllable.setAttribute("lexical_stress", true);
            stress_set = true;
          }
        }
      }

      switch (best_trace[i][j]) {
        case DEL:
          i = i - 1;
          break;
        case INS:
          j = j - 1;
          break;
        case SUB:
          i = i - 1;
          j = j - 1;
      }
    }
    if ((syllable != null) && (syllable.getLabel().length() > 0)) {
      syllables.add(0, syllable);
    }
    if (!stress_set) {
      syllables.get(0).setAttribute("lexical_stress", true);
    }

    return syllables;
  }


  /**
   * Determines the substitution cost between two phones.
   * <p/>
   * Substituting one vowel for another incurs zero cost.
   * <p/>
   * Substituting one consonant for another incurs a cost of one.
   * <p/>
   * Vowel/Consonant substitutions incur a cost of 20.
   *
   * @param phone1 the first phone
   * @param phone2 the second phone
   * @return the substitution cost
   */
  private static Double subCost(String phone1, String phone2) {
    if (phone1.equalsIgnoreCase(phone2)) return 0.0;
    if (PhoneUtils.isVowel(phone1) && PhoneUtils.isVowel(phone2)) return 0.0;
    if (!PhoneUtils.isVowel(phone1) && !PhoneUtils.isVowel(phone2)) return 1.0;
    // Severely penalize consonant vowel matching.
    return 20.0;
  }


  /**
   * Cleans a string for indexing into the lexicon.
   *
   * @param s the string
   * @return the cleaned string.
   */
  private String lexiconKey(String s) {
    return s.replaceAll("/.*", "").replaceAll("\\-*$", "").replaceAll("[\\}\\{]", "").toUpperCase();
  }
}
