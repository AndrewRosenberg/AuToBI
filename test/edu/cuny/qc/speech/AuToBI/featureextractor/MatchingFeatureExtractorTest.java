/*  MatchingFeatureExtractorTest.java

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
 * Test class for MatchingFeatureExtractor
 *
 * @see MatchingFeatureExtractor
 */
public class MatchingFeatureExtractorTest {
  private MatchingFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new MatchingFeatureExtractor("first", "second");
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("matching[first,second]"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("first"));
    assertTrue(fe.getRequiredFeatures().contains("second"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("first", "abcd");
    w.setAttribute("second", "notequal");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("matching[first,second]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectlyOnMatch() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("first", "abcd");
    w.setAttribute("second", "notequal");
    Word w2 = new Word(0, 1, "word");
    w2.setAttribute("first", "equal");
    w2.setAttribute("second", "equal");

    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertEquals("INCORRECT", w.getAttribute("matching[first,second]"));
      assertEquals("CORRECT", w2.getAttribute("matching[first,second]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFailsGracefully() {
    Word w = new Word(0, 1, "word");
    w.setAttribute("first", "abcd");
    Word w2 = new Word(0, 1, "word");
    w2.setAttribute("first", "equal");
    w2.setAttribute("second", "equal");

    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertFalse("Word has a constructed attribute when it shouldn't", w.hasAttribute("matching[first,second]"));
      assertEquals("CORRECT", w2.getAttribute("matching[first,second]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
