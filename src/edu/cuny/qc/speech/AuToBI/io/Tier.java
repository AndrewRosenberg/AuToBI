/*  Tier.java

    Copyright 2009-2014 Andrew Rosenberg
    
    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;
import java.util.LinkedList;

/**
 * Tier is an abstract class for storing groups of regions.
 * <p/>
 * The ToBI standard is typically structured in terms of time aligned tiers.  This class can be extended to
 * accomodate differing input Tier formats.
 */
public abstract class Tier {
  protected String name = null;     // the name of the tier
  protected List<Region> regions;   // the regions contained in the tier
  protected boolean is_point_tier;  // whether the regions are intervals or points

  /**
   * Constructs a new Tier.
   * <p/>
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
