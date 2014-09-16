/*  Aggregation.java

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

import java.util.Collection;
import java.io.Serializable;

import static org.apache.commons.math3.special.Erf.erf;

/**
 * A class to store aggregations of real numbered information.
 * <p/>
 * Aggregation is used to store and serialize running means and standard deviations of values such as pitch and
 * intensity.
 */
public class Aggregation implements Serializable {
  private static final long serialVersionUID = 2012709453361591892L;

  private String label; // an optional label for the aggregation
  private Double min;   // the maximum value in the aggregation
  private Double max;   // the minimum value in the aggregation
  private Double sum;   // the sum of all values added to the aggregation
  private Double ssq;   // the sum of squares of all values added to the aggregation
  private Integer n;    // the number of elements in the aggregation

  /**
   * Constructs a new Aggregation
   */
  public Aggregation() {
    this.label = "";
    this.min = Double.MAX_VALUE;
    this.max = -Double.MAX_VALUE;
    this.sum = 0.0;
    this.ssq = 0.0;
    this.n = 0;
  }

  /**
   * Constructs a new Aggregation with a label
   *
   * @param label the label
   */
  public Aggregation(String label) {
    this();
    this.label = label;
  }


  /**
   * Sets the label.
   *
   * @param label The new label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets the label
   *
   * @return label
   */
  public String getLabel() {
    return label;
  }


  /**
   * Inserts a new value in the Aggregation
   *
   * @param v the value
   */
  public void insert(Double v) {
    sum += v;
    ssq += (v * v);
    max = (max == null) ? v : Math.max(max, v);
    min = (min == null) ? v : Math.min(min, v);
    n++;
  }

  /**
   * Removes a value from the Aggregation.
   * <p/>
   * Removing a value from an Aggregation can invalidate its minimum and maximum calculation if the value was equal to
   * the current minmum or maximum.
   * <p/>
   * Note: no check is made that the value was ever initially added to the Aggregation.
   *
   * @param v the value
   */
  public void remove(Double v) {
    sum -= v;
    ssq -= (v * v);
    if (v.equals(max)) {
      max = Double.NaN;  // no running max and min
    }
    if (v.equals(min)) {
      min = Double.NaN;
    }
    n--;
  }

  /**
   * Inserts a Collection of values.
   *
   * @param values the values
   */
  public void insert(Collection<Double> values) {
    for (Double d : values)
      this.insert(d);
  }

  /**
   * Removes a Collection of values.
   *
   * @param values the values
   */
  public void remove(Collection<Double> values) {
    for (Double d : values)
      this.remove(d);
  }

  /**
   * Calculates the mean value.
   * <p/>
   * If there are no elements in the Aggregation, the mean is zero.
   *
   * @return the mean
   */
  public Double getMean() {
    if (n < 1) return 0.0;
    return sum / n;
  }

  /**
   * Calculates the standard deviation.
   * <p/>
   * If there are less than 2 elements in the Aggregation, the standard deviation is zero.
   *
   * @return the standard deviation
   */
  public Double getStdev() {
    if (n < 2) return 0.0;

    return Math.sqrt(getVariance());
  }

  /**
   * Calculates the variance
   * <p/>
   * If there are less than 2 elements in the Aggregation, the variance is zero.
   *
   * @return the variance
   */
  public Double getVariance() {
    if (n < 2) return 0.0;

    Double mean = sum / n;
    return (ssq - (n * mean * mean)) / (n - 1);
  }

  /**
   * Calculates the root mean squared value.
   *
   * @return the rms
   */
  public Double getRMS() {
    return Math.sqrt(ssq / n);
  }

  /**
   * Retrieves the minimum value.
   *
   * @return the minimum
   */
  public Double getMin() {
    return min;
  }

  /**
   * Overrides the current min with an externally calculated value
   * <p/>
   * This is helpful if the minimum value gets invalidated by remove.
   *
   * @param min the minimum value
   */
  public void setMin(Double min) {
    this.min = min;
  }

  /**
   * Retrieves the maximum value.
   *
   * @return the maximum
   */
  public Double getMax() {
    return max;
  }

  /**
   * Overrides the current max with an externally calculated value
   * <p/>
   * This is helpful if the maximum value gets invalidated by remove.
   *
   * @param max the maximum value
   */
  public void setMax(Double max) {
    this.max = max;
  }

  /**
   * Retrieves the number of elements.
   *
   * @return the number of elements.
   */
  public int getSize() {
    return n;
  }

  /**
   * Treating this aggregation as a Gaussian probability distribution function, evaluates the probability that a value
   * generated was generated by this aggregation.
   *
   * @param value the value to evaluate
   * @return the gaussian PDF evaluated at value
   */
  public double evaluateGaussianPDF(double value) {
    double mean = getMean();
    double stdev = getStdev();

    double pdf = 1 / (stdev * Math.sqrt(2 * Math.PI));
    pdf *= Math.pow(Math.E, (-(value - mean) * (value - mean)) / (2 * stdev * stdev));
    return pdf;
  }

  /**
   * Evaluates the CDF of the aggregation.  The aggregation is treated as a Gaussian distribution in this case
   * <p/>
   * The CDF is the probability of a value drawn from the distribution falling below f
   * <p/>
   * cdf(x) = 1/2 [1 + erf(x - mu/sqrt(2 * stdev^2))]
   *
   * @param value the value
   * @return the CDF
   */
  public double evaluateGaussianCDF(double value) {
    double stdev = getStdev();
    return .5 * (1 + erf((value - getMean()) / Math.sqrt(2 * stdev * stdev)));
  }

  /**
   * Generates a descriprion of the Aggregation
   *
   * @return the description
   */
  public String toString() {
    String s = "";
    s += "mean: " + getMean();
    s += " stdev: " + getStdev();
    s += " variance: " + getVariance();
    s += " stderr: " + (getStdev() / getSize());
    s += " min: " + getMin();
    s += " max: " + getMax();
    return s;
  }
}
