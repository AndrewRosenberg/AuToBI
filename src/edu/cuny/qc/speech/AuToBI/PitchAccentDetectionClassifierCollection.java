/*  PitchAccentDetectionClassifierCollection.java

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
