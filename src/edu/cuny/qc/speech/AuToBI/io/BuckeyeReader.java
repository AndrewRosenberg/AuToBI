/*  BuckeyeReader.java

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
 * Reader class for buckeye corpus files.
 */
public class BuckeyeReader extends AuToBIWordReader {
  private String filename; // the words file to read.

  public BuckeyeReader(String filename) {
    this.filename = filename;
  }

  /**
   * Reads a list of words from the specified file.
   *
   * @return a list of words
   * @throws IOException     If the file cannot be read.
   * @throws AuToBIException If there is a problem with the file format.
   */
  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader reader = new AuToBIFileReader(filename);
    ArrayList<Word> words = new ArrayList<Word>();

    boolean started = false;
    String line;
    double start_time = 0.0;
    while ((line = reader.readLine()) != null) {
      if (line.trim().startsWith("#")) {
        started = true;
      } else if (started) {
        String[] data = line.trim().split(";");
        String[] first_chunk = data[0].split("\\s+");
        if (first_chunk.length < 3) {
          continue;
        }
        String label = first_chunk[2];
        double end_time = Double.parseDouble(first_chunk[0]);
        if (!label.startsWith("<") && !label.startsWith("{") && data.length >= 3) {
          Word w = new Word(start_time, end_time, label, null, filename);
          w.setAttribute("canonical_pron", data[1]);
          w.setAttribute("actual_pron", data[2]);
          words.add(w);
        }
        start_time = end_time;
      }
    }

    return words;
  }
}
