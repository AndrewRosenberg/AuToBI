/* GMMComponent.java

  Copyright 2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.core.syllabifier;

/**
 * A simple class to hold mean and variance information for Gaussian Mixture Model components.
 */
class GMMComponent {
  double mean = 0;
  double variance = 0;
  boolean isSilence;
  double weight;  // mixture coefficient
  double n; // total (weighted) responsibility assigned to this component

  public GMMComponent(double mean, double variance) {
    this.mean = mean;
    this.variance = variance;
    this.isSilence = false;
    this.weight = 1.;
    this.n = 0.;
  }

  public GMMComponent(double mean, double variance, double weight) {
    this.mean = mean;
    this.variance = variance;
    this.isSilence = false;
    this.weight = weight;
    this.n = 0.;
  }

  /**
   * Calculates the gaussian likelihood of the value
   *
   * @param value the value
   * @return the gaussian likelihood of the value.
   */
  public Double calcLikelihood(Double value) {
    double stdev = Math.sqrt(variance);
    double pdf = 1 / (stdev * Math.sqrt(2 * Math.PI));
    pdf *= Math.pow(Math.E, (-(value - mean) * (value - mean)) / (2 * stdev * stdev));
    return pdf;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}