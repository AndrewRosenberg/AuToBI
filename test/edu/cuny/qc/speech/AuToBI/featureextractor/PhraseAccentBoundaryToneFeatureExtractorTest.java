/*  PhraseAccentBoundaryToneFeatureExtractor.java

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

import static junit.framework.Assert.*;

/**
 * Test class for PhraseAccentBoundaryToneFeatureExtractor
 *
 * @see PhraseAccentBoundaryToneFeatureExtractor
 */
public class PhraseAccentBoundaryToneFeatureExtractorTest {
  private PhraseAccentBoundaryToneFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new PhraseAccentBoundaryToneFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("nominal_PhraseAccentBoundaryTone"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(0, fe.getRequiredFeatures().size());
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "word");
    w.setPhraseAccent("L-");
    w.setBoundaryTone("H%");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("nominal_PhraseAccentBoundaryTone"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "word");
    w.setPhraseAccent("L-");
    w.setBoundaryTone("H%");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals("L-Hx", w.getAttribute("nominal_PhraseAccentBoundaryTone"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsCorrectlyWithNoBoundaryTone() {
    Word w = new Word(0, 1, "word");
    w.setPhraseAccent("L-");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("nominal_PhraseAccentBoundaryTone"));
      assertEquals("NOTONE", w.getAttribute("nominal_PhraseAccentBoundaryTone"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
