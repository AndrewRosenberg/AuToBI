/*  NormalizedContourFeatureExtractor.java

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
 * Test class for NormalizationParameterFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.NormalizationParameterFeatureExtractor
 */
public class NormalizedContourFeatureExtractorTest {
  private NormalizedContourFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new NormalizedContourFeatureExtractor("f0", "normparams");
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("norm_f0"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("f0"));
    assertTrue(fe.getRequiredFeatures().contains("normparams"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{10, 11, 12, 13}));
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();
    snp.insertPitch(10);
    snp.insertPitch(11);
    snp.insertPitch(12);
    snp.insertPitch(13);
    w.setAttribute("normparams", snp);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("norm_f0"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{10, 11, 12, 13}));
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();
    snp.insertPitch(10);
    snp.insertPitch(11);
    snp.insertPitch(12);
    snp.insertPitch(13);
    w.setAttribute("normparams", snp);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      Contour c = (Contour) w.getAttribute("norm_f0");
      assertEquals(4, c.size());
      assertEquals(0.1, c.getStep());
      assertEquals(-1.16189, c.get(0), 0.0001);
      assertEquals(-0.38729, c.get(1), 0.0001);
      assertEquals(0.38729, c.get(2), 0.0001);
      assertEquals(1.16189, c.get(3), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefullyWithNoPitch() {
    Word w = new Word(0, 1, "word");
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();
    snp.insertPitch(10);
    snp.insertPitch(11);
    snp.insertPitch(12);
    snp.insertPitch(13);
    w.setAttribute("normparams", snp);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertFalse(w.hasAttribute("norm_f0"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefullyWithNoSNP() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{10, 11, 12, 13}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertFalse(w.hasAttribute("norm_f0"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
