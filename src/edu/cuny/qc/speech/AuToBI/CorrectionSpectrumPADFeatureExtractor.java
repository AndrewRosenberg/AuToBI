/*  CorrectionSpectrumPADFeatureExtractor.java

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

import java.util.List;

/**
 * A feature extraction to generate hypothesized correction values for spectrum based pitch accent detection.
 * <p/>
 * The Corrected Classifier is a two tiered ensemble technique where first predictions are generated, then they are
 * corrected using a second feature set.  This feature extraction routine generates hypotheses for the second --
 * correction -- tier.
 */
public class CorrectionSpectrumPADFeatureExtractor extends FeatureExtractor {

  private int low;                      // the low bark
  private int high;                     // the high bark
  private AuToBIClassifier classifier;  // The correction classifier
  private AuToBI autobi;                // An associated autobi object for feature extraction

  /**
   * Constructs a CorrectionSpectrumPADFeatureExtractor
   * <p/>
   * Each correction is associated with a frequency region defined in bark by low and high.
   *
   * @param low        the low bark value
   * @param high       the high bark value
   * @param classifier the correction classifier
   * @param autobi     an autobi object to manage the feature extraction for the correction classifier
   */
  CorrectionSpectrumPADFeatureExtractor(int low, int high, AuToBIClassifier classifier, AuToBI autobi) {
    this.low = low;
    this.high = high;
    this.classifier = classifier;
    this.autobi = autobi;

    extracted_features.add("nominal_bark_" + low + "_" + high + "__correction_prediction");
    extracted_features.add("bark_" + low + "_" + high + "__correction_prediction_confidence");
  }


  /**
   * Extracts correction features over each region.
   *
   * @param regions the regions to generate features for
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {

    // Construct a feature set.
    CorrectionSpectrumPADFeatureSet fs = new CorrectionSpectrumPADFeatureSet(low, high);

    fs.setDataPoints(regions);
    fs.setClassAttribute("nominal_PitchAccent");

    // Extract correction features.
    try {
      autobi.extractFeatures(fs, false);
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
    fs.constructFeatures();

    for (Word w : (List<Word>) regions) {
      try {
        Distribution result = classifier.distributionForInstance(w);

        w.setAttribute("nominal_bark_" + low + "_" + high + "__correction_prediction", result.getKeyWithMaximumValue());
        w.setAttribute("bark_" + low + "_" + high + "__correction_prediction_confidence",
                       result.get(result.getKeyWithMaximumValue()));
      } catch (Exception e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}