/*  AuToBIWordReader.java

    Copyright 2012-2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;

import java.io.IOException;
import java.util.List;

/**
 * An abstract class to govern the reading of a variety of input file types.
 */
public abstract class AuToBIWordReader {
  public String silence_regex = null;  // A regular expression to match against silence

  /**
   * Generate a list of words from an appropriate set of input files.
   *
   * @return A list of words
   * @throws IOException                                    If there is something wrong with the file reading
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException If there is a formatting problem
   */
  abstract public List<Word> readWords() throws IOException, AuToBIException;

  /**
   * Sets the silence regular expression
   *
   * @param regex the silence regular expression.
   */
  public void setSilenceRegex(String regex) {
    this.silence_regex = regex;
  }

  /**
   * Retrieves the silence regular expression
   *
   * @return the regular expression
   */
  public String getSilenceRegex() {
    return this.silence_regex;
  }
}
