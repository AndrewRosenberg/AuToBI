/*  ConsGZReaderTest.java

    Copyright 2013 Andrew Rosenberg

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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for ConsGZReader
 *
 * @see ConsGZReader
 */
@SuppressWarnings("unchecked")
public class ConsGZReaderTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private final String testfile = TEST_DIR + "/test.cons.gz";
  private ConsGZReader reader;

  @Before
  public void setUp() throws Exception {
    reader = new ConsGZReader(testfile);
  }

  @Test
  public void testReadWordsCorrectlyGetsTheNumberOfWords() {
    try {
      List<Word> words = reader.readWords();
      assertEquals(7, words.size());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }
}
