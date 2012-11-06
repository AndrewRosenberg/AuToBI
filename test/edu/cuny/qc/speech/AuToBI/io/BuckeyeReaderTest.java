/*  BuckeyeReaderTest.java

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.io.BuckeyeReader
 */
public class BuckeyeReaderTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadsLines() {
    BuckeyeReader reader = new BuckeyeReader(TEST_DIR + "/test.buckeye.words");

    try {
      List<Word> words = reader.readWords();
      assertEquals(998, words.size());
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testReadsLinesWithExtraWhiteSpace() {
    // This file has a double space between the first and second fields.
    BuckeyeReader reader = new BuckeyeReader(TEST_DIR + "/test2.buckeye.words");

    try {
      List<Word> words = reader.readWords();
      assertEquals(979, words.size());
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testSetsCanonicalPronunciation() {
    BuckeyeReader reader = new BuckeyeReader(TEST_DIR + "/test.buckeye.words");

    try {
      List<Word> words = reader.readWords();
      for (Word w : words) {
        assertTrue(w.hasAttribute("canonical_pron"));
      }
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testSetsAcutalPronunciation() {
    BuckeyeReader reader = new BuckeyeReader(TEST_DIR + "/test.buckeye.words");

    try {
      List<Word> words = reader.readWords();
      for (Word w : words) {
        assertTrue(w.hasAttribute("actual_pron"));
      }
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
  }
}
