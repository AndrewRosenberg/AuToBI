/*  ToBIUtilsTest.java

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for ToBIUtils utility methods.
 */
public class ToBIUtilsTest {

  @Test
  public void testSetIntermediatePhraseBoundary() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter("3");

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter("4");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntermediatePhraseBoundary(words, "boundary");
    assertEquals("INTERMEDIATE_BOUNDARY", w1.getAttribute("boundary"));
    assertEquals("INTONATIONAL_BOUNDARY", w2.getAttribute("boundary"));
  }

  @Test
  public void testSetIntermediatePhraseBoundaryWorksWithNulls() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter(null);

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter(null);

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntermediatePhraseBoundary(words, "boundary");
    assertEquals("NONBOUNDARY", w1.getAttribute("boundary"));
    assertEquals("NONBOUNDARY", w2.getAttribute("boundary"));
  }

  @Test
  public void testSetIntermediatePhraseBoundaryWorksWithMinus() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter("3-");

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter("4-");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntermediatePhraseBoundary(words, "boundary");
    assertEquals("INTERMEDIATE_BOUNDARY", w1.getAttribute("boundary"));
    assertEquals("INTONATIONAL_BOUNDARY", w2.getAttribute("boundary"));
  }

  @Test
  public void testSetIntermediatePhraseBoundaryWorksWithThreeP() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter("3p");

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter("4-");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntermediatePhraseBoundary(words, "boundary");
    assertEquals("INTERMEDIATE_BOUNDARY", w1.getAttribute("boundary"));
    assertEquals("INTONATIONAL_BOUNDARY", w2.getAttribute("boundary"));
  }


  @Test
  public void testSetIntonationalPhraseBoundary() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter("3");

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter("4");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntonationalPhraseBoundary(words, "boundary");
    assertEquals("NONBOUNDARY", w1.getAttribute("boundary"));
    assertEquals("INTONATIONAL_BOUNDARY", w2.getAttribute("boundary"));
  }

  @Test
  public void testSetIntonationalPhraseBoundaryWorksWithNulls() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter(null);

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter(null);

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntonationalPhraseBoundary(words, "boundary");
    assertEquals("NONBOUNDARY", w1.getAttribute("boundary"));
    assertEquals("NONBOUNDARY", w2.getAttribute("boundary"));
  }

  @Test
  public void testSetIntonationalPhraseBoundaryWorksWithMinus() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBreakAfter("3-");

    Word w2 = new Word(0, 1, "two");
    w2.setBreakAfter("4-");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setIntonationalPhraseBoundary(words, "boundary");
    assertEquals("NONBOUNDARY", w1.getAttribute("boundary"));
    assertEquals("INTONATIONAL_BOUNDARY", w2.getAttribute("boundary"));
  }


  @Test
  public void testSetPhraseAccentBoundaryTone() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setPhraseAccent("L-");
    w1.setBoundaryTone("Hx");

    Word w2 = new Word(0, 1, "two");
    w2.setPhraseAccent("L-");
    w2.setBoundaryTone("Lx");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPhraseAccentBoundaryTone(words, "pabt");
    assertEquals("L-Hx", w1.getAttribute("pabt"));
    assertEquals("L-Lx", w2.getAttribute("pabt"));
  }

  @Test
  public void testSetPhraseAccentBoundaryToneWithNull() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setPhraseAccent("L-");
    w1.setBoundaryTone("Hx");

    Word w2 = new Word(0, 1, "two");
    w2.setPhraseAccent("L-");
    w2.setBoundaryTone(null);

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPhraseAccentBoundaryTone(words, "pabt");
    assertEquals("L-Hx", w1.getAttribute("pabt"));
    assertEquals("NOTONE", w2.getAttribute("pabt"));
  }

  @Test
  public void testSetPhraseAccent() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setPhraseAccent("L-");
    w1.setBoundaryTone("Hx");

    Word w2 = new Word(0, 1, "two");
    w2.setPhraseAccent("H-");
    w2.setBoundaryTone("Lx");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPhraseAccent(words, "phrase");
    assertEquals("L-", w1.getAttribute("phrase"));
    assertEquals("H-", w2.getAttribute("phrase"));
  }

  @Test
  public void testSetPhraseAccentWorksWithNull() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setPhraseAccent("L-");
    w1.setBoundaryTone("Hx");

    Word w2 = new Word(0, 1, "two");
    w2.setPhraseAccent(null);
    w2.setBoundaryTone("Lx");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPhraseAccent(words, "phrase");
    assertEquals("L-", w1.getAttribute("phrase"));
    assertEquals("NOTONE", w2.getAttribute("phrase"));
  }

  @Test
  public void testSetPitchAccentType() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAccent("L*");

    Word w2 = new Word(0, 1, "two");
    w2.setAccent("L+H*");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPitchAccentType(words, "accent");
    assertEquals("L*", w1.getAttribute("accent"));
    assertEquals("L+H*", w2.getAttribute("accent"));
  }

  @Test
  public void testSetPitchAccentTypeWorksWithNull() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAccent("L*");

    Word w2 = new Word(0, 1, "two");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPitchAccentType(words, "accent");
    assertEquals("L*", w1.getAttribute("accent"));
    assertEquals("NOACCENT", w2.getAttribute("accent"));
  }

  @Test
  public void testSetPitchAccent() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAccent("L*");

    Word w2 = new Word(0, 1, "two");
    w2.setAccent("L+H*");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPitchAccent(words, "accent");
    assertEquals("ACCENTED", w1.getAttribute("accent"));
    assertEquals("ACCENTED", w2.getAttribute("accent"));
  }

  @Test
  public void testSetPitchAccentWithNull() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAccent("L*");

    Word w2 = new Word(0, 1, "two");


    words.add(w1);
    words.add(w2);

    ToBIUtils.setPitchAccent(words, "accent");
    assertEquals("ACCENTED", w1.getAttribute("accent"));
    assertEquals("DEACCENTED", w2.getAttribute("accent"));
  }

  @Test
  public void testSetPitchAccentWithEmpty() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAccent("L*");

    Word w2 = new Word(0, 1, "two");
    w2.setAccent("");

    words.add(w1);
    words.add(w2);

    ToBIUtils.setPitchAccent(words, "accent");
    assertEquals("ACCENTED", w1.getAttribute("accent"));
    assertEquals("DEACCENTED", w2.getAttribute("accent"));
  }

  @Test
  public void testGenerateBreaksFromTones() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setBoundaryTone("L%");

    Word w2 = new Word(0, 1, "two");
    w2.setPhraseAccent("H-");

    Word w3 = new Word(0, 1, "three");


    words.add(w1);
    words.add(w2);
    words.add(w3);

    ToBIUtils.generateBreaksFromTones(words);
    assertEquals("-1", w1.getBreakBefore());
    assertEquals("4", w1.getBreakAfter());

    assertEquals("4", w2.getBreakBefore());
    assertEquals("3", w2.getBreakAfter());

    assertEquals("3", w3.getBreakBefore());
    assertEquals("1", w3.getBreakAfter());
  }


  @Test
  public void testCheckToBIAnnotationThrowsAnErrorBoundaryToneWithoutBreakIndex() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);

    w1.setBoundaryTone("H%");
    w1.setBreakAfter("1");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testCheckToBIAnnotationThrowsAnErrorPhraseAccentWithoutBreakIndex() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);

    w1.setPhraseAccent("H-");
    w1.setBreakAfter("1");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }


  @Test
  public void testCheckToBIAnnotationMultiplePhrases() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);
    words.add(w2);

    w1.setPhraseAccent("H-");
    w1.setBreakAfter("4");

    w2.setAccent("H*");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      // Expected.
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCheckToBIAnnotationWithGoodPhrases() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);
    words.add(w2);

    w1.setPhraseAccent("H-");
    w1.setAccent("L*");
    w1.setBreakAfter("4");

    w2.setAccent("H*");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      // Expected.
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCheckToBIAnnotationWithGoodIntermediatePhrase() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);
    words.add(w2);

    w1.setPhraseAccent("H-");
    w1.setAccent("L*");
    w1.setBreakAfter("3");

    w2.setAccent("H*");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      // Expected.
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testCheckToBIAnnotationWithBadIntermediatePhrase() {
    List<Word> words = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "two");
    Word w3 = new Word(0, 1, "three");

    words.add(w1);
    words.add(w2);

    w1.setAccent("L*");
    w1.setBreakAfter("3");

    w2.setAccent("H*");

    try {
      ToBIUtils.checkToBIAnnotations(words);
      // Expected.
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetPitchAccentWithBadUnknownAccents() {
    Word w = new Word(0, 1, "one");

    String label = "*?";
    assertEquals("X*?", ToBIUtils.getPitchAccent(label));
  }

  @Test
  public void testGetPitchAccentWithBadUnknownAccentsTwo() {
    Word w = new Word(0, 1, "one");

    String label = "X*";
    assertEquals("X*?", ToBIUtils.getPitchAccent(label));
  }

  @Test
  public void testGetPitchAccentWithBadUnknownAccentsThree() {
    Word w = new Word(0, 1, "one");

    String label = "*";
    assertEquals("X*?", ToBIUtils.getPitchAccent(label));
  }

  @Test
  public void testParseToneStringTreatsPlusesAsAccents() {
    String label = "L+";

    String[] tones = ToBIUtils.parseToneString(label);
    assertEquals("L+", tones[0]);
    assertNull(tones[1]);
    assertNull(tones[2]);
  }

  @Test
  public void testParseToneStringTreatsLeadingPlusesAsAccents() {
    String label = "+L*";

    String[] tones = ToBIUtils.parseToneString(label);
    assertEquals("+L*", tones[0]);
    assertNull(tones[1]);
    assertNull(tones[2]);
  }
}
