/*  PitchAccentDetectionClassifierCollection.java

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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;

import java.io.Serializable;
import java.util.HashMap;

/**
 * PitchAccentDetectionClassifierCollection contains and serializes the ensemble of classifiers that are used in
 * corrected energy based pitch accent detection.
 * <p/>
 * This collection includes a set of pitch accent detection models, each of which is trained on a distinct frequency
 * band of the signal's spectum, and a second set of correction models resposible for correcting the hypotheses
 * from the energy based classifiers.
 * <p/>
 * This should be converted into a more general object to store classifier ensembles.
 */
public class PitchAccentDetectionClassifierCollection implements Serializable {
  private static final long serialVersionUID = 20100420L;

  private HashMap<String, AuToBIClassifier> pitch_accent_detectors;  // the initial pitch accent detectors
  private HashMap<String, AuToBIClassifier> correction_classifiers;  // the correcting classifiers

  /**
   * Constructs a new, empty, PitchAccentDetectionClassifierCollection.
   */
  public PitchAccentDetectionClassifierCollection() {
    pitch_accent_detectors = new HashMap<String, AuToBIClassifier>();
    correction_classifiers = new HashMap<String, AuToBIClassifier>();
  }

  /**
   * Sets a pitch accent detector for a specific frequency region.
   *
   * @param low        the low frequency (usually in bark)
   * @param high       the high frequency
   * @param classifier the classifier
   */
  public void setPitchAccentDetector(int low, int high, AuToBIClassifier classifier) {
    pitch_accent_detectors.put(generateKey(low, high), classifier);
  }

  /**
   * Gets a pitch accent detector for a specific frequency region
   *
   * @param low  the low frequency (usually in bark)
   * @param high the high frequency
   * @return the classifier
   */
  public AuToBIClassifier getPitchAccentDetector(int low, int high) {
    return pitch_accent_detectors.get(generateKey(low, high));
  }


  /**
   * Sets a correction classifier for a pitch accent detector trained on a specific frequency region.
   *
   * @param low        the low frequency (usually in bark)
   * @param high       the high frequency
   * @param classifier the classifier
   */
  public void setCorrectionClassifier(int low, int high, AuToBIClassifier classifier) {
    correction_classifiers.put(generateKey(low, high), classifier);
  }

  /**
   * Gets a correction classifier for a pitch accent detector trained on a specific frequency region.
   *
   * @param low  the low frequency (usually in bark)
   * @param high the high frequency
   * @return the classifier
   */
  public AuToBIClassifier getCorrectionClassifier(int low, int high) {
    return correction_classifiers.get(generateKey(low, high));
  }

  /**
   * Generates a string key based on the frequency region parameters.
   *
   * @param low  the low frequency (usually in bark)
   * @param high the high frequency
   * @return a string representing the region
   */
  private String generateKey(int low, int high) {
    return Integer.toString(low) + "_" + Integer.toString(high);
  }
}
