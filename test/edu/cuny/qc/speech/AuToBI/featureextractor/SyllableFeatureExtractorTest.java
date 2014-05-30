/*  SyllableFeatureExtractorTest.java

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SyllableFeatureExtractor
 *
 * @see SyllableFeatureExtractor
 */
@SuppressWarnings("unchecked")
public class SyllableFeatureExtractorTest {

  private SyllableFeatureExtractor fe;
  private List<Region> regions;
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    try {
      fe = new SyllableFeatureExtractor("phones", TEST_DIR + "/lexicon.txt");
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("syls"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("phones"));
  }

  @Test
  public void testExtractFeatureExtractsFeatures() {
    Word w = new Word(0, 1, "abandonment");
    List<Region> phones = new ArrayList<Region>();
    phones.add(new Region(0, 0, "a"));
    phones.add(new Region(0, 0, "b"));
    phones.add(new Region(0, 0, "ae"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "d"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "m"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "t"));
    w.setAttribute("phones", phones);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("syls"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureExtractsCVSyllablesOnOOV() {
    Word w = new Word(0, 1, "this word is not in the lexicon");
    List<Region> phones = new ArrayList<Region>();
    phones.add(new Region(0, 0, "a"));
    phones.add(new Region(0, 0, "b"));
    phones.add(new Region(0, 0, "ae"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "d"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "m"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "t"));
    w.setAttribute("phones", phones);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("syls"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "abandonment");
    List<Region> phones = new ArrayList<Region>();
    phones.add(new Region(0, 0, "a"));
    phones.add(new Region(0, 0, "b"));
    phones.add(new Region(0, 0, "ae"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "d"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "m"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "t"));
    w.setAttribute("phones", phones);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      List<Region> syllables = (List<Region>) w.getAttribute("syls");
      assertEquals(4, syllables.size());
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureExtractsCVSyllablesCorrectlyOnOOV() {
    Word w = new Word(0, 1, "this word is not in the lexicon");
    List<Region> phones = new ArrayList<Region>();
    phones.add(new Region(0, 0, "a"));
    phones.add(new Region(0, 0, "b"));
    phones.add(new Region(0, 0, "ae"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "d"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "m"));
    phones.add(new Region(0, 0, "ax"));
    phones.add(new Region(0, 0, "n"));
    phones.add(new Region(0, 0, "t"));
    w.setAttribute("phones", phones);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      List<Region> syllables = (List<Region>) w.getAttribute("syls");
      assertEquals(4, syllables.size());
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }
}
