/*  PitchCandidate.java

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
