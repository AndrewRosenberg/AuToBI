/*  WordTest.java

    Copyright (c) 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.core;

import org.junit.Test;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertNotSame;

/**
 * Test class for Word.
 *
 * @see Word
 */
public class WordTest {
  @Test
  public void testConstructorInitializesVariablesProperly() {
    Word w = new Word(0.0, 0.1, "test_label", "test_accent", "/test/file/name.txt");

    assertEquals(0.0, w.getStart(), 0.0001);
    assertEquals(0.1, w.getEnd(), 0.0001);
    assertEquals("test_label", w.getLabel());
    assertEquals("test_accent", w.getAccent());
    assertEquals("/test/file/name.txt", w.getFile());
    assertEquals(-1.0, w.getAccentTime(), 0.0001);
  }

  @Test
  public void testNoFileConstructorInitializesVariablesProperly() {
    Word w = new Word(0.0, 0.1, "test_label", "test_accent");

    assertEquals(0.0, w.getStart(), 0.0001);
    assertEquals(0.1, w.getEnd(), 0.0001);
    assertEquals("test_label", w.getLabel());
    assertEquals("test_accent", w.getAccent());
    assertNull(w.getFile());
    assertEquals(-1.0, w.getAccentTime(), 0.0001);
  }

  @Test
  public void testNoAccentConstructorInitializesVariablesProperly() {
    Word w = new Word(0.0, 0.1, "test_label");

    assertEquals(0.0, w.getStart(), 0.0001);
    assertEquals(0.1, w.getEnd(), 0.0001);
    assertEquals("test_label", w.getLabel());
    assertNull(w.getAccent());
    assertNull(w.getFile());
    assertEquals(-1.0, w.getAccentTime(), 0.0001);
  }

  @Test
  public void testCopyConstructorInitializesVariablesProperly() {
    Word old_w = new Word(0.0, 0.1, "test_label", "test_accent", "/test/file/name.txt");
    Word w = new Word(old_w);
    assertNotSame(old_w, w);
    assertEquals(0.0, w.getStart(), 0.0001);
    assertEquals(0.1, w.getEnd(), 0.0001);
    assertEquals("test_label", w.getLabel());
    assertEquals("test_accent", w.getAccent());
    assertEquals("/test/file/name.txt", w.getFile());
    assertEquals(-1.0, w.getAccentTime(), 0.0001);
  }

  @Test
  public void testSetAndGetAccent() {
    Word w = new Word(0.0, 0.1, "test_label");

    w.setAccent("test*");
    assertEquals("test*", w.getAccent());
  }

  @Test
  public void testSetAndGetAccentWithWhiteSpace() {
    Word w = new Word(0.0, 0.1, "test_label");

    w.setAccent("test*    ");
    assertEquals("test*", w.getAccent());
  }

  @Test
  public void testSetAndIsAccented() {
    Word w = new Word(0.0, 0.1, "test_label");

    assertFalse(w.isAccented());
    w.setAccent("test*    ");
    assertTrue(w.isAccented());
  }

  @Test
  public void testSetAndGetBreakBefore() {
    Word w = new Word(0.0, 0.1, "test_label");

    assertNull(w.getBreakBefore());
    w.setBreakBefore("6");
    assertEquals("6", w.getBreakBefore());
  }

  @Test
  public void testSetAndGetBreakAfter() {
    Word w = new Word(0.0, 0.1, "test_label");

    assertNull(w.getBreakAfter());
    w.setBreakAfter("6");
    assertEquals("6", w.getBreakAfter());
  }

  @Test
  public void testIsIntonationalPhraseFinalInitializedFalse() {
    Word w = new Word(0.0, 0.1, "test_label");

    assertFalse(w.isIntonationalPhraseFinal());
  }

  @Test
  public void testIsIntonationalPhraseFinalWithFourMinus() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("4-");
    assertTrue(w.isIntonationalPhraseFinal());
  }

  @Test
  public void testIsIntonationalPhraseFinalWithFour() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("4");
    assertTrue(w.isIntonationalPhraseFinal());
  }

  @Test
  public void testIsIntonationalPhraseFinalWithThreeFalse() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("3");
    assertFalse(w.isIntonationalPhraseFinal());
  }

  @Test
  public void testIsIntermediatePhraseFinalWithThree() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("3");
    assertTrue(w.isIntermediatePhraseFinal());
  }

  @Test
  public void testIsIntermediatePhraseFinal() {
    Word w = new Word(0.0, 0.1, "test_label");
    assertFalse(w.isIntermediatePhraseFinal());
  }

  @Test
  public void testIsIntermediatePhraseFinalWithTwoFalse() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("2");
    assertFalse(w.isIntermediatePhraseFinal());
  }

  @Test
  public void testIsIntermediatePhraseFinalWithFour() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBreakAfter("4");
    assertTrue(w.isIntermediatePhraseFinal());
  }

  @Test
  public void testSetAndGetPhraseAccent() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setPhraseAccent("L-");
    assertEquals("L-", w.getPhraseAccent());
  }

  @Test
  public void testSetAndGetBoundaryTone() {
    Word w = new Word(0.0, 0.1, "test_label");
    w.setBoundaryTone("L%");
    assertEquals("L%", w.getBoundaryTone());
  }

  @Test
  public void testHasPhraseAccent() {
    Word w = new Word(0.0, 0.1, "test_label");
    assertFalse(w.hasPhraseAccent());
    w.setPhraseAccent("L-");
    assertTrue(w.hasPhraseAccent());
  }

  @Test
  public void testHasBoundaryTone() {
    Word w = new Word(0.0, 0.1, "test_label");
    assertFalse(w.hasBoundaryTone());
    w.setBoundaryTone("L%");
    assertTrue(w.hasBoundaryTone());
  }
}
