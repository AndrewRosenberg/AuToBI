/*  AuToBIFileReader.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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

import java.io.*;

/**
 * A wrapper class to BufferedReader that tracks the current line number of the file.
 */
public class AuToBIFileReader extends BufferedReader {

  private int line_number; // the current line number
  private String filename; // the filename

  /**
   * Constructs an AuToBIFileReader.
   *
   * @param filename the file to read
   * @throws FileNotFoundException if the file cannot be found.
   */
  public AuToBIFileReader(String filename) throws FileNotFoundException {
    super(new FileReader(filename));
    this.filename = filename;
    this.line_number = 1;
  }

  /**
   * Constructs an AuToBIFileReader with a specified character encoding.
   * <p/>
   * This is used to support reading of non-utf-8 character encoding.
   *
   * @param filename    the file to read
   * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
   * @throws FileNotFoundException if the file cannot be found.
   * @throws java.io.UnsupportedEncodingException
   *                               If the named charset is not supported
   */
  public AuToBIFileReader(String filename, String charsetName)
      throws FileNotFoundException, UnsupportedEncodingException {
    super(new InputStreamReader(new FileInputStream(filename), charsetName));
    this.filename = filename;
    this.line_number = 1;
  }

  /**
   * Reads a line, and increments the line counter
   *
   * @return the line
   * @throws IOException if there is a problem reading
   */
  public String readLine() throws IOException {
    line_number++;
    return super.readLine();
  }

  /**
   * Retrieves the current line number.
   *
   * @return the current line number
   */
  public int getLineNumber() {
    return line_number;
  }

  /**
   * Gets the filename
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }
}
