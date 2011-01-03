/*  Tier.java

    Copyright 2009-2010 Andrew Rosenberg
    
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

import java.util.List;
import java.util.LinkedList;

/**
 * Tier is an abstract class for storing groups of regions.
 *
 * The ToBI standard is typically structured in terms of time aligned tiers.  This class can be extended to
 * accomodate differing input Tier formats.
 */
public abstract class Tier {
  protected String name = null;     // the name of the tier
  protected List<Region> regions;   // the regions contained in the tier
  protected boolean is_point_tier;  // whether the regions are intervals or points

  /**
   * Constructs a new Tier.
   *
   * By default Tiers contain interval regions
   */
  public Tier() {
    is_point_tier = false;
    regions = new LinkedList<Region>();
  }

  /**
   * Retrieves the regions in the Tier.
   *
   * @return the regions
   */
  public List<Region> getRegions() {
    return regions;
  }

  /**
   * Determines if the Tier contains points or intervals.
   *
   * @return true if the Tier is a point tier, false otherwise
   */
  public boolean isAPointTier() {
    return is_point_tier;
  }

  /**
   * Retrieves the name of the tier.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }
}
