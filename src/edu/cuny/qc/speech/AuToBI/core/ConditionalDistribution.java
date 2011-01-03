/*  ConditionalDistribution.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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

import java.util.HashMap;

/**
 * A ConditionalDistribution is a structure to associate strings and distributions.
 * <p/>
 * This class can be used to calculate conditional probability distributions, or simply count tokens conditioned on a
 * string.
 *
 * @see Distribution
 */
public class ConditionalDistribution extends HashMap<String, Distribution> {


  /**
   * Adds a key value pair to the conditional distribution.
   *
   * @param key   the key
   * @param value the value
   */
  public void add(String key, String value) {
    add(key, value, 1);
  }

  /**
   * Adds a key value pair to the conditional distribution with a specified weight.
   *
   * @param key    the key
   * @param value  the value
   * @param weight the weight
   */
  public void add(String key, String value, double weight) {
    if (!containsKey(key))
      put(key, new Distribution());

    get(key).add(value, weight);
  }

  /**
   * Normalizes the conditional distributions.
   */
  public void normalize() {
    for (String key : keySet()) {
      get(key).normalize();
    }
  }
}
