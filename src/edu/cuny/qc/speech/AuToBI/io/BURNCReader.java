/*  BURNCReader.java

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
package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.util.AlignmentUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;
import edu.cuny.qc.speech.AuToBI.util.WordReaderUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BURNCReader is an AuToBIWordReader specifically for reading the Boston University Radio News Corpus (BURNC) input
 * files.
 * <p/>
 * The input format is unconventional.  It includes forced alignment output files (*.ala) which include phone hypotheses
 * and word boundaries.  (Under typical usage, only the word boundaries are read.)
 * <p/>
 * Tones and break information are stored in vanilla XWaves files.
 */
public class BURNCReader extends AuToBIWordReader {
  private String filestem;       // The stem of the set of BURNC files to be read
  private String phone_feature;  // The name of a feature to store phone regions in.

  /**
   * Constructs a new BURNC reader.
   *
   * @param filestem the name of the file to read
   */
  public BURNCReader(String filestem) {
    this(filestem, null);
  }

  /**
   * Constructs a new BURNC reader that optionally reads BURNC phones and stores then in a list associated with each
   * word.
   */
  public BURNCReader(String filestem, String phone_feature) {
    // TODO: clear any extension if present.
    this.filestem = filestem;
    this.phone_feature = phone_feature;
  }

  /**
   * Reads BURNC words incorporating information from .ala, .ton and .brk files.
   * <p/>
   * Tones and breaks are read from .ton and .brk files in the same directory (if available).
   *
   * @return a list of words read from the ala file.
   */
  public List<Word> readWords() {

    List<Word> words = readALAWords();
    if (words == null) return null;

    try {
      List<Region> tones = readTones();
      List<Region> breaks = readBreaks();

      AlignmentUtils.copyToBIBreaks(words, breaks);
      // BURNC annotations include the correct number of phrase ending tones, but
      // these are not always aligned with the phrase final breaks
      AlignmentUtils.copyToBITonesByIndex(words, tones);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    return words;
  }

  /**
   * Reads word boundaries from an .ala file.
   * <p/>
   * Ala files include phone identities and word identities.
   * <p/>
   * Phone identities are whitespace separated and contain the following fields: phone_id start_time(ms) duration(ms)
   * <p/>
   * Lines that are start with ">" characters contain word annotations, but no start and end times, these must be
   * constructed from the phone annotations. For example:
   * <p/>
   * N       20      3
   * <p/>
   * AY      23      12
   * <p/>
   * N       35      4
   * <p/>
   * TCL     39      2
   * <p/>
   * T       41      4
   * <p/>
   * IY+1    45      8
   * <p/>
   * N       53      5
   * <p/>
   * >nineteen
   * <p/>
   * S       58      10
   * <p/>
   * EH+1    68      8
   * <p/>
   * V       76      3
   * <p/>
   * EN      79      4
   * <p/>
   * TCL     83      2
   * <p/>
   * T       85      1
   * <p/>
   * IY      86      7
   * <p/>
   * >seventy
   * <p/>
   * Indicates two words: "nineteen" which starts at .2 and ends at .58 and "seventy" which starts at .58 and ends at
   * .96
   *
   * @return A list of words.
   */
  protected List<Word> readALAWords() {
    String line;
    Double word_start_time = -1.0;
    Double end_time = -1.0;
    AuToBIFileReader reader;
    String filename = filestem + ".ala";
    ArrayList<Word> words = new ArrayList<Word>();

    ArrayList<Region> phones = new ArrayList<Region>();
    boolean read_phones = false;
    if (phone_feature != null && phone_feature.length() != 0) {
      read_phones = true;
    }

    try {
      reader = new AuToBIFileReader(filename);
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith(">")) {
          // Read a word and construct the start and end times.
          String word = line.replaceFirst(">", "");
          if (!WordReaderUtils.isSilentRegion(word, silence_regex)) {
            Word w = new Word(word_start_time, end_time, word, null, filename);
            w.setAttribute("speaker_id", filename.replaceFirst("^.*/", "").subSequence(0, 3));
            if (read_phones) {
              w.setAttribute(phone_feature, phones);  // Assign the phones to the list.
            }
            words.add(w);
          }
          if (read_phones) {
            phones = new ArrayList<Region>();
          }
          word_start_time = -1.0;
        } else {

          // The .ala format is a whitespace delimited format containing the fields:
          // phoneid word_start_time duration
          String[] data = line.split("\\s+");
          if (word_start_time == -1.0) {
            word_start_time = (Double.parseDouble(data[1]) - 1) / 100.0;
          }
          end_time = ((Double.parseDouble(data[1]) - 1) + Double.parseDouble(data[2])) / 100.0;

          if (read_phones) {
            String label = data[0];
            boolean stressed = false;
            if (label.endsWith("+1")) {
              label = label.substring(0, label.length() - 2);
              stressed = true;
            }
            Region r = new Region((Double.parseDouble(data[1]) - 1) / 100.0, end_time, label);
            if (stressed) {
              r.setAttribute("lexical_stress", true);
            }
            phones.add(r);
          }
        }
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    return words;
  }

  /**
   * Reads ToBI tones from an xwaves formatted Tones file and aligns them to the input words.
   *
   * @return a list of regions describing the tones
   * @throws java.io.FileNotFoundException if there is no tones file
   */
  public List<Region> readTones() throws FileNotFoundException {
    String filename = filestem + ".ton";
    return readXWavesPointTier(filename);
  }

  /**
   * Reads ToBI break indices from an xwaves formatted Breaks file and aligns them to the input words.
   *
   * @return a list of regions describing the breaks
   * @throws java.io.FileNotFoundException if there is no break file
   */
  public List<Region> readBreaks() throws FileNotFoundException {
    String filename = filestem + ".brk";
    return readXWavesPointTier(filename);
  }

  /**
   * Read information from an XWaves formatted input file as points.
   *
   * @param filename the xwaves formatted filename
   * @return a list of point regions
   * @throws FileNotFoundException if there is no such file
   */
  public List<Region> readXWavesPointTier(String filename) throws FileNotFoundException {
    List<Region> tier = new ArrayList<Region>();

    AuToBIFileReader reader = new AuToBIFileReader(filename);

    String line;
    // Pass the header
    try {
      while ((line = reader.readLine()) != null && !line.matches("#")) {
      }

      while ((line = reader.readLine()) != null) {
        line = AuToBIReaderUtils.removeTabsAndTrim(line);
        String[] vars = line.split("\\s+");
        Double time;
        try {
          time = Double.parseDouble(vars[0]);
          if (!time.isNaN()) {
            String label;
            if (vars.length < 3) {
              label = "";
            } else {
              label = vars[2];
            }

            tier.add(new Region(time, time, label, filename));
          }
        } catch (NumberFormatException ignored) {
        }
      }
      reader.close();

      return tier;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
