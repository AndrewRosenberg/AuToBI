/*  PitchFrame.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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
