/*  DurationFeatureExtractorTest.java

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
import static org.junit.Assert.*;

/**
 * Test class for DurationFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.DurationFeatureExtractor
 */
public class DurationFeatureExtractorTest {

  @Test
  public void testSetAttributeNameCorrectlySetsExtractedFeatures() {
    DurationFeatureExtractor fe = new DurationFeatureExtractor();

    List<String> extracted_features = fe.getExtractedFeatures();

    assertTrue(extracted_features.contains("duration"));
    assertTrue(extracted_features.contains("prevPause"));
    assertTrue(extracted_features.contains("follPause"));
    assertTrue(extracted_features.contains("nominal_precedesSilence"));
    assertTrue(extracted_features.contains("nominal_followsSilence"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      regions.add(w);

      Word w1 = new Word(1, 2, "test");
      regions.add(w1);

      Word w2 = new Word(2.5, 4, "test");
      regions.add(w2);

      Word w3 = new Word(4, 5.5, "test");
      regions.add(w3);

      Word w4 = new Word(5.7, 8, "test");
      regions.add(w4);

      ArrayList<String> features = new ArrayList<String>();
      features.add("attr");

      DurationFeatureExtractor fe = new DurationFeatureExtractor();
      fe.extractFeatures(regions);

      assertTrue(w2.hasAttribute("duration"));
      assertTrue(w2.hasAttribute("prevPause"));
      assertTrue(w2.hasAttribute("follPause"));
      assertTrue(w2.hasAttribute("nominal_precedesSilence"));
      assertTrue(w2.hasAttribute("nominal_followsSilence"));

    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      regions.add(w);

      Word w1 = new Word(1, 2, "test");
      regions.add(w1);

      Word w2 = new Word(2.5, 4, "test");
      regions.add(w2);

      Word w3 = new Word(4, 5.5, "test");
      regions.add(w3);

      Word w4 = new Word(5.7, 8, "test");
      regions.add(w4);

      ArrayList<String> features = new ArrayList<String>();
      features.add("attr");

      DurationFeatureExtractor fe = new DurationFeatureExtractor();
      fe.extractFeatures(regions);

      assertEquals(1.5, (Double) w2.getAttribute("duration"), 0.0001);
      assertEquals(0.5, (Double) w2.getAttribute("prevPause"), 0.0001);
      assertEquals(0.0, (Double) w2.getAttribute("follPause"), 0.0001);
      assertEquals("FALSE", w2.getAttribute("nominal_precedesSilence"));
      assertEquals("TRUE", w2.getAttribute("nominal_followsSilence"));

    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesSmallNegativePausesAreZero() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      regions.add(w);

      Word w1 = new Word(0.9999, 2, "test");
      regions.add(w1);

      Word w2 = new Word(2.5, 4, "test");
      regions.add(w2);

      Word w3 = new Word(4, 5.5, "test");
      regions.add(w3);

      Word w4 = new Word(5.7, 8, "test");
      regions.add(w4);

      ArrayList<String> features = new ArrayList<String>();
      features.add("attr");

      DurationFeatureExtractor fe = new DurationFeatureExtractor();
      fe.extractFeatures(regions);

      assertEquals(0.0, (Double) w1.getAttribute("prevPause"), 0.0001);

    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
