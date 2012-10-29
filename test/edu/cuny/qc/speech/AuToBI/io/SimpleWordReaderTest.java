/*  SimpleWordReaderTest.java

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
import static org.junit.Assert.fail;

/**
 * Test class for SimpleWordReader
 *
 * @see edu.cuny.qc.speech.AuToBI.io.SimpleWordReader
 */
@SuppressWarnings("unchecked")
public class SimpleWordReaderTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadsWords() {
    SimpleWordReader reader = new SimpleWordReader(TEST_DIR + "/test.txt");

    try {
      List<Word> words = reader.readWords();
      assertEquals(5, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWithUTF16() {
    SimpleWordReader reader = new SimpleWordReader(TEST_DIR + "/test.utf16.txt", "UTF16");

    try {
      List<Word> words = reader.readWords();
      assertEquals(5, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWithNonStandardOrder() {
    SimpleWordReader reader = new SimpleWordReader(TEST_DIR + "/test.order.txt", "UTF8", 0, 2, 1);

    try {
      List<Word> words = reader.readWords();
      assertEquals(5, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadsWithInvalidOrderDefaultsTo012() {
    SimpleWordReader reader = new SimpleWordReader(TEST_DIR + "/test.txt", "UTF8", 0, 0, 0);

    try {
      List<Word> words = reader.readWords();
      assertEquals(5, words.size());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }
}
