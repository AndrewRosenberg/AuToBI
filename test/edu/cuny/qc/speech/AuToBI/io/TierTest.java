/*  TierTest.java

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

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/**
 * Test class for Tier
 */
public class TierTest {

  @Test
  public void testGetters() {
    Tier t = new Tier() {
      @Override
      public List<Region> getRegions() {
        return new ArrayList<Region>();
      }
    };

    t.name = "test";
    t.is_point_tier = true;

    assertEquals("test", t.getName());
    assertEquals(true, t.isAPointTier());
  }

  @Test
  public void testTierDefaultsToIntervalTier() {
    Tier t = new Tier() {
      @Override
      public List<Region> getRegions() {
        return new ArrayList<Region>();
      }
    };

    assertEquals(false, t.isAPointTier());
  }

  @Test
  public void testGetRegions() {
    Tier t = new Tier() {
      @Override
      public List<Region> getRegions() {
        return new ArrayList<Region>();
      }
    };

    assertNotNull(t.getRegions());
  }
}
