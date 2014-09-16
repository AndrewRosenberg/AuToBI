/*  FormattedFile.java

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

/**
 * FormattedFile associates an enumerated type describing the formatting of the prosodic annotation.
 * <p/>
 * This allows multiple format extensions (like TextGrid) to be associated with a variety of styles of annotation
 * formatting.
 */
public class FormattedFile {
  public static enum Format {
    TEXTGRID, CPROM, BURNC, SIMPLE_WORD, SWB_NXT, BUCKEYE, KOH, CONS_GZ, DUR, POSTING_LIST,
    RHAPSODIE
  }

  public String filename;
  public Format format;

  /**
   * Constructs a new formatted file based on the given filename.
   * <p/>
   * The file format is determined by the extension of the filename.
   * <p/>
   * Note, however, that multiple formats can use the same extension.  For example, there are many ways to use a
   * TextGrid to encode information.
   *
   * @param filename the filename
   */
  public FormattedFile(String filename) {
    this.filename = filename;
    String filestem = filename.substring(filename.lastIndexOf("/") + 1);
    if (filename.toLowerCase().endsWith("textgrid")) {
      this.format = FormattedFile.Format.TEXTGRID;
    } else if (filename.toLowerCase().endsWith("ala")) {
      this.format = FormattedFile.Format.BURNC;
    } else if (filestem.matches("s\\d\\d\\d\\d[ab].words")) {
      this.format = Format.BUCKEYE;
    } else if (filename.toLowerCase().endsWith("words") || filename.toLowerCase().endsWith(".txt")) {
      this.format = FormattedFile.Format.SIMPLE_WORD;
    } else if (filename.toLowerCase().endsWith("terminals.xml")) {
      this.format = FormattedFile.Format.SWB_NXT;
    } else if (filename.endsWith("augocc")) {
      this.format = FormattedFile.Format.KOH;
    } else if (filename.endsWith("in")) {
      this.format = FormattedFile.Format.DUR;
    } else if (filename.endsWith("cons.gz")) {
      this.format = FormattedFile.Format.CONS_GZ;
    } else if (filename.endsWith("kws_posting_list")) {
      this.format = FormattedFile.Format.POSTING_LIST;
    }
  }

  /**
   * Constructs a new formatted file with a given filename and format.
   *
   * @param filename the filename
   * @param format   the format indicator
   */
  public FormattedFile(String filename, Format format) {
    this.filename = filename;
    this.format = format;
  }

  /**
   * Retrieves the associated filename.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Retrieves the file format.
   *
   * @return the file format
   */
  public Format getFormat() {
    return format;
  }
}

