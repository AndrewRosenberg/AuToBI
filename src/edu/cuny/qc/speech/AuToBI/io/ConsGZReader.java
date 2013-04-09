/*  ConsGZReader.java

    Copyright (c) 2013 Andrew Rosenberg

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * ConsGZReader is a class that reads gzipped plain text representations of consensus nets.
 */
public class ConsGZReader extends AuToBIWordReader {
  private String filename; // the words file to read.
  private String charset;  // the charset encoding

  public ConsGZReader(String filename) {
    this.filename = filename;
    this.charset = null;
  }

  public ConsGZReader(String filename, String charset) {
    this.filename = filename;
    this.charset = charset;
  }

  /**
   * Reads a GZipped Consensus net file and returns the words.
   * <p/>
   * The label of the consensus net contains the word hypotheses and posteriors in a string format.
   * At this point, there is no need to store these in a more accessible format though they'd be parsed here.
   *
   * @return a list of "words" one for each CN group of edges.
   */
  public List<Word> readWords() throws AuToBIException {

    try {
      BufferedReader reader;
      if (charset == null) {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
      } else {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), charset));
      }

      List<Word> words = new ArrayList<Word>();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] data = line.trim().split("\\s+");
        Word w =
            new Word(Double.parseDouble(data[0]), Double.parseDouble(data[0]) + Double.parseDouble(data[1]), data[2]);
        w.setFile(filename);
        words.add(w);
      }
      return words;
    } catch (IOException e) {
      throw new AuToBIException(e.getMessage());
    }
  }
}
