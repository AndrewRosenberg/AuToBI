/*  SimpleWordReader.java

    Copyright 2009-2011 Andrew Rosenberg

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
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SimpleWordReader reads orthographic information from a text file containing start times, end times, and the lexical
 * item.
 * <p/>
 * Format:
 * <p/>
 * <Orthography> <Start> <End>
 */
public class SimpleWordReader extends AuToBIWordReader {
  private String filename;
  private String charset_name;

  /**
   * Constructs a new SimpleWordReader for a given file.
   *
   * @param filename the filename
   */
  public SimpleWordReader(String filename) {
    this(filename, null);
  }

  /**
   * Constructs a new SimpleWordReader for a given filename and character set.
   * <p/>
   * This allows a user to specify a non-ascii encoding for the file.
   *
   * @param filename     the filename
   * @param charset_name the character set identifier
   */
  public SimpleWordReader(String filename, String charset_name) {
    this.filename = filename;
    this.charset_name = charset_name;
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
    AuToBIFileReader file_reader;
    if (charset_name != null) {
      file_reader = new AuToBIFileReader(filename, charset_name);
    } else {
      file_reader = new AuToBIFileReader(filename);
    }

    List<Word> words = new ArrayList<Word>();
    String line;
    while ((line = file_reader.readLine()) != null) {

      String[] data = line.split("\\s+");
      if (data.length < 3) {
        throw new AuToBIException("Line " + file_reader.getLineNumber() + " has too few fields - " + line);
      }

      Double start = Double.parseDouble(data[1]);
      Double end = Double.parseDouble(data[2]);

      if (end <= start) {
        throw new AuToBIException("End time before start time on line " + file_reader.getLineNumber() + " - " + line);
      }

      words.add(new Word(start, end, data[0]));
    }

    return words;
  }
}
