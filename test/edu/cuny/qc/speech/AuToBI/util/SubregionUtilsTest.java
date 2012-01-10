/*  SubregionUtilsTest.java

    Copyright 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import org.junit.Test;
import sun.java2d.pipe.RegionSpanIterator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 1/4/11 Time: 10:30 AM To change this template use File | Settings | File
 * Templates.
 */
public class SubregionUtilsTest {

  @Test
  public void testAlignLongestSubregion() {
    Word word = new Word(0, 10, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 6, "test");
    Region subr4 = new Region(7, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word.hasAttribute("subregion"));

    assertEquals(subr3, word.getAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithEarlySubregions() {
    Word word = new Word(3, 10, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 6, "test");
    Region subr4 = new Region(7, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word.hasAttribute("subregion"));

    assertEquals(subr3, word.getAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithLateSubregions() {
    Word word = new Word(1, 6.5, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 6, "test");
    Region subr4 = new Region(7, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word.hasAttribute("subregion"));

    assertEquals(subr3, word.getAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithNoSubregions() {
    Word word = new Word(3, 10, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 2.5, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertNull(word.getAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithMultipleWords() {
    Word word1 = new Word(1, 6.5, "test");
    Word word2 = new Word(8, 9, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 6, "test");
    Region subr4 = new Region(7, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word1);
    words.add(word2);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word1.hasAttribute("subregion"));
    assertEquals(subr3, word1.getAttribute("subregion"));

    assertFalse(word2.hasAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithMultipleWordsAndSubregionOverlap() {
    Word word1 = new Word(1, 6.5, "test");
    Word word2 = new Word(6.5, 9, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 6, "test");
    Region subr4 = new Region(6, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word1);
    words.add(word2);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word1.hasAttribute("subregion"));
    assertEquals(subr3, word1.getAttribute("subregion"));

    assertTrue(word2.hasAttribute("subregion"));
    assertEquals(subr4, word2.getAttribute("subregion"));
  }

  @Test
  public void testAlignLongestSubregionWithMultipleWordsAndDanglingWord() {
    Word word1 = new Word(1, 6.5, "test");
    Word word2 = new Word(6.5, 9, "test");

    Region subr1 = new Region(1, 2, "test");
    Region subr2 = new Region(2, 3, "test");
    Region subr3 = new Region(3, 7, "test");
    Region subr4 = new Region(7, 8, "test");

    List<Word> words = new ArrayList<Word>();
    words.add(word1);
    words.add(word2);

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(subr1);
    subregions.add(subr2);
    subregions.add(subr3);
    subregions.add(subr4);

    SubregionUtils.alignLongestSubregionsToWords(words, subregions, "subregion");

    assertTrue(word1.hasAttribute("subregion"));
    assertEquals(subr3, word1.getAttribute("subregion"));

    assertTrue(word2.hasAttribute("subregion"));
    assertEquals(subr4, word2.getAttribute("subregion"));
  }

  @Test
  public void testParseSubregionNameMilliseconds() {
    String subregionName = "100ms";

    try {
      double size = SubregionUtils.parseSubregionName(subregionName);
      assertEquals(0.1, size, 0.0001);
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testParseSubregionNameSeconds() {
    String subregionName = "10s";

    try {
      double size = SubregionUtils.parseSubregionName(subregionName);
      assertEquals(10.0, size, 0.0001);
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testParseSubregionNameUnknownUnit() {
    String subregionName = "10d";

    try {
      double size = SubregionUtils.parseSubregionName(subregionName);
      fail();
    } catch (FeatureExtractorException e) {
      // Expected
    }
  }

  @Test
  public void testParseSubregionNameMinutes() {
    String subregionName = "10m";

    try {
      double size = SubregionUtils.parseSubregionName(subregionName);
      fail();
    } catch (FeatureExtractorException e) {
      // Expected
    }
  }

  @Test
  public void testParseSubregionNameFractionalSize() {
    String subregionName = "0.50s";

    try {
      double size = SubregionUtils.parseSubregionName(subregionName);
      fail();
    } catch (FeatureExtractorException e) {
      // Expected
    }
  }

  @Test
  public void testSetAttributeToSubregion() {
    Region r = new Region(0, 10, "test");
    r.setAttribute("attribute", "valuevalue");

    List<Region> regions = new ArrayList<Region>();
    regions.add(r);

    Region r1 = new Region(1, 2, "subr1");
    r.setAttribute("subregion", r1);

    SubregionUtils.assignFeatureToSubregions(regions, "subregion", "attribute");

    assertTrue(r1.hasAttribute("attribute"));
    assertEquals("valuevalue", r1.getAttribute("attribute"));
  }

  @Test
  public void testSetAttributeToAllSubregions() {
    Region r = new Region(0, 10, "test");
    r.setAttribute("attribute", "valuevalue");

    List<Region> regions = new ArrayList<Region>();
    regions.add(r);

    Region r1 = new Region(1, 2, "subr1");
    Region r2 = new Region(2, 3, "subr2");

    List<Region> subregions = new ArrayList<Region>();
    subregions.add(r1);
    subregions.add(r2);

    r.setAttribute("subregions", subregions);

    SubregionUtils.assignFeatureToAllSubregions(regions, "subregions", "attribute");

    assertTrue(r1.hasAttribute("attribute"));
    assertEquals("valuevalue", r1.getAttribute("attribute"));

    assertTrue(r2.hasAttribute("attribute"));
    assertEquals("valuevalue", r2.getAttribute("attribute"));
  }

  @Test
  public void testGetSlice() {
    WavData wavData = new WavData();

    wavData.sampleRate = 10;
    wavData.sampleSize = 8;
    wavData.numberOfChannels = 1;
    wavData.t0 = 4;

    wavData.samples = new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}};

    try {
      WavData subwav = SubregionUtils.getSlice(wavData, 4.09, 4.31);

      assertEquals(4.10, subwav.t0, 0.001);

    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetSliceBeyondTheEnd() {
    WavData wavData = new WavData();

    wavData.sampleRate = 10;
    wavData.sampleSize = 8;
    wavData.numberOfChannels = 1;
    wavData.t0 = 4;

    wavData.samples = new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}};

    try {
      WavData subwav = SubregionUtils.getSlice(wavData, 8.09, 8.31);

      assertNull(subwav);

    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetSliceBeforeTheBeginning() {
    WavData wavData = new WavData();

    wavData.sampleRate = 10;
    wavData.sampleSize = 8;
    wavData.numberOfChannels = 1;
    wavData.t0 = 4;

    wavData.samples = new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}};

    try {
      WavData subwav = SubregionUtils.getSlice(wavData, 1.09, 1.31);

      assertNull(subwav);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetSliceEmptyRegion() {
    WavData wavData = new WavData();

    wavData.sampleRate = 10;
    wavData.sampleSize = 8;
    wavData.numberOfChannels = 1;
    wavData.t0 = 4;

    wavData.samples = new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}};

    try {
      WavData subwav = SubregionUtils.getSlice(wavData, 4.09, 4.09);
      fail();
    } catch (AuToBIException e) {
      // Expected
    }
  }

  @Test
  public void testGetSliceNegativeSize() {
    WavData wavData = new WavData();

    wavData.sampleRate = 10;
    wavData.sampleSize = 8;
    wavData.numberOfChannels = 1;
    wavData.t0 = 4;

    wavData.samples = new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}};

    try {
      WavData subwav = SubregionUtils.getSlice(wavData, 5.09, 4.09);
      fail();
    } catch (AuToBIException e) {
      // Expected
    }
  }
}
