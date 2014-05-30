/*  PseudosyllableFeatureExtractorTest.java

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
 * Test class for PseudosyllableFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.PseudosyllableFeatureExtractor
 */
public class PseudosyllableFeatureExtractorTest {
  private PseudosyllableFeatureExtractor fe;
  private List<Region> regions;
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Before
  public void setUp() throws Exception {
    fe = new PseudosyllableFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("psyl"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("wav"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(4.3, 5, "word");
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
      assertTrue(w.hasAttribute("psyl"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(4.3, 5, "word");
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
      Region syl = (Region) w.getAttribute("psyl");
      // Don't worry about the specific region, but make sure that there is overlap.
      // Different pseudosyllabification algorithms will yield different hypotheses here
      // this test shouldn't break if the internal algorithms is modified.
      assertTrue("Syllable does not overlap the word", syl.getEnd() > w.getStart() && syl.getStart() < w.getEnd());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
