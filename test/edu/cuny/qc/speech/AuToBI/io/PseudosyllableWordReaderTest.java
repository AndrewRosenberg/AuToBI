/*  PseudosyllableWordReaderTest.java

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
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class for PseudosyllableWordReader
 */
public class PseudosyllableWordReaderTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private WavData wavData;

  @Before
  public void setUp() throws Exception {
    WavReader reader = new WavReader();
    wavData = reader.read(TEST_DIR + "/bdc-test.wav");
  }

  @Test
  public void testConstructor() {
    PseudosyllableWordReader reader = new PseudosyllableWordReader(wavData);
    assertEquals(25.0, reader.getThreshold(), 0.0001);
  }

  @Test
  public void testSetThreshold() {
    PseudosyllableWordReader reader = new PseudosyllableWordReader(wavData);

    reader.setThreshold(25.0);
    assertEquals(25.0, reader.getThreshold(), 0.0001);
  }

  @Test
  public void testConstructorSetsThreshold() {
    PseudosyllableWordReader reader = new PseudosyllableWordReader(wavData, 30);

    assertEquals(30.0, reader.getThreshold(), 0.0001);
  }

  @Test
  public void testReadWords() {
    PseudosyllableWordReader reader = new PseudosyllableWordReader(wavData);

    try {
      List<Word> words = reader.readWords();
      assertEquals(12, words.size());

    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadWordsWithHighThreshold() {
    PseudosyllableWordReader reader = new PseudosyllableWordReader(wavData, 30);

    try {
      List<Word> words = reader.readWords();
      assertEquals(12, words.size());

    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }
}
