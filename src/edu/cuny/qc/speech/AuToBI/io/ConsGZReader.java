/*  ConsGZReader.java

    Copyright (c) 2013-2014 Andrew Rosenberg

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
