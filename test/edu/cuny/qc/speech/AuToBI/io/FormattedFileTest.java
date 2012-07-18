/*  FormattedFileTest.java

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

package edu.cuny.qc.speech.AuToBI.io;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for FormattedFile
 *
 * @see FormattedFile
 */
@SuppressWarnings("unchecked")
public class FormattedFileTest {

  @Test
  public void testConstructsTextGridFile() {
    FormattedFile ff = new FormattedFile("blah.TextGrid");
    assertEquals(FormattedFile.Format.TEXTGRID, ff.getFormat());
  }

  @Test
  public void testConstructsBURNCFile() {
    FormattedFile ff = new FormattedFile("blah.ala");
    assertEquals(FormattedFile.Format.BURNC, ff.getFormat());
  }

  @Test
  public void testConstructsBuckeyeFile() {
    FormattedFile ff = new FormattedFile("s1234b.words");
    assertEquals(FormattedFile.Format.BUCKEYE, ff.getFormat());
  }

  @Test
  public void testConstructsSimpleWordFile() {
    FormattedFile ff = new FormattedFile("file.words");
    assertEquals(FormattedFile.Format.SIMPLE_WORD, ff.getFormat());
  }

  @Test
  public void testConstructsSwitchboardFile() {
    FormattedFile ff = new FormattedFile("file.terminals.xml");
    assertEquals(FormattedFile.Format.SWB_NXT, ff.getFormat());
  }

  @Test
  public void testConstructorAndGetter() {
    FormattedFile ff = new FormattedFile("test/file.name", FormattedFile.Format.SWB_NXT);

    assertEquals("test/file.name", ff.getFilename());
    assertEquals(FormattedFile.Format.SWB_NXT, ff.getFormat());
  }
}
