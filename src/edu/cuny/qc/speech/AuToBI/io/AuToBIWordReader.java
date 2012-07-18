/*  AuToBIWordReader.java

    Copyright 2012 Andrew Rosenberg

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
   * @throws IOException If there is something wrong with the file reading
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *                     If there is a formatting problem
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
