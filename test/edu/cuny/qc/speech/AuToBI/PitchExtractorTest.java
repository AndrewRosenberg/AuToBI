/*  PitchExtractorTest.java

    Copyright (c) 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class for PitchExtractor.
 */
public class PitchExtractorTest {

  @Test
  public void testPitchExtractorRunsWithoutException() {
    String inFile = System.getenv().get("AUTOBI_TEST_DIR") + "/test.wav";
    WavReader reader = new WavReader();
    WavData inWave = null;
    try {
      inWave = reader.read(inFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    PitchExtractor pe = new PitchExtractor(inWave);
    try {
      Contour c = pe.soundToPitch();
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testPitchExtractorGeneratesAReasonableNumberOfPitchFrames() {
    String inFile = System.getenv().get("AUTOBI_TEST_DIR") + "/test.wav";
    WavReader reader = new WavReader();
    WavData inWave = null;
    try {
      inWave = reader.read(inFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    PitchExtractor pe = new PitchExtractor(inWave);
    try {
      Contour c = pe.soundToPitch();
      assertEquals(c.size(), (inWave.getDuration() - 2 * c.getStart()) / c.getStep() + 1, 0.5);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testPitchExtractorGeneratesAReasonableNumberOfPitchFramesWith8khzFile() {
    String inFile = System.getenv().get("AUTOBI_TEST_DIR") + "/test.8k.wav";
    WavReader reader = new WavReader();
    WavData inWave = null;
    try {
      inWave = reader.read(inFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    PitchExtractor pe = new PitchExtractor(inWave);
    try {
      Contour c = pe.soundToPitch();
      assertEquals(c.size(), (inWave.getDuration() - 2 * c.getStart()) / c.getStep() + 1, 0.5);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testPitchExtractorGeneratesCorrectLengthsWithEmptyFrames() {
    String inFile = System.getenv().get("AUTOBI_TEST_DIR") + "/test.zero.wav";
    WavReader reader = new WavReader();
    WavData inWave = null;
    try {
      inWave = reader.read(inFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    PitchExtractor pe = new PitchExtractor(inWave);
    try {
      Contour c = pe.soundToPitch();
      assertEquals(c.size(), (inWave.getDuration() - 2 * c.getStart()) / c.getStep() + 1, 0.5);
    } catch (AuToBIException e) {
      fail();
    }
  }
}
