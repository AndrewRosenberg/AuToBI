/*  AuToBIFileWriter.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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

