/*  NegativeSymmetricList.java

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

import java.util.ArrayList;

/**
 * NegativeSymmetricList is a wrapper around an ArrayList<Double> that is symmetric for positive and negative indices.
 * <p/>
 * This allows for retrieval of negative indices, since they are the same as the corresponding positive index.
 * <p/>
 * This could also be accomplished using ArrayList.get(Math.abs(i)).
 */
public class NegativeSymmetricList extends ArrayList<Double> {
  /**
   * Retrieves a value from the list.
   *
   * @param i the index of the value
   * @return the associated value
   */
  public Double get(int i) {
    if (i >= 0) {
      return super.get(i);
    } else {
      return super.get(-i);
    }
  }

  /**
   * Sets the value of an index in the list.
   * <p/>
   * Since the list is symmetric, the value for the negative index is also set.
   *
   * @param i     the index
   * @param value the new value
   * @return the element previously at position i
   */
  public Double set(int i, Double value) {
    if (i >= 0) {
      return super.set(i, value);
    } else {
      return super.set(-i, value);
    }
  }
}
