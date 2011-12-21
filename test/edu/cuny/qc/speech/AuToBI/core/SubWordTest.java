/*  SubWordTest.java

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Test class for SubWord.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.SubWord
 */
public class SubWordTest {
  @Test
  public void testConstructor() {
    SubWord sw = new SubWord(0.0, 1.0, "test", "accent*", "/test/file.txt");

    assertEquals(0.0, sw.getStart());
    assertEquals(1.0, sw.getEnd());
    assertEquals("test", sw.getLabel());
    assertEquals("accent*", sw.getAccent());
    assertEquals("/test/file.txt", sw.getFile());
  }

  @Test
  public void testConstructorNoFile() {
    SubWord sw = new SubWord(0.0, 1.0, "test", "accent*");

    assertEquals(0.0, sw.getStart());
    assertEquals(1.0, sw.getEnd());
    assertEquals("test", sw.getLabel());
    assertEquals("accent*", sw.getAccent());
    assertNull(sw.getFile());
  }

  @Test
  public void testConstructorNoAccent() {
    SubWord sw = new SubWord(0.0, 1.0, "test");

    assertEquals(0.0, sw.getStart());
    assertEquals(1.0, sw.getEnd());
    assertEquals("test", sw.getLabel());
    assertNull(sw.getAccent());
    assertNull(sw.getFile());
  }

  @Test
  public void testSetAndGetWord() {
    SubWord sw = new SubWord(0.0, 1.0, "test");
    Word w = new Word(-1.0, 5.0, "test_word");
    sw.setWord(w);
    assertEquals(w, sw.getWord());
  }

  @Test
  public void testToString() {
    SubWord sw = new SubWord(0.0, 1.0, "test");
    Word w = new Word(-1.0, 5.0, "test_word");
    sw.setWord(w);
    assertEquals("test [0.0, 1.0] (null)[null, null] within test_word [-1.0, 5.0] (null)[null, null]", sw.toString());
  }
}
