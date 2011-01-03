/*  PitchCandidate.java

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
 * A single hypothesized PitchCandidate stores the frequency and the strength of a hypothesis.  The strength can be
 * understood as the confidence of the pitch hypothesis.
 */
public class PitchCandidate {
  private double frequency;  // the frequency of the candidate
  private double strength;   // the strength of the candidate

  /**
   * Retrieves the frequency.
   *
   * @return the frequency
   */
  public double getFrequency() {
    return frequency;
  }

  /**
   * Sets the frequency of the candidate.
   *
   * @param frequency the frequency
   */
  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }

  /**
   * Retrieves the strength of the candidate.
   *
   * @return the strength
   */
  public double getStrength() {
    return strength;
  }

  /**
   * Sets the strength of the candidate.
   *
   * @param strength the strength
   */
  public void setStrength(double strength) {
    this.strength = strength;
	}
}
