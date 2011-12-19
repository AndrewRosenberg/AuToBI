/*  Pair.java

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

/**
 * Pair<T1,T2> is a lightweight generic to represent two objects as a single Pair.
 *
 * @param <T1> the first object
 * @param <T2> the second object
 */
public class Pair<T1, T2> {
  public T1 first;   // the first object
  public T2 second;  // the second object

  /**
   * Constructs a new pair with values, v1 and v2.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  public Pair(T1 v1, T2 v2) {
    first = v1;
    second = v2;
  }

  /**
   * Constructs a new empty pair.
   */
  public Pair() {
    first = null;
    second = null;
  }
}
