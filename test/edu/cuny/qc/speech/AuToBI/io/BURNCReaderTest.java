/*  BURNCReaderTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for BURNCReader
 *
 * @see BURNCReader
 */
@SuppressWarnings("unchecked")
public class BURNCReaderTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private final String testfile_stem = TEST_DIR + "/test";
  private BURNCReader reader;

  @Before
  public void setUp() throws Exception {
    reader = new BURNCReader(testfile_stem);
  }

  @Test
  public void testReadWordsCorrectlySkipsSilence() {
    List<Word> words = reader.readWords();

    assertEquals(3, words.size());
  }

  @Test
  public void testReadWordsCorrectlySetsLabels() {
    List<Word> words = reader.readWords();

    assertEquals(3, words.size());
    assertEquals("Wanted", words.get(0).getLabel());
    assertEquals("Chief", words.get(1).getLabel());
    assertEquals("Justice", words.get(2).getLabel());
  }

  @Test
  public void testReadWordsCorrectlySetsPhones() {
    reader = new BURNCReader(testfile_stem, "phones");
    List<Word> words = reader.readWords();
    assertTrue(words.size() > 1);
    assertTrue(words.get(0).hasAttribute("phones"));
    List<Region> phones = (List<Region>) words.get(0).getAttribute("phones");

    assertEquals(8, phones.size());
    assertEquals("W", phones.get(0).getLabel());
    assertEquals("AA", phones.get(1).getLabel());
    assertEquals("N", phones.get(2).getLabel());
    assertEquals("TCL", phones.get(3).getLabel());
    assertEquals("T", phones.get(4).getLabel());
    assertEquals("AX", phones.get(5).getLabel());
    assertEquals("DCL", phones.get(6).getLabel());
    assertEquals("D", phones.get(7).getLabel());
  }

  @Test
  public void testReadWordsCorrectlySetsPhonesWithLexicalStress() {
    reader = new BURNCReader(testfile_stem, "phones");
    List<Word> words = reader.readWords();
    assertTrue(words.size() > 1);
    assertTrue(words.get(0).hasAttribute("phones"));
    List<Region> phones = (List<Region>) words.get(0).getAttribute("phones");

    assertEquals(8, phones.size());
    assertEquals("AA", phones.get(1).getLabel());
    assertTrue(phones.get(1).hasAttribute("lexical_stress"));
  }

  @Test
  public void testReadLinesCorrectlyAlignsPitchAccentTones() {
    List<Word> words = reader.readWords();

    assertNotNull(words.get(0).getAccent());
    assertEquals("H*", words.get(0).getAccent());
    assertNotNull(words.get(1).getAccent());
    assertEquals("H*", words.get(1).getAccent());
    assertNotNull(words.get(2).getAccent());
    assertEquals("!H*", words.get(2).getAccent());
  }

  @Test
  public void testReadLinesCorrectlyAlignsPhraseEndingTones() {
    List<Word> words = reader.readWords();

    assertNotNull(words.get(0).getPhraseAccent());
    assertEquals("L-", words.get(0).getPhraseAccent());
    assertNotNull(words.get(0).getBoundaryTone());
    assertEquals("L%", words.get(0).getBoundaryTone());

    assertNull(words.get(1).getPhraseAccent());
    assertNull(words.get(1).getBoundaryTone());

    assertNotNull(words.get(2).getPhraseAccent());
    assertEquals("L-", words.get(2).getPhraseAccent());
    assertNull(words.get(2).getBoundaryTone());
  }

  @Test
  public void testReadLinesCorrectlyAlignsBreaks() {
    List<Word> words = reader.readWords();

    assertEquals("4", words.get(0).getBreakAfter());
    assertEquals("1", words.get(1).getBreakAfter());
    assertEquals("3-", words.get(2).getBreakAfter());

    assertNull(words.get(0).getBreakBefore());
    assertEquals("4", words.get(1).getBreakBefore());
    assertEquals("1", words.get(2).getBreakBefore());
  }

  @Test
  public void testReadAlaWords() {
    List<Word> words = reader.readALAWords();

    assertEquals(3, words.size());
  }

  @Test
  public void testReadAlaWordsSetsSpeakerID() {
    List<Word> words = reader.readWords();

    assertEquals(3, words.size());
    // The speaker ID for BURNC files is the first three characters in the filestem.
    assertTrue(words.get(0).hasAttribute("speaker_id"));
    assertEquals("tes", words.get(0).getAttribute("speaker_id"));
    assertTrue(words.get(1).hasAttribute("speaker_id"));
    assertEquals("tes", words.get(1).getAttribute("speaker_id"));
    assertTrue(words.get(2).hasAttribute("speaker_id"));
    assertEquals("tes", words.get(2).getAttribute("speaker_id"));
  }

  @Test
  public void testReadTones() {
    try {
      List<Region> tones = reader.readTones();

      // Tones should include all of the tone markings in the files
      // in the test case this is H*, HiF0, L-L%, H*, HiF0, !H*, L-
      // No parsing of the tones is performed by readTones.
      assertEquals(7, tones.size());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadBreaks() {
    try {
      List<Region> breaks = reader.readBreaks();

      // There must be the same number of breaks as words.
      assertEquals(3, breaks.size());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
  }
}
