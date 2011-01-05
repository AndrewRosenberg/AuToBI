/*  TimeValuePair.java

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

package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.core.Pair;

import java.io.Serializable;

/**
 * TimeValuePairs associate values and times, and are typically used to represent acoustic
 * contours in AuToBI.
 *
 * @Deprecated Use Contour instead.
 *
 */
@Deprecated
public class TimeValuePair extends Pair<Double, Double> implements Serializable {
  private static final long serialVersionUID = 6410344724558496459L;

  /**
   * Constructs a new TimeValuePair.
   *
   * @param t the time
   * @param v the value
   */
  public TimeValuePair(double t, double v) {
    first = t;
    second = v;
  }

  /**
   * Retrieves the time.
   *
   * @return the time
   */
  public Double getTime() {
    return first;
  }

  /**
   * Sets the time.
   *
   * @param time the new time
   */
  public void setTime(double time) {
    this.first = time;
  }

  /**
   * Retrieves the value.
   *
   * @return the value
   */
  public Double getValue() {
    return second;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(double value) {
    this.second = value;
  }
}
