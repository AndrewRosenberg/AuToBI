/*  SpeakerNormalizationParameter.java

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

import java.io.Serializable;
import java.util.HashMap;

/**
 * A set of parameters used for speaker normalization
 * <p/>
 * Currently is used for z-score normalization of pitch, log pitch,  intensity and log_intensity
 */
public class SpeakerNormalizationParameter implements Serializable {
  private static final long serialVersionUID = 20100509L;
  // an association from the normalization attribute to the aggregation used for the normalization
  private HashMap<String, Aggregation> params;
  private String speaker_id;  // a speaker identifier for the parameters


  /**
   * Constructs an empty SpeakerNormalizationParameter.
   */
  public SpeakerNormalizationParameter() {
    this("");
  }

  /**
   * Constructs an empty SpeakerNormalizationParameter with a speaker identifier.
   *
   * @param speaker_id the speaker identifier.
   */
  public SpeakerNormalizationParameter(String speaker_id) {
    this.speaker_id = speaker_id;
    params = new HashMap<String, Aggregation>();
    params.put("f0", new Aggregation());
    params.put("log[f0]", new Aggregation());
    params.put("I", new Aggregation());
    params.put("log[I]", new Aggregation());
  }

  /**
   * Retrieves the speaker id value
   *
   * @return the speaker identifier
   */
  public String getSpeakerId() {
    return speaker_id;
  }

  /**
   * Inserts a pitch value to the parameters.
   *
   * @param pitch the pitch value
   */
  public void insertPitch(double pitch) {
    params.get("f0").insert(pitch);
    params.get("log[f0]").insert(Math.log(pitch));
  }

  /**
   * Inserts an intensity value.
   *
   * @param intensity the intensity value.
   */
  public void insertIntensity(double intensity) {
    params.get("I").insert(intensity);
    params.get("log[I]").insert(Math.log(intensity));
  }

  /**
   * Normalizes a value.
   * <p/>
   * The user must specify what type of value is supplied so the appropriate normalization parameters can be used.
   *
   * @param feature the type of feature
   * @param value   the value of the feature
   * @return the zscore normalized value
   */
  public double normalize(String feature, double value) {
    return (value - params.get(feature).getMean()) / params.get(feature).getStdev();
  }

  /**
   * Range normalizes a value
   *
   * @param feature the type of feature
   * @param v       the value of the feature
   * @return the range normalized value
   */
  public double rangeNormalize(String feature, double v) {
    return (v - params.get(feature).getMin()) / (params.get(feature).getMax() - params.get(feature).getMin());
  }

  /**
   * Inserts a list of pitch values into the normalization parameters.
   *
   * @param pitch_values the pitch values to insert
   */
  public void insertPitch(Contour pitch_values) {
    for (Pair<Double, Double> pitch : pitch_values) {
      insertPitch(pitch.second);
    }
  }

  /**
   * Inserts a list of intensity values into the normalization parameters.
   *
   * @param intensity_values the intensity values to insert
   */
  public void insertIntensity(Contour intensity_values) {
    for (Pair<Double, Double> intensity : intensity_values) {
      insertIntensity(intensity.second);
    }
  }

  /**
   * Determines if a feature can be normalized by this parameter object.
   *
   * @param feature_name the name of the feature to normalize.
   * @return true if the feature can be normalized, false if it cannot.
   */
  public boolean canNormalize(String feature_name) {
    return params.containsKey(feature_name);
  }

  /**
   * Generates a string representation of the normalization parameters.
   */
  public String toString() {
    String s = "";
    s += "f0: mean " + params.get("f0").getMean() + " - stdev " + params.get("f0").getStdev();
    s += "\nI: mean " + params.get("I").getMean() + " - stdev " + params.get("I").getStdev();
    return s;
  }
}
