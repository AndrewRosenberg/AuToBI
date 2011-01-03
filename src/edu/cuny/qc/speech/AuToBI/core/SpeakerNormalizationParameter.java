/*  SpeakerNormalizationParameter.java

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

import java.io.Serializable;
import java.util.List;
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
    params.put("log_f0", new Aggregation());
    params.put("I", new Aggregation());
    params.put("log_I", new Aggregation());
  }

  /**
   * Retrieves the speaker id value
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
    params.get("log_f0").insert(Math.log(pitch));
  }

  /**
   * Inserts an intensity value.
   *
   * @param intensity the intensity value.
   */
  public void insertIntensity(double intensity) {
    params.get("I").insert(intensity);
    params.get("log_I").insert(Math.log(intensity));
  }

  /**
   * Normalizes a value.
   * <p/>
   * The user must specify what type of value is supplied so the appropriate normalization parameters can be used.
   *
   * @param feature the type of feature
   * @param value   the value of the feature
   * @return the zscore speaker normalized value
   */
  public double normalize(String feature, double value) {
    return (value - params.get(feature).getMean()) / params.get(feature).getStdev();
  }

  /**
   * Normalizes a pitch value.
   *
   * @param pitch the pitch value to normalize
   * @return the normalized value
   */
  public double normalizePitch(double pitch) {
    return normalize("f0", pitch);
  }

  /**
   * Normalizes a log pitch value.
   *
   * @param log_pitch the log pitch value to normalize
   * @return the normalized value
   */
  public double normalizeLogPitch(double log_pitch) {
    return normalize("log_f0", log_pitch);
  }

  /**
   * Normalizes an intensity value.
   *
   * @param intensity the intensity value to normalize
   * @return the normalized value
   */
  public double normalizeIntensity(double intensity) {
    return normalize("I", intensity);
  }

  /**
   * Normalizes a log intensity value.
   *
   * @param log_intensity the log intensity value to normalize.
   * @return the normalized log intensity value
   */
  public double normalizeLogIntensity(double log_intensity) {
    return normalize("log_I", log_intensity);
  }

  /**
   * Inserts a list of pitch values into the normalization parameters.
   *
   * @param pitch_values the pitch values to insert
   */
  public void insertPitch(List<TimeValuePair> pitch_values) {
    for (TimeValuePair pitch : pitch_values) {
      insertPitch(pitch.second);
    }
  }

  /**
   * Inserts a list of intensity values into the normalization parameters.
   *
   * @param intensity_values the intensity values to insert
   */
  public void insertIntensity(List<TimeValuePair> intensity_values) {
    for (TimeValuePair intensity : intensity_values) {
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
}
