/*  PitchFrame.java

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
import java.util.List;

/**
 * PitchFrame represents a number of pitch candidates for a single frame
 */
public class PitchFrame {

  private List<PitchCandidate> candidates;  // the candidates.
  private double intensity;                 // the intensity of the frame.

  /**
   * Constructs a new empty Pitch Frame
   */
  public PitchFrame() {
    candidates = new ArrayList<PitchCandidate>();
    intensity = 0.0;
  }

  /**
   * Sets the intensity of the frame.
   *
   * @param d the intensity
   */
  public void setIntensity(double d) {
    intensity = d;
  }

  /**
   * Retrieves the intensity of the frame.
   *
   * @return the intensity
   */
  public double getIntensity() {
    return intensity;
  }


  /**
   * Retrieves a single PitchCandidate.
   * <p/>
   * This can raise an IndexOutOfBounds Exception if there isn't a candidate for the requested index.
   *
   * @param i the index of the requested candidate
   * @return the desired candidate.
   */
  public PitchCandidate getCandidate(int i) {
    return candidates.get(i);
  }

  /**
   * Retrieves the number of candidates for this frame.
   *
   * @return the number of candidates.
   */
  public int getNumCandidates() {
    return candidates.size();
  }

  /**
   * Adds a new empty candidate to the frame.
   */
  public void addCandidate() {
    candidates.add(new PitchCandidate());
  }

  /**
   * Sets the value of a pitch candidate.
   *
   * @param i         the position of the candidate to alter.
   * @param candidate the new pitch candidate.
   */
  public void setCandidate(int i, PitchCandidate candidate) {
    candidates.set(i, candidate);
  }
}
