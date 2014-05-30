/*  SpectrumFeatureExtractorTest.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test class for SpectrumFeatureExtractor
 *
 * @see SpectrumFeatureExtractor
 */
public class SpectrumFeatureExtractorTest {
  private SpectrumFeatureExtractor fe;
  private List<Region> regions;

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Before
  public void setUp() throws Exception {
    fe = new SpectrumFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testDefaultConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("spectrum"));
  }

  @Test
  public void testDefaultConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("wav"));
  }

  @Test
  public void testArgConstructorSetsExtractedFeaturesCorrectly() {
    fe = new SpectrumFeatureExtractor(0.1, 0.05);
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("spectrum"));
  }

  @Test
  public void testArgConstructorSetsRequiredFeaturesCorrectly() {
    fe = new SpectrumFeatureExtractor(0.1, 0.05);
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("wav"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    regions.add(w);

    WavReader reader = new WavReader();
    WavData wav = null;
    try {
      wav = reader.read(TEST_DIR + "/test.wav");
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    w.setAttribute("wav", wav);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("spectrum"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test");
    regions.add(w);

    WavReader reader = new WavReader();
    WavData wav = null;
    try {
      wav = reader.read(TEST_DIR + "/bdc-test.wav");
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
    w.setAttribute("wav", wav);

    try {
      fe.extractFeatures(regions);
      Spectrum s = (Spectrum) w.getAttribute("spectrum");
      assertEquals(835, s.numFrames());
      assertEquals(256, s.numFreqs());
      // Assume that the spectrum extraction algorithm is tested in SpectrumExtractor.
      // Here we'll make sure that the generated spectrum passes some sanity checks.
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesFailsGracefullyWithNoWavData() {
    Word w = new Word(0, 1, "test");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertFalse(w.hasAttribute("spectrum"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesFailsGracefullyWithVeryShortRegion() {
    Word w = new Word(0, 0.001, "test");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertFalse(w.hasAttribute("spectrum"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
