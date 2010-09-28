/*  BURNCReader.java

    Copyright 2010 Andrew Rosenberg

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
  private String filestem;

  /**
   * Constructs a new BURNC reader.
   *
   * @param filestem the name of the file to read
   */
  public BURNCReader(String filestem) {
    this.filestem = filestem;
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
      AlignmentUtils.copyToBITones(words, tones);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    return words;
  }

  /**
   * Reads word boundaries from the .ala file.
   *
   * @return A list of words.
   */
  private List<Word> readALAWords() {
    String line;
    Double start_time = -1.0;
    Double end_time = -1.0;
    AuToBIFileReader reader;
    String filename = filestem + ".ala";
    ArrayList<Word> words = new ArrayList<Word>();

    try {
      reader = new AuToBIFileReader(filename);
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith(">")) {
          // insert Region
          String word = line.replaceFirst(">", "");
          if (!WordReaderUtils.isSilentRegion(word)) {
            Word w = new Word(start_time, end_time, word, null, filename);
            w.setAttribute("speaker_id", filename.replaceFirst("^.*/", "").subSequence(0, 3));
            words.add(w);
          }
          start_time = -1.0;
        } else {
          String[] data = line.split("\\s+");
          if (start_time == -1.0) {
            start_time = (Double.parseDouble(data[1]) - 1) / 100.0;
          }
          end_time = ((Double.parseDouble(data[1]) - 1) + Double.parseDouble(data[2])) / 100.0;
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
        line = WordReaderUtils.removeTabsAndTrim(line);
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
