/*  CPromTextGridReaderTest.java

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
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for CPromTextGridReader
 *
 * @see edu.cuny.qc.speech.AuToBI.io.CPromTextGridReader
 */
@SuppressWarnings("unchecked")
public class CPromTextGridReaderTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private final String testfile = TEST_DIR + "/test.cprom.TextGrid";
  private CPromTextGridReader reader;

  @Before
  public void setUp() throws Exception {
    reader = new CPromTextGridReader(testfile, "words", "delivery", "UTF16", false);
  }

  @Test
  public void testReadWordsCorrectlySkipsSilence() {
    try {
      List<Word> words = reader.readWords();
      assertEquals(23, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadWordsWithDefaultConstructor() {
    reader = new CPromTextGridReader(testfile, null, null, null, false);
    try {
      List<Word> words = reader.readWords();
      assertEquals(23, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadWordsCorrectlySetsLabels() {
    String[] expected_labels =
        ("voilà donc vous avez bien perçu même intuitivement qu' il|y avait euh des choses issues de radios " +
            "commerciales ou de radios classiques etcaetera")
            .split(" ");

    try {
      List<Word> words = reader.readWords();
      for (int i = 0; i < expected_labels.length; i++) {
        assertEquals(expected_labels[i], words.get(i).getLabel());
      }
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadWordsCorrectlySetsProminenceAsPitchAccent() {
    String[] expected_prominence =
        "n n n n n y n n n n y n n n n n n y n n n y y".split(" ");

    try {
      List<Word> words = reader.readWords();
      for (int i = 0; i < expected_prominence.length; i++) {
        if (expected_prominence[i].equals("y")) {
          assertNotNull(words.get(i).getAccent());
          assertEquals("X*?", words.get(i).getAccent());
        } else {
          assertNull(words.get(i).getAccent());
        }
      }
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }


  @Test
  public void testReadWordsCorrectlySetsSecondaryProminenceAsPitchAccent() {
    reader = new CPromTextGridReader(testfile, "words", "delivery", "UTF16", true);
    String[] expected_prominence =
        "y n n n y y n y n n y n n n n n y y n n n y y".split(" ");

    try {
      List<Word> words = reader.readWords();
      for (int i = 0; i < expected_prominence.length; i++) {
        if (expected_prominence[i].equals("y")) {
          assertNotNull(words.get(i).getLabel() + " should be accented but isn't", words.get(i).getAccent());
          assertEquals("X*?", words.get(i).getAccent());
        } else {
          assertNull(words.get(i).getAccent());
        }
      }
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCopyProminenceAnnotationsWorksWithAlignedLabels() {
    List<Word> words = new ArrayList<Word>();
    List<Region> prominences = new ArrayList<Region>();

    Word w1 = new Word(0, 1, "one");
    words.add(w1);
    Word w2 = new Word(1, 2, "two");
    words.add(w2);

    prominences.add(new Region(0, 1, "P"));
    prominences.add(new Region(1, 2, ""));
    reader.copyProminenceAnnotations(words, prominences);

    assertTrue(w1.isAccented());
    assertEquals("X*?", w1.getAccent());
    assertFalse(w2.isAccented());
  }

  @Test
  public void testCopyProminenceAnnotationsWorksWithLiaisonProminence() {
    List<Word> words = new ArrayList<Word>();
    List<Region> prominences = new ArrayList<Region>();

    Word w1 = new Word(0, 1, "de");
    words.add(w1);
    Word w2 = new Word(1, 2, "radios");
    words.add(w2);

    prominences.add(new Region(0, 1.5, "P"));
    prominences.add(new Region(1.5, 2, ""));
    reader.copyProminenceAnnotations(words, prominences);

    assertFalse(w1.isAccented());
    assertTrue(w2.isAccented());
    assertEquals("X*?", w2.getAccent());
  }
}
