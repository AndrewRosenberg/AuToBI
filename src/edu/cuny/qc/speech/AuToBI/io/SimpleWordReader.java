/*  SimpleWordReader.java

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
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SimpleWordReader reads orthographic information from a text file containing start times, end times, and the lexical
 * item.
 * <p/>
 * A user may specify the order of the three fields, orthography, start time and end time.  The time is assumed to be in
 * seconds. Default Format:
 * <p/>
 * <Orthography> <Start> <End>
 */
public class SimpleWordReader extends AuToBIWordReader {
  private String filename;
  private String charset_name;

  private int ortho_idx;  // the index of the orthographic label
  private int start_idx;  // the index of the start time
  private int end_idx;    // the index of the end time

  /**
   * Constructs a new SimpleWordReader for a given file.
   *
   * @param filename the filename
   */
  public SimpleWordReader(String filename) {
    this(filename, null, 0, 1, 2);
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
    this(filename, charset_name, 0, 1, 2);
  }

  /**
   * Constructs a new SimpleWordReader for a given filename and character set.
   * <p/>
   * This allows a user to specify a non-ascii encoding for the file.
   *
   * @param filename     the filename
   * @param charset_name the character set identifier
   * @param ortho_idx    the index of the orthographic field
   * @param start_idx    the index of the start time field
   * @param end_idx      the index of the end time field
   */
  public SimpleWordReader(String filename, String charset_name, int ortho_idx, int start_idx, int end_idx) {
    this.filename = filename;
    this.charset_name = charset_name;
    this.ortho_idx = ortho_idx;
    this.start_idx = start_idx;
    this.end_idx = end_idx;

    if (ortho_idx == start_idx || ortho_idx == end_idx || start_idx == end_idx) {
      AuToBIUtils.error(
          "Orthographic, Start and End indices should be unique in SimpleWordReader.  Using defaults, ortho: 0, " +
              "start: 1, end: 2");
      this.ortho_idx = 0;
      this.start_idx = 1;
      this.end_idx = 2;
    }
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

      Double start = Double.parseDouble(data[start_idx]);
      Double end = Double.parseDouble(data[end_idx]);

      if (end < start) {
        throw new AuToBIException("End time before start time on line " + file_reader.getLineNumber() + " - " + line);
      }

      words.add(new Word(start, end, data[ortho_idx], null, filename));
    }

    return words;
  }
}
