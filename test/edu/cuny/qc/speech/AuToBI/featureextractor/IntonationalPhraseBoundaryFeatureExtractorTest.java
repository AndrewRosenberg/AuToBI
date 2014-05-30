/*  IntonationalPhraseBoundaryFeatureExtractorTest.java

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for IntonationalPhraseBoundaryFeatureExtractor
 *
 * @see IntonationalPhraseBoundaryFeatureExtractor
 */
public class IntonationalPhraseBoundaryFeatureExtractorTest {


  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    IntonationalPhraseBoundaryFeatureExtractor fe =
        new IntonationalPhraseBoundaryFeatureExtractor();

    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("nominal_IntonationalPhraseBoundary"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    IntonationalPhraseBoundaryFeatureExtractor fe =
        new IntonationalPhraseBoundaryFeatureExtractor();

    assertEquals(0, fe.getRequiredFeatures().size());
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    IntonationalPhraseBoundaryFeatureExtractor fe =
        new IntonationalPhraseBoundaryFeatureExtractor();

    List<Region> regions = new ArrayList<Region>();
    Word w = new Word(0.0, 1.0, "test");
    regions.add(w);
    w.setPhraseAccent("L-");
    w.setPhraseAccent("L-");
    w.setBoundaryTone("H%");
    w.setBreakAfter("4");

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("nominal_IntonationalPhraseBoundary"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {

    IntonationalPhraseBoundaryFeatureExtractor fe =
        new IntonationalPhraseBoundaryFeatureExtractor();

    List<Region> regions = new ArrayList<Region>();
    Word w = new Word(0.0, 1.0, "test");
    regions.add(w);
    w.setPhraseAccent("L-");
    w.setBoundaryTone("H%");
    w.setBreakAfter("4");

    try {
      fe.extractFeatures(regions);

      assertEquals("INTONATIONAL_BOUNDARY", w.getAttribute("nominal_IntonationalPhraseBoundary"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }
}
