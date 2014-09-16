/*  PitchFeatureExtractor.java

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
 * Test class for PitchFeatureExtractor
 *
 * @see PitchFeatureExtractor
 */
public class PitchFeatureExtractorTest {
  private PitchFeatureExtractor fe;
  private List<Region> regions;
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Before
  public void setUp() throws Exception {
    fe = new PitchFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("f0"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("wav"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "word");
    w.setAccent("H*");
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
      assertTrue(w.hasAttribute("f0"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "word");
    w.setAccent("H*");
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
      Contour c = (Contour) w.getAttribute("f0");
      // Assume that Pitch Extraction is tested in PitchExtractor
      // We'll do some sanity checks here.
      assertEquals(100, c.size());
      assertEquals(0.01, c.getStep(), 0.0001);
      assertEquals(0.0037499999161809683, c.getStart(), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }


  @Test
  public void testExtractFeaturesAssignsTheFullContour() {
    Word w = new Word(0.25, 0.75, "word");
    w.setAccent("H*");
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
      Contour c = (Contour) w.getAttribute("f0");
      // Assume that Pitch Extraction is tested in PitchExtractor
      // We'll do some sanity checks here.
      assertEquals(100, c.size());
      assertEquals(0.01, c.getStep(), 0.0001);
      assertEquals(0.0037499999161809683, c.getStart(), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesAssignsTheSameObjectToSubsequentRegions() {
    Word w = new Word(0.25, 0.50, "word");
    Word w2 = new Word(0.50, 0.75, "word");
    w.setAccent("H*");
    w2.setAccent("H*");
    regions.add(w);
    regions.add(w2);

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
    w2.setAttribute("wav", wav);
    try {
      fe.extractFeatures(regions);
      Contour c = (Contour) w.getAttribute("f0");
      Contour c2 = (Contour) w2.getAttribute("f0");
      assertTrue(c == c2);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
