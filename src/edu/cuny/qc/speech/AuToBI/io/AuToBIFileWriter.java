/*  AuToBIFileWriter.java

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
 * A wrapper around a BufferedWriter.
 * <p/>
 * AuToBIFileWriter doesn't currently extend the functionality of BufferedWriter.
 * <p/>
 * This class is a placeholder if any extensions are required.
 */
public class AuToBIFileWriter extends BufferedWriter {

  public AuToBIFileWriter(String filename) throws IOException {
    super(new FileWriter(filename));
  }

  /**
   * Constructs a new AuToBIFileWriter with an option to open a file for appending.
   *
   * @param filename the name of the file to write to.
   * @param append   If true, opens the file for appending.
   * @throws IOException
   */
  public AuToBIFileWriter(String filename, boolean append) throws IOException {
    super(new FileWriter(filename, append));
  }
}

