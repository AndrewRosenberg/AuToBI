/*  AlignmentUtilsTest.java

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
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.*;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.util.AuToBIUtilsTest
 */
public class AlignmentUtilsTest {

  @Test
  public void testCopyToBITonesByTimePitchAccent() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone = new Region(0.5, 0.5, "H*");
    tones.add(tone);

    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertEquals("H*", w.getAccent());
  }

  @Test
  public void testCopyToBITonesByTimePitchAccentHandlesSeparatedComplexTones() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone_a = new Region(0.5, 0.5, "H+");
    Region tone_b = new Region(0.75, 0.75, "+L*");
    tones.add(tone_a);
    tones.add(tone_b);

    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertEquals("H+L*", w.getAccent());
  }

  @Test
  public void testCopyToBITonesByTimePhraseAccent() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone = new Region(0.5, 0.5, "H-");
    tones.add(tone);

    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertEquals("H-", w.getPhraseAccent());
  }

  @Test
  public void testCopyToBITonesByTimeBoundaryTone() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone = new Region(0.5, 0.5, "H%");
    tones.add(tone);
    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertEquals("H%", w.getBoundaryTone());
  }

  @Test
  public void testCopyToBITonesByTimePitchAccentOutOfRangeLate() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone = new Region(1.5, 1.5, "H*");
    tones.add(tone);

    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertNull(w.getAccent());
  }

  @Test
  public void testCopyToBITonesByTimePitchAccentOutOfRangeEarly() {
    List<Word> words = new ArrayList<Word>();
    Word w = new Word(0.5, 1, "test");
    words.add(w);

    List<Region> tones = new ArrayList<Region>();
    Region tone = new Region(0.0, 0.0, "H*");
    tones.add(tone);

    AlignmentUtils.copyToBITonesByTime(words, tones);

    assertNull(w.getAccent());
  }

  @Test
  public void testCopyToBIBreaksBreakAfter() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    words.add(w1);
    Word w2 = new Word(2, 3, "test");
    words.add(w2);

    List<Region> breaks = new ArrayList<Region>();
    Region b1 = new Region(2, 2, "3");
    Region b2 = new Region(2, 2, "4");
    breaks.add(b1);
    breaks.add(b2);

    try {
      AlignmentUtils.copyToBIBreaks(words, breaks);
      assertEquals("3", w1.getBreakAfter());
      assertEquals("4", w2.getBreakAfter());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCopyToBIBreaksBreakBefore() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    words.add(w1);
    Word w2 = new Word(2, 3, "test");
    words.add(w2);

    List<Region> breaks = new ArrayList<Region>();
    Region b1 = new Region(2, 2, "3");
    Region b2 = new Region(2, 2, "4");
    breaks.add(b1);
    breaks.add(b2);

    try {
      AlignmentUtils.copyToBIBreaks(words, breaks);
      assertNull(w1.getBreakBefore());
      assertEquals("3", w2.getBreakBefore());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCopyToBIBreaksMismatch() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    words.add(w1);
    Word w2 = new Word(2, 3, "test");
    words.add(w2);

    List<Region> breaks = new ArrayList<Region>();
    Region b1 = new Region(2, 2, "3");

    breaks.add(b1);


    try {
      AlignmentUtils.copyToBIBreaks(words, breaks);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testCopyToBIBreaksByTimeAssignsBreaksToRegionsAfterTheLastBreak() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    words.add(w1);
    Word w2 = new Word(2, 3, "test");
    words.add(w2);

    Word w3 = new Word(4, 5, "test");
    words.add(w3);

    List<Region> breaks = new ArrayList<Region>();
    Region b1 = new Region(2, 2, "3");
    Region b2 = new Region(3, 3, "4");
    breaks.add(b1);
    breaks.add(b2);

    AlignmentUtils.copyToBIBreaksByTime(words, breaks);
    assertNotNull(w1.getBreakBefore());
    assertNotNull(w1.getBreakAfter());
    assertNotNull(w2.getBreakBefore());
    assertNotNull(w2.getBreakAfter());
    assertNotNull(w3.getBreakBefore());
    assertNotNull(w3.getBreakAfter());
  }

  @Test
  public void testCopyToBITonesByIndexWhenAlignedByTime() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "H*");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(2.55, 2.55, "H-");
    Region t4 = new Region(2.75, 2.75, "L%");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);
    tones.add(t4);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      assertEquals("H*", w1.getAccent());
      assertNull(w1.getPhraseAccent());
      assertNull(w1.getBoundaryTone());
      assertEquals("L+H*", w2.getAccent());
      assertEquals("H-", w2.getPhraseAccent());
      assertEquals("L%", w2.getBoundaryTone());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCopyToBITonesByIndexBURNCTones() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();

    Region t1 = new Region(2.55, 2.55, "-X?");
    Region t2 = new Region(2.55, 2.55, "L%");

    tones.add(t1);
    tones.add(t2);

    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      assertEquals("X-?", w2.getPhraseAccent());

    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCopyToBITonesByIndexBURNCTonesTwo() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();

    Region t1 = new Region(2.55, 2.55, "X%?");

    tones.add(t1);

    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      assertEquals("X-?", w2.getPhraseAccent());
      assertEquals("X%?", w2.getBoundaryTone());

    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCopyToBITonesByIndexWhenAlignedByTimeCompoundPhraseEndingTone() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "H*");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(2.55, 2.55, "H-L%");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      assertEquals("H*", w1.getAccent());
      assertNull(w1.getPhraseAccent());
      assertNull(w1.getBoundaryTone());
      assertEquals("L+H*", w2.getAccent());
      assertEquals("H-", w2.getPhraseAccent());
      assertEquals("L%", w2.getBoundaryTone());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCopyToBITonesByIndexWhenAlignedByTimeLateBoundaryTone() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "H*");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(3.55, 3.55, "H-L%");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      assertEquals("H*", w1.getAccent());
      assertNull(w1.getPhraseAccent());
      assertNull(w1.getBoundaryTone());
      assertEquals("L+H*", w2.getAccent());
      assertEquals("H-", w2.getPhraseAccent());
      assertEquals("L%", w2.getBoundaryTone());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCopyToBITonesByIndexThrowsOnEmptyTone() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(3.55, 3.55, "H-L%");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testCopyToBITonesByIndexThrowsOnMissingPhraseAccent() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "H*");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(3.55, 3.55, "L%");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testCopyToBITonesByIndexThrowsOnMissingBoundaryTone() {

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1, 2, "test");
    w1.setBreakAfter("1");
    words.add(w1);

    Word w2 = new Word(2, 3, "test");
    words.add(w2);
    w2.setBreakAfter("4");

    List<Region> tones = new ArrayList<Region>();
    Region t1 = new Region(1.5, 1.5, "H*");
    Region t2 = new Region(2.5, 2.5, "L+H*");
    Region t3 = new Region(3.55, 3.55, "H-");

    tones.add(t1);
    tones.add(t2);
    tones.add(t3);


    try {
      AlignmentUtils.copyToBITonesByIndex(words, tones);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetNextRegion() {
    List<Region> regions = new ArrayList<Region>();
    Region r1 = new Region(1, 1, "one");
    Region r2 = new Region(2, 2, "two");
    regions.add(r1);
    regions.add(r2);

    Region rTest = AlignmentUtils.getNextRegionBeforeTime(1, regions.listIterator());
    assertEquals(r1, rTest);
  }

  @Test
  public void testGetNextRegionSecond() {
    List<Region> regions = new ArrayList<Region>();
    Region r1 = new Region(1, 1, "one");
    Region r2 = new Region(2, 2, "two");
    regions.add(r1);
    regions.add(r2);

    ListIterator<Region> li = regions.listIterator();

    Region rTest = AlignmentUtils.getNextRegionBeforeTime(0, li);
    assertNull(rTest);
  }
}
