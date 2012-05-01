/*  ContextNormalizedFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.core.ContextDesc;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for ContextNormalizedFeatureExtractor
 *
 * @see ContextNormalizedFeatureExtractor
 */
public class ContextNormalizedFeatureExtractorTest {

  @Test
  public void testRequiredAndExtractedAttributeNames() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    assertTrue(fe.getExtractedFeatures().contains("attr_label__zMin"));
    assertTrue(fe.getExtractedFeatures().contains("attr_label__zMax"));
    assertTrue(fe.getExtractedFeatures().contains("attr_label__zMean"));
    assertTrue(fe.getExtractedFeatures().contains("attr_label__zNorm"));
    assertTrue(fe.getExtractedFeatures().contains("attr_label__rNorm"));
  }

  @Test
  public void testExtractFeaturesGeneratesNoFeatureWithNullFeature() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    try {
      fe.extractFeatures(regions);
      assertFalse(r.hasAttribute("attr"));
      assertFalse(r.hasAttribute("attr_label__zMin"));
      assertFalse(r.hasAttribute("attr_label__zMax"));
      assertFalse(r.hasAttribute("attr_label__zMean"));
      assertFalse(r.hasAttribute("attr_label__zNorm"));
      assertFalse(r.hasAttribute("attr_label__rNorm"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesGeneratesNoFeatureWithZeroLengthContour() {
    ContextNormalizedFeatureExtractor cnfe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, 0));

    try {
      cnfe.extractFeatures(regions);
      assertFalse(r.hasAttribute("attr_label__zMin"));
      assertFalse(r.hasAttribute("attr_label__zMax"));
      assertFalse(r.hasAttribute("attr_label__zMean"));
      assertFalse(r.hasAttribute("attr_label__zNorm"));
      assertFalse(r.hasAttribute("attr_label__rNorm"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }


  @Test
  public void testExtractFeaturesExtractsContourBasedFeatures() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 0));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));
    r.setAttribute("attr__min", 0.0);
    r.setAttribute("attr__max", 2.0);
    r.setAttribute("attr__mean", 0.5);
    r.setAttribute("attr__stdev", 0.2);

    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("attr_label__zMin"));
      assertTrue(r.hasAttribute("attr_label__zMax"));
      assertTrue(r.hasAttribute("attr_label__zMean"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsContourBasedFeatures() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 3.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));
    r.setAttribute("attr__min", 0.0);
    r.setAttribute("attr__max", 2.0);
    r.setAttribute("attr__mean", 0.5);
    r.setAttribute("attr__stdev", 0.2);

    try {
      fe.extractFeatures(regions);
      assertEquals(-0.78335, (Double) r.getAttribute("attr_label__zMin"), 0.0001);
      assertEquals(1.3055, (Double) r.getAttribute("attr_label__zMax"), 0.0001);
      assertEquals(-0.2611, (Double) r.getAttribute("attr_label__zMean"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsDoubleBasedFeatures() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 3.0, "test");
    Region r1 = new Region(3.0, 4.0, "test");
    regions.add(r);
    regions.add(r1);

    r.setAttribute("attr", 0.5);
    r.setAttribute("attr__min", 0.0);
    r.setAttribute("attr__max", 2.0);
    r.setAttribute("attr__mean", 0.5);
    r.setAttribute("attr__stdev", 0.2);
    r1.setAttribute("attr", 1.0);
    r1.setAttribute("attr__min", 0.0);
    r1.setAttribute("attr__max", 2.0);
    r1.setAttribute("attr__mean", 0.5);
    r1.setAttribute("attr__stdev", 0.2);

    try {
      fe.extractFeatures(regions);
      assertFalse(r.hasAttribute("attr_label__zNorm"));
      assertFalse(r.hasAttribute("attr_label__rNorm"));
      assertEquals(0.70711, (Double) r1.getAttribute("attr_label__zNorm"), 0.0001);
      assertEquals(1.0, (Double) r1.getAttribute("attr_label__rNorm"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
