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
 * A single hypothesized PitchCandidate stores the frequency and the strengths of a hypothesis.  The strengths can be
 * understood as the confidence of the pitch hypothesis.
 */
public class PitchCandidate {
  public double frequency;  // the frequency of the candidate
  public double strength;   // the strengths of the candidate

  public PitchCandidate() {
    frequency = 0.0;
    strength = 0.0;
  }

  public PitchCandidate(double frequency, double strength) {
    this.frequency = frequency;
    this.strength = strength;
  }
}
