/*  DURReader.java

    Copyright (c) 2012 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A file reader for the DUR corpus.
 */
public class DURReader extends AuToBIWordReader {
  private String filename;

  /**
   * Constructs a new DURReader.
   *
   * @param filename a DUR filename
   */
  public DURReader(String filename) {
    this.filename = filename;
  }

  /**
   * Reads words from the DUR file.
   *
   * @return a list of words
   * @throws IOException     if there is a problem reading the file
   * @throws AuToBIException if there is a problem with the formatting of the file
   */
  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader reader = new AuToBIFileReader(filename);

    List<Word> words = new ArrayList<Word>();
    String line;
    Word word = null;
    Double time = 0.0;
    String next_break = null;
    while ((line = reader.readLine()) != null) {

      if (line.trim().length() > 0 && !line.startsWith("**")) {
        String[] data = line.trim().split("\\s+");
        if (data.length == 1) {
          if (word != null && (!word.getLabel().equals("si") && !word.getLabel().equals(",")))
            words.add(word);
          word = new Word(time, time, data[0]);
          if (next_break != null) {
            word.setBreakBefore(next_break);
            next_break = null;
          }
        } else if (data.length == 2) {
          // Intermediate phrases are indicated with a ] token at the start or end of a word
          if (word != null && data[0].equals("]")) {
            if (word.getDuration() == 0) {
              word.setBreakBefore("3");
              if (words.size() > 0) {
                words.get(words.size() - 1).setBreakAfter("3");
                words.get(words.size() - 1).setPhraseAccent("X-?");
              }
            } else {
              word.setBreakAfter("3");
              word.setPhraseAccent("X-?");
              next_break = "3";
            }
          }

          // Intermediate phrases are indicated with a } token at the start or end of a word
          if (word != null && data[0].equals("}")) {
            if (word.getDuration() == 0) {
              word.setBreakBefore("4");
              if (words.size() > 0) {
                words.get(words.size() - 1).setBreakAfter("4");
                words.get(words.size() - 1).setPhraseAccent("X-?");
                words.get(words.size() - 1).setBoundaryTone("X%?");
              }
            } else {
              word.setBreakAfter("4");
              word.setPhraseAccent("X-?");
              word.setBoundaryTone("X%?");
              next_break = "4";
            }
          }
          time = Double.parseDouble(data[1]);
          if (word != null && !data[0].equals("}") && !data[0].equals("]")) {
            word.setEnd(time);
          }
          if (word != null && (data[0].contains("'") || data[0].contains("\""))) {
            word.setAccent("H*");
          }
        } else {
          throw new AuToBIException("Line " + reader.getLineNumber() + " contains more than 2 fields");
        }
      }
    }
    words.get(words.size() - 1).setBreakAfter("4");
    words.get(words.size() - 1).setPhraseAccent("X-?");
    words.get(words.size() - 1).setBoundaryTone("X%?");
    return words;
  }
}
