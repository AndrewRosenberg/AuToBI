/*  NXTTierTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.io.NXTTier
 */
public class NXTTierTest {

  @Test
  public void testParseBreakParsesTime() {
    String line =
        "<break nite:id=\"sw4890A.break.26\" nite:start=\"9.334875\" nite:end=\"9.334875\" UWtime=\"9.344875\" " +
            "index=\"4-\" phraseTone=\"L\" boundaryTone=\"H\">";

    NXTTier tier = new NXTTier();
    List<Region> marks = tier.parseBreak(line);

    assertEquals(9.344875, marks.get(0).getStart(), 0.0001);
    assertEquals(9.344875, marks.get(0).getEnd(), 0.0001);
  }

  @Test
  public void testParseBreakParsesBreakIndex() {
    String line =
        "<break nite:id=\"sw4890A.break.26\" nite:start=\"9.334875\" nite:end=\"9.334875\" UWtime=\"9.344875\" " +
            "index=\"4-\" phraseTone=\"L\" boundaryTone=\"H\">";

    NXTTier tier = new NXTTier();
    List<Region> marks = tier.parseBreak(line);
    assertEquals("4-", marks.get(0).getLabel());
  }

  @Test
  public void testParseBreakParsesBoundaryTone() {
    String line =
        "<break nite:id=\"sw4890A.break.26\" nite:start=\"9.334875\" nite:end=\"9.334875\" UWtime=\"9.344875\" " +
            "index=\"4-\" phraseTone=\"L\" boundaryTone=\"H\">";

    NXTTier tier = new NXTTier();
    List<Region> marks = tier.parseBreak(line);
    assertEquals("H%", marks.get(2).getLabel());
  }

  @Test
  public void testParseBreakParsesPhraseAccent() {
    String line =
        "<break nite:id=\"sw4890A.break.26\" nite:start=\"9.334875\" nite:end=\"9.334875\" UWtime=\"9.344875\" " +
            "index=\"4-\" phraseTone=\"L\" boundaryTone=\"H\">";

    NXTTier tier = new NXTTier();
    List<Region> marks = tier.parseBreak(line);
    assertEquals("L-", marks.get(1).getLabel());
  }

  @Test
  public void testParseWordWorks() {
    String line =
        "<word pos=\"NN\" nite:id=\"s1_38\" msstateID=\"sw4890A-ms98-a-0002-28\" msstate=\"sw4890A-ms98-a-0002\" " +
            "nite:end=\"6.054875\" nite:start=\"5.805750\" orth=\"jury\">";

    NXTTier tier = new NXTTier();
    Region r = tier.parseWord(line);

    assertEquals(6.054875, r.getEnd(), 0.0001);
    assertEquals(5.805750, r.getStart(), 0.0001);
    assertEquals("jury", r.getLabel());
  }

  @Test
  public void testParseAccentWorks() {
    String line =
        "<accent nite:id=\"sw4890.A.acc247.aw826\" nite:start=\"297.835282\" nite:end=\"297.835282\" " +
            "strengths=\"full\">";

    NXTTier tier = new NXTTier();
    Region r = tier.parseAccent(line);

    assertEquals(297.835282, r.getEnd(), 0.0001);
    assertEquals(297.835282, r.getStart(), 0.0001);
    assertEquals("X*?", r.getLabel());
  }

}
