/*  ContourIterateor.java

    Copyright 2009-2011 Andrew Rosenberg

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

import java.util.Iterator;

/**
 * An Iterator class to traverse Contour objects.  This is used to retrieve time value pairs for a given contour.
 *
 * @see Contour
 */
public class ContourIterator implements Iterator<Pair<Double, Double>> {
  private int i;      // the current index
  private Contour c;  // the associated contour

  /**
   * Constructs a new ContourIterator for a given Contour.
   *
   * @param contour the contour
   */
  public ContourIterator(Contour contour) {
    i = 0;
    c = contour;
  }

  /**
   * Determines if the contour has another item.
   *
   * @return true if there are more entries in the contour, false otherwise
   */
  public boolean hasNext() {
    while (i < c.size() && c.isEmpty(i)) {
      ++i;
    }
    return i < c.size();
  }

  /**
   * Retrieves the next entry in the contour.
   *
   * @return a pair containing time and value information for the next contour entry
   */
  public Pair<Double, Double> next() {
    while (c.isEmpty(i)) {
      ++i;
    }
    Pair<Double, Double> next_pair = c.getPair(i);
    ++i;
    return next_pair;
  }


  /**
   * Removes the current entry in the contour, by setting it to empty.
   */
  public void remove() {
    c.setEmpty(i);
  }
}
