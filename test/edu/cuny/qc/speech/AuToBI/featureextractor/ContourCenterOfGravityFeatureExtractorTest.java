/*  ContourCenterOfGravityFeatureExtractorTest.java

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for ContourCenterOfGravityFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.ContourCenterOfGravityFeatureExtractor
 */
public class ContourCenterOfGravityFeatureExtractorTest {

  @Test
  public void testExtractFeaturesGeneratesNoFeatureWithNullFeature() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    try {
      ccogfe.extractFeatures(regions);
      assertFalse(r.hasAttribute("attr__cog"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesGeneratesNoFeatureWithZeroLengthContour() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, 0));

    try {
      ccogfe.extractFeatures(regions);
      assertFalse(r.hasAttribute("attr__cog"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testRequiredAndExtractedAttributeNames() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    assertTrue(ccogfe.getRequiredFeatures().contains("attr"));
    assertTrue(ccogfe.getExtractedFeatures().contains("cog[attr]"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 1.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, .1, new double[]{0.0, 0.0, 1.0, 2.0}));

    try {
      ccogfe.extractFeatures(regions);
      assertTrue(r.hasAttribute("cog[attr]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesDoesNotExtractFeaturesOnZeroLengthedRegions() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 0.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));

    try {
      ccogfe.extractFeatures(regions);
      assertFalse(r.hasAttribute("cog[attr]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeatures() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    List<Region> regions = new ArrayList<Region>();
    Region r = new Region(0.0, 3.0, "test");
    regions.add(r);

    r.setAttribute("attr", new Contour(0, 1, new double[]{0.0, 0.0, 1.0, 2.0}));

    try {
      ccogfe.extractFeatures(regions);
      assertEquals(0.88888, (Double) r.getAttribute("cog[attr]"), 0.00001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
