/*  BuckeyeReader.java

    Copyright (c) 2012-2014 Andrew Rosenberg

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
