/*  AuToBIReaderUtilsTest.java

    Copyright 2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.io.FormattedFile;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.util.AuToBIUtilsTest
 */
public class AuToBIReaderUtilsTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testRemoveTabsAndTrim() {
    String s = "   this is a test    ";
    assertEquals("this is a test", AuToBIReaderUtils.removeTabsAndTrim(s));
  }

  @Test
  public void testRemoveTabsAndTrimWithTabs() {
    String s = "   this is\ta test    ";
    assertEquals("this is a test", AuToBIReaderUtils.removeTabsAndTrim(s));
  }

  @Test
  public void testGlobFormattedFilesWithFormat() {
    String pattern = TEST_DIR + "/*.wav";

    List<FormattedFile> files = AuToBIReaderUtils.globFormattedFiles(pattern, FormattedFile.Format.BURNC);

    assertTrue(files.size() > 0);
    for (FormattedFile f : files) {
      assertEquals(FormattedFile.Format.BURNC, f.getFormat());
    }
  }

  @Test
  public void testGlobFormattedFiles() {
    String pattern = TEST_DIR + "/*.TextGrid";

    List<FormattedFile> files = AuToBIReaderUtils.globFormattedFiles(pattern);

    assertTrue(files.size() > 0);
    for (FormattedFile f : files) {
      assertEquals(FormattedFile.Format.TEXTGRID, f.getFormat());
    }
  }

}
