/*  SubregionResetFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SubregionResetFeatureExtractor
 *
 * @see SubregionResetFeatureExtractor
 */
public class SubregionResetFeatureExtractorTest {

  private SubregionResetFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorThrowsExceptionOnBadString() {
    try {
      fe = new SubregionResetFeatureExtractor("this is not a valid subregion description");
      fail();
    } catch (FeatureExtractorException e) {
    }
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    try {
      fe = new SubregionResetFeatureExtractor("200ms");
      assertEquals(2, fe.getExtractedFeatures().size());
      assertTrue(fe.getExtractedFeatures().contains("van[200ms]"));
      assertTrue(fe.getExtractedFeatures().contains("trail[200ms]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    try {
      fe = new SubregionResetFeatureExtractor("200ms");
      assertEquals(0, fe.getRequiredFeatures().size());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeatures() {
    try {
      fe = new SubregionResetFeatureExtractor("200ms");
      Word w = new Word(0, 1, "test");
      regions.add(w);

      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("van[200ms]"));
      assertTrue(w.hasAttribute("trail[200ms]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectly() {
    try {
      fe = new SubregionResetFeatureExtractor("200ms");
      Word w = new Word(0, 1, "test");
      regions.add(w);

      fe.extractFeatures(regions);
      Region van_200ms = (Region) w.getAttribute("van[200ms]");
      assertEquals(0.8, van_200ms.getStart(), 0.0001);
      assertEquals(1.0, van_200ms.getEnd(), 0.0001);
      Region trail = (Region) w.getAttribute("trail[200ms]");
      assertEquals(0.0, trail.getStart(), 0.0001);
      assertEquals(0.2, trail.getEnd(), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
