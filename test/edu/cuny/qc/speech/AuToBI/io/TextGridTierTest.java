/*  TextGridTierTest.java

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

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


/**
 * Test class for TextGridTier
 */
public class TextGridTierTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  private TextGridTier t;

  @Before
  public void setUp() throws Exception {
    t = new TextGridTier();
  }

  @Test
  public void testAddPoint() {
    // This file contains a single point annotation
    try {
      AuToBIFileReader reader = new AuToBIFileReader(TEST_DIR + "/point.TextGrid");

      t.addPoint(reader);

      assertEquals(1, t.getRegions().size());
      assertEquals(4.443236, t.getRegions().get(0).getStart());
      assertEquals(4.443236, t.getRegions().get(0).getEnd());
      assertEquals("H*", t.getRegions().get(0).getLabel());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (TextGridSyntaxErrorException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testAddInterval() {
    // This file contains a single interval annotation
    try {
      AuToBIFileReader reader = new AuToBIFileReader(TEST_DIR + "/interval.TextGrid");

      t.addInterval(reader);

      assertEquals(1, t.getRegions().size());
      assertEquals(4.36744, t.getRegions().get(0).getStart());
      assertEquals(4.51463, t.getRegions().get(0).getEnd());
      assertEquals("My", t.getRegions().get(0).getLabel());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (TextGridSyntaxErrorException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadIntervalTier() {
    try {
      AuToBIFileReader reader = new AuToBIFileReader(TEST_DIR + "/interval_tier.TextGrid");

      t.readTier(reader);

      assertEquals(11, t.getRegions().size());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (TextGridSyntaxErrorException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testReadPointTier() {
    try {
      AuToBIFileReader reader = new AuToBIFileReader(TEST_DIR + "/point_tier.TextGrid");

      t.readTier(reader);

      assertEquals(10, t.getRegions().size());
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (TextGridSyntaxErrorException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
