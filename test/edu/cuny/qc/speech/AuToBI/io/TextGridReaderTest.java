/*  TextGridReaderTest.java

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.io.NXTTier
 */
public class TextGridReaderTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadsWords() {
    String filestem = TEST_DIR + "/test.TextGrid";
    TextGridReader reader = new TextGridReader(filestem);

    try {
      List<Word> words = reader.readWords();

      assertEquals(10, words.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  @Test
  public void testReadsWordsWithNamedTiers() {
    String filestem = TEST_DIR + "/test.TextGrid";
    TextGridReader reader = new TextGridReader(filestem, "words", "tones", "breaks");

    try {
      List<Word> words = reader.readWords();

      assertEquals(10, words.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWordsWithUTF16() {
    String filestem = TEST_DIR + "/test.utf16.TextGrid";
    TextGridReader reader = new TextGridReader(filestem, "UTF16");

    try {
      List<Word> words = reader.readWords();

      assertEquals(10, words.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWordsWithNoTonesTier() {
    String filestem = TEST_DIR + "/test.notones.TextGrid";
    TextGridReader reader = new TextGridReader(filestem);

    try {
      List<Word> words = reader.readWords();

      assertEquals(10, words.size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGeneratesDefaultTonesWithNoTonesTier() {
    String filestem = TEST_DIR + "/test.notones.TextGrid";
    TextGridReader reader = new TextGridReader(filestem);

    try {
      List<Word> words = reader.readWords();

      assertEquals(10, words.size());
      for (Word word : words) {
        if (word.isIntonationalPhraseFinal()) {
          assertTrue(word.hasBoundaryTone());
          assertTrue(word.hasPhraseAccent());
        } else if (word.isIntermediatePhraseFinal()) {
          assertFalse(word.hasBoundaryTone());
          assertTrue(word.hasPhraseAccent());
        } else {
          assertFalse(word.hasBoundaryTone());
          assertFalse(word.hasPhraseAccent());
        }
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWordsThrowsExceptionWithNoWords() {
    String filestem = TEST_DIR + "/test.nowords.TextGrid";
    TextGridReader reader = new TextGridReader(filestem);

    try {
      reader.readWords();

      fail();
    } catch (Exception e) {
      // expected.
    }
  }

  @Test
  public void testReadsWordsFromTextGridWithNumberAsTime() {
    String filestem = TEST_DIR + "/test.number.TextGrid";
    TextGridReader reader = new TextGridReader(filestem);
    try {
      List<Word> words = reader.readWords();
      assertEquals(6, words.size());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }

  }
}
