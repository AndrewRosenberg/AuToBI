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

    assertTrue(fe.getExtractedFeatures().contains("zMinWordContext[attr,label]"));
    assertTrue(fe.getExtractedFeatures().contains("zMaxWordContext[attr,label]"));
    assertTrue(fe.getExtractedFeatures().contains("zMeanWordContext[attr,label]"));
    assertTrue(fe.getExtractedFeatures().contains("zNormWordContext[attr,label]"));
    assertTrue(fe.getExtractedFeatures().contains("rNormWordContext[attr,label]"));
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
      assertFalse(r.hasAttribute("zMinWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMaxWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMeanWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zNormWordContext[attr,label]"));
      assertFalse(r.hasAttribute("rNormWordContext[attr,label]"));
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
      assertFalse(r.hasAttribute("zMinWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMaxWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMeanWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zNormWordContext[attr,label]"));
      assertFalse(r.hasAttribute("rNormWordContext[attr,label]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesFailsGracefullyWithNoRegions() {
    ContextNormalizedFeatureExtractor cnfe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();

    try {
      cnfe.extractFeatures(regions);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesGeneratesNoFeatureWithZeroLengthRegion() {
    ContextNormalizedFeatureExtractor cnfe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 1));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));
    r.setAttribute("min[attr]", 0.0);
    r.setAttribute("max[attr]", 2.0);
    r.setAttribute("mean[attr]", 0.5);
    r.setAttribute("stdev[attr]", 0.2);


    try {
      cnfe.extractFeatures(regions);
      assertFalse(r.hasAttribute("zMinWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMaxWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zMeanWordContext[attr,label]"));
      assertFalse(r.hasAttribute("zNormWordContext[attr,label]"));
      assertFalse(r.hasAttribute("rNormWordContext[attr,label]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }


  @Test
  public void testExtractFeaturesExtractsContourBasedFeatures() {
    FeatureExtractor fe =
        new ContextNormalizedFeatureExtractor("attr", new ContextDesc("label", 0, 0));

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 3.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));
    r.setAttribute("min[attr]", 0.0);
    r.setAttribute("max[attr]", 2.0);
    r.setAttribute("mean[attr]", 0.5);
    r.setAttribute("stdev[attr]", 0.2);

    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("zMinWordContext[attr,label]"));
      assertTrue(r.hasAttribute("zMaxWordContext[attr,label]"));
      assertTrue(r.hasAttribute("zMeanWordContext[attr,label]"));
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
    r.setAttribute("min[attr]", 0.0);
    r.setAttribute("max[attr]", 2.0);
    r.setAttribute("mean[attr]", 0.5);
    r.setAttribute("stdev[attr]", 0.2);

    try {
      fe.extractFeatures(regions);
      assertEquals(-0.78335, (Double) r.getAttribute("zMinWordContext[attr,label]"), 0.0001);
      assertEquals(1.3055, (Double) r.getAttribute("zMaxWordContext[attr,label]"), 0.0001);
      assertEquals(-0.2611, (Double) r.getAttribute("zMeanWordContext[attr,label]"), 0.0001);
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
    r.setAttribute("min[attr]", 0.0);
    r.setAttribute("max[attr]", 2.0);
    r.setAttribute("mean[attr]", 0.5);
    r.setAttribute("stdev[attr]", 0.2);
    r1.setAttribute("attr", 1.0);
    r.setAttribute("min[attr]", 0.0);
    r.setAttribute("max[attr]", 2.0);
    r.setAttribute("mean[attr]", 0.5);
    r.setAttribute("stdev[attr]", 0.2);

    try {
      fe.extractFeatures(regions);
      assertEquals(0.70711, (Double) r1.getAttribute("zNormWordContext[attr,label]"), 0.0001);
      assertEquals(1.0, (Double) r1.getAttribute("rNormWordContext[attr,label]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
