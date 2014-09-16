/*  TextGridReader.java

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
package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.AlignmentUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.WordReaderUtils;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Read a TextGrid and generate a list of Words.
 * <p/>
 * The names of orthogonal, tones and breaks tiers in the TextGrid can be specified or standard "words", "tones",
 * "breaks" can be used.
 */
public class TextGridReader extends AuToBIWordReader {

  protected String filename;          // the name of the textgrid file
  protected String charsetName;  // the name of the character set of the file to read.

  protected String words_tier_name;   // the name of the words tier
  protected String tones_tier_name;   // the name of the tones tier
  protected String breaks_tier_name;  // the name of the breaks tier

  protected Tier words_tier;   // a words Tier object
  protected Tier tones_tier;   // a tones Tier object
  protected Tier breaks_tier;  // a breaks Tier object

  /**
   * Constructs a new TextGridReader for a TextGrid file with default tier names.
   *
   * @param filename the filename to read
   */
  public TextGridReader(String filename) {
    this.filename = filename;
  }

  /**
   * Constructs a new TextGridReader for a TextGrid file with default tier names.
   *
   * @param filename    the filename to read
   * @param charsetName the name of the character set for the input
   */
  public TextGridReader(String filename, String charsetName) {
    this.filename = filename;
    this.charsetName = charsetName;
  }

  /**
   * Constructs a new TextGridReader with specified file and tier names.
   *
   * @param filename         the file name
   * @param words_tier_name  the name of the orthogonal tier
   * @param tones_tier_name  the name of the tones tier
   * @param breaks_tier_name the name of the breaks tier
   */
  public TextGridReader(String filename, String words_tier_name, String tones_tier_name, String breaks_tier_name) {
    this.filename = filename;
    this.words_tier_name = words_tier_name;
    this.tones_tier_name = tones_tier_name;
    this.breaks_tier_name = breaks_tier_name;
  }

  /**
   * Constructs a new TextGridReader with specified file and tier names.
   *
   * @param filename         the file name
   * @param words_tier_name  the name of the orthogonal tier
   * @param tones_tier_name  the name of the tones tier
   * @param breaks_tier_name the name of the breaks tier
   * @param charsetName      the name of the character set for the input
   */
  public TextGridReader(String filename, String words_tier_name, String tones_tier_name, String breaks_tier_name,
                        String charsetName) {
    this.filename = filename;
    this.words_tier_name = words_tier_name;
    this.tones_tier_name = tones_tier_name;
    this.breaks_tier_name = breaks_tier_name;
    this.charsetName = charsetName;
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
   * @throws IOException                                    if there is a reader problem
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException if there is an alignment problem
   */
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader file_reader;
    if (charsetName != null) {
      file_reader = new AuToBIFileReader(filename, charsetName);
    } else {
      file_reader = new AuToBIFileReader(filename);
    }

    Tier tier;
    readTextGridTier(file_reader);  // Remove TextGrid header
    do {
      tier = readTextGridTier(file_reader);

      if (tier.name != null && words_tier_name != null) {
        if (tier.name.equals(words_tier_name)) {
          words_tier = tier;
        }
      } else if (tier.name != null && (tier.name.equals("words") || tier.name.equals("orthographic"))) {
        words_tier = tier;
      }

      if (tier.name != null && tones_tier_name != null) {
        if (tier.name.equals(tones_tier_name)) {
          tones_tier = tier;
        }
      } else if (tier.name != null && tier.name.equals("tones")) {
        tones_tier = tier;
      }

      if (tier.name != null && breaks_tier_name != null) {
        if (tier.name.equals(breaks_tier_name)) {
          breaks_tier = tier;
        }
      } else if (tier.name != null && tier.name.equals("breaks")) {
        breaks_tier = tier;
      }

    } while (tier.name != null);

    if (words_tier == null) {
      String tier_name = words_tier_name == null ? "'words' or 'orthographic'" : words_tier_name;
      throw new TextGridSyntaxErrorException("No words tier found with name, " + tier_name);
    }

    List<Word> words = generateWordList(words_tier.getRegions());

    if (tones_tier != null) {
      AlignmentUtils.copyToBITonesByTime(words, tones_tier.getRegions());
      if (breaks_tier == null || breaks_tier.getRegions().size() == 0) {
        AuToBIUtils.warn(
            "Null or empty specified breaks tier found.  Default breaks will be generated from phrase ending tones in" +
                " the tones tier.");
        ToBIUtils.generateBreaksFromTones(words);
      } else {
        try {
          AlignmentUtils.copyToBIBreaks(words, breaks_tier.getRegions());
        } catch (AuToBIException e) {

          for (int i = 0; i < words.size(); ++i) {
            if (words.get(i).getEnd() != breaks_tier.getRegions().get(i).getStart()) {
              AuToBIUtils.error("misaligned break at: " + breaks_tier.getRegions().get(i).getStart());
            }
          }
          throw e;
        }
        ToBIUtils.checkToBIAnnotations(words);
      }
    } else if (breaks_tier != null) {
      AlignmentUtils.copyToBIBreaks(words, breaks_tier.getRegions());
      AuToBIUtils
          .warn("No specified tones tier found.  Default phrase ending tones will be generated from breaks tier.");
      ToBIUtils.generateDefaultTonesFromBreaks(words);
    }

    return words;
  }


  /**
   * Converts the list of regions held in a Tier to a list of words.
   * <p/>
   * Omits silent regions when creating the list of words.
   * <p/>
   * Note: words can hold ToBI annotations, while regions are more general objects.
   *
   * @param regions the regions to convert
   * @return a list of words
   */
  protected List<Word> generateWordList(List<Region> regions) {
    List<Word> words = new ArrayList<Word>();
    for (Region r : regions) {
      if (!WordReaderUtils.isSilentRegion(r.getLabel(), silence_regex)) {
        Word w = new Word(r.getStart(), r.getEnd(), r.getLabel(), null, r.getFile());
        w.setAttribute("speaker_id", filename.replaceFirst("^.*/", "").subSequence(0, 2));
        words.add(w);
      }
    }
    return words;
  }


  /**
   * Generates a TextGridTier from the supplied AuToBIFileReader.
   *
   * @param reader The AuToBIFileReader
   * @return the Tier
   * @throws IOException if there is no tier to be read or if there is a problem with the reader
   */
  public Tier readTextGridTier(AuToBIFileReader reader) throws IOException, TextGridSyntaxErrorException {
    TextGridTier tier = new TextGridTier();
    tier.readTier(reader);
    return tier;
  }
}
