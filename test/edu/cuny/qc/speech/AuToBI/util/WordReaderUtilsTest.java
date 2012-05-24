/*  WordReaderUtilsTest.java

    Copyright 2011-2012 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIParameters;
import edu.cuny.qc.speech.AuToBI.io.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for ToBIUtils utility methods.
 */
public class WordReaderUtilsTest {

  @Test
  public void testIsSilentRegionWorksWithDefaultTrue() {

    String label = "sil";
    assertTrue(WordReaderUtils.isSilentRegion(label));
  }

  @Test
  public void testIsSilentRegionWorksWithEmptyString() {
    String label = "";
    assertTrue(WordReaderUtils.isSilentRegion(label));
  }

  @Test
  public void testIsSilentRegionWorksWithNullString() {
    assertTrue(WordReaderUtils.isSilentRegion(null));
  }

  @Test
  public void testIsSilentRegionWorksWithDefaultFalse() {
    String label = "RealWord";
    assertFalse(WordReaderUtils.isSilentRegion(label));
  }

  @Test
  public void testIsSilentRegionWorksWithNullRegexFalse() {
    String label = "RealWord";
    assertFalse(WordReaderUtils.isSilentRegion(label, null));
  }

  @Test
  public void testIsSilentRegionWorksWithNullRegexTrue() {
    String label = "sil";
    assertTrue(WordReaderUtils.isSilentRegion(label, null));
  }

  @Test
  public void testIsSilentRegionWorksWithRegexTrue() {
    String label = "RealWord";
    assertTrue(WordReaderUtils.isSilentRegion(label, "^R.*"));
  }

  @Test
  public void testIsSilentRegionWorksWithRegexFalse() {
    String label = "sil";
    assertFalse(WordReaderUtils.isSilentRegion(label, "^R.*"));
  }

  @Test
  public void testGetWordReaderTextGrid() {
    FormattedFile file = new FormattedFile("test.txt", FormattedFile.Format.TEXTGRID);

    AuToBIWordReader reader = WordReaderUtils.getAppropriateReader(file, new AuToBIParameters());
    assertTrue(reader instanceof TextGridReader);
  }

  @Test
  public void testGetWordReaderCPROM() {
    FormattedFile file = new FormattedFile("test.txt", FormattedFile.Format.CPROM);

    AuToBIWordReader reader = WordReaderUtils.getAppropriateReader(file, new AuToBIParameters());
    assertTrue(reader instanceof CPromTextGridReader);
  }

  @Test
  public void testGetWordReaderBURNC() {
    FormattedFile file = new FormattedFile("test.txt", FormattedFile.Format.BURNC);

    AuToBIWordReader reader = WordReaderUtils.getAppropriateReader(file, new AuToBIParameters());
    assertTrue(reader instanceof BURNCReader);
  }

  @Test
  public void testGetWordReaderSimpleWord() {
    FormattedFile file = new FormattedFile("test.txt", FormattedFile.Format.SIMPLE_WORD);

    AuToBIWordReader reader = WordReaderUtils.getAppropriateReader(file, new AuToBIParameters());
    assertTrue(reader instanceof SimpleWordReader);
  }
}
