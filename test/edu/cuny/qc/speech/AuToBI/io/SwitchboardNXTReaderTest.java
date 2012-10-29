/*  SwitchboardNXTReaderTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.io.NXTTier
 */
public class SwitchboardNXTReaderTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadsWords() {
    String filestem = TEST_DIR + "/test.swb";
    SwitchboardNXTReader reader = new SwitchboardNXTReader(filestem);

    try {
      List<Word> words = reader.readWords();

      assertEquals(571, words.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


}
