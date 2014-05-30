/*  SubregionFeatureExtractorTest.java

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
 * Test class for SubregionFeatureExtractor
 *
 * @see SubregionFeatureExtractor
 */
public class SubregionFeatureExtractorTest {

  private SubregionFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorThrowsExceptionOnBadString() {
    try {
      fe = new SubregionFeatureExtractor("this is not a valid subregion description");
      fail();
    } catch (FeatureExtractorException e) {
    }
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    try {
      fe = new SubregionFeatureExtractor("200ms");
      assertEquals(1, fe.getExtractedFeatures().size());
      assertTrue(fe.getExtractedFeatures().contains("subregion[200ms]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    try {
      fe = new SubregionFeatureExtractor("200ms");
      assertEquals(0, fe.getRequiredFeatures().size());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeatures() {
    try {
      fe = new SubregionFeatureExtractor("200ms");
      Word w = new Word(0, 1, "test");
      regions.add(w);

      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("subregion[200ms]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectly() {
    try {
      fe = new SubregionFeatureExtractor("200ms");
      Word w = new Word(0, 1, "test");
      regions.add(w);

      fe.extractFeatures(regions);
      Region subr = (Region) w.getAttribute("subregion[200ms]");
      assertEquals(0.8, subr.getStart(), 0.0001);
      assertEquals(1.0, subr.getEnd(), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
