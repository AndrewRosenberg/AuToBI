/*  NormalizationParameterFeatureExtractor.java

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
public class NormalizationParameterFeatureExtractorTest {
  private NormalizationParameterFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new NormalizationParameterFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("spkrNormParams"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("f0"));
    assertTrue(fe.getRequiredFeatures().contains("I"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{10, 11, 12, 13}));
    w.setAttribute("I", new Contour(0, .1, new double[]{0, 1, 2, 3}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("spkrNormParams"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectlyOnMatch() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{10, 11, 12, 13}));
    w.setAttribute("I", new Contour(0, .1, new double[]{0, 1, 2, 3}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      SpeakerNormalizationParameter snp = (SpeakerNormalizationParameter) w.getAttribute("spkrNormParams");
      assertEquals("f0: mean 11.5 - stdev 1.2909944487358056\n" +
          "I: mean 1.5 - stdev 1.2909944487358056", snp.toString());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefullyWithNoPitch() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("I", new Contour(0, .1, new double[]{0, 1, 2, 3}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("spkrNormParams"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefullyWithNoIntensity() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("f0", new Contour(0, .1, new double[]{0, 1, 2, 3}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("spkrNormParams"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefullyWithNoAcoustics() {
    Word w = new Word(0, 1, "word");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("spkrNormParams"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
