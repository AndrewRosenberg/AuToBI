/*  FormattedFile.java

    Copyright (c) 2009-2011 Andrew Rosenberg

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

/**
 * FormattedFile associates an enumerated type describing the formatting of the prosodic annotation.
 * <p/>
 * This allows multiple format extensions (like TextGrid) to be associated with a variety of styles of annotation
 * formatting.
 */
public class FormattedFile {
  public static enum Format {TEXTGRID, CPROM, BURNC, SIMPLE_WORD, SWB_NXT, BUCKEYE, DUR}

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
    } else if (filename.toLowerCase().endsWith("words")) {
      this.format = FormattedFile.Format.SIMPLE_WORD;
    } else if (filename.toLowerCase().endsWith("terminals.xml")) {
      this.format = FormattedFile.Format.SWB_NXT;
    } else if (filename.toLowerCase().endsWith(".in")) {
      this.format = FormattedFile.Format.DUR;
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

