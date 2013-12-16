/**
 EMSyllabifierTest.java

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
package edu.cuny.qc.speech.AuToBI.core.syllabifier;


import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test class for EMSyllabifier class.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.syllabifier.EMSyllabifier
 */
public class VillingSyllabifierTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private VillingSyllabifier vs;

  @Before
  public void setup() {
    vs = new VillingSyllabifier();
  }

  @Test
  public void testSyllabifierIDsSyllables() {
    WavReader r = new WavReader();
    WavData wav = null;
    try {
      wav = r.read(TEST_DIR + "/test_villing.wav");
    } catch (UnsupportedAudioFileException e) {
      fail();
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
    List<Region> regions = vs.generatePseudosyllableRegions(wav);

    // Note: there are 12 true syllables in the file.  If the approach is tuned to become more accurate,
    // the expected value may change.
    assertEquals(14, regions.size());
  }
}
