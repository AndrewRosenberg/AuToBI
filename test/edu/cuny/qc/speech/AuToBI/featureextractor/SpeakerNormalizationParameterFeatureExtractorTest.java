/*  SpeakerNormalizationParameterFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test class for SpeakerNormalizationParameterFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.SpeakerNormalizationParameterFeatureExtractor
 */
public class SpeakerNormalizationParameterFeatureExtractorTest {
  private SpeakerNormalizationParameterFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new SpeakerNormalizationParameterFeatureExtractor("speaker_id", "dest");
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("dest"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("f0"));
    assertTrue(fe.getRequiredFeatures().contains("I"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("speaker_id", "spkr1");
    w.setAttribute("I", new Contour(0, 0.1, new double[]{1, 2, 3, 4, 5}));
    w.setAttribute("f0", new Contour(0, 0.1, new double[]{11, 12, 13, 14, 15}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("dest"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("speaker_id", "spkr1");
    w.setAttribute("I", new Contour(0, 0.1, new double[]{1, 2, 3, 4, 5}));
    w.setAttribute("f0", new Contour(0, 0.1, new double[]{11, 12, 13, 14, 15}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      SpeakerNormalizationParameter norm_params = (SpeakerNormalizationParameter) w.getAttribute("dest");
      // TODO: Fix this test when SpeakerNormalizationParameters get a getter method for easier inspection.
      assertEquals("f0: mean 13.0 - stdev 1.5811388300841898\n" +
          "I: mean 3.0 - stdev 1.5811388300841898", norm_params.toString());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
