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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Test class for edu.cuny.qc.speech.AuToBI.io.NXTTier
 */
public class WavReaderTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadsWavFile() {
    WavReader reader = new WavReader();

    try {
      WavData wav = reader.read(TEST_DIR + "/test.wav");
      assertEquals(44100.0, wav.sampleRate, 0.0001);
      assertEquals(16, wav.sampleSize);
      assertEquals(1.0, wav.getDuration());
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }


  @Test
  public void testReadsSectionOfWavFile() {
    WavReader reader = new WavReader();

    try {
      WavData wav = reader.read(TEST_DIR + "/test.wav", 0.2, 0.3);

      assertEquals(44100.0, wav.sampleRate, 0.0001);
      assertEquals(16, wav.sampleSize);
      assertEquals(0.1, wav.getDuration(), 0.00001);
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testThrowsExceptionIfNoFileExists() {
    WavReader reader = new WavReader();

    try {
      reader.read(TEST_DIR + "/nosuchfile.wav");
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      // expected.
    }
  }
}
