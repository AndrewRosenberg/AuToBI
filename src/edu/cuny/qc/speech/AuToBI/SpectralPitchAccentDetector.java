/*  SpectralPitchAccentDetector.java

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
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;

/**
 * SpectralPitchAccentDetector is an ensemble classifier.
 * <p/>
 * The only operation this class performs is the weighted majority voting of externally trained classifiers.
 * <p/>
 * A set of classifiers trained using SpectrumPADTrainer and CorrectionSpectrumPADTrainer are responsible for
 * generating the individual predictions and correction hypotheses.  The corrections are applied and voting calculations
 * are made.
 */
public class SpectralPitchAccentDetector extends AuToBIClassifier {

  // Patterns of features used in the voting calculation.
  private final String prediction_pattern = "nominal_bark_##LOW##_##HIGH##__prediction";
  private final String prediction_confidence_pattern = "bark_##LOW##_##HIGH##__prediction_confidence";
  private final String correction_pattern = "nominal_bark_##LOW##_##HIGH##__correction_prediction";
  private final String correction_confidence_pattern = "bark_##LOW##_##HIGH##__correction_prediction_confidence";

  // The label of the positive class
  private final String positive_class = "ACCENTED";
  // The label of the negative class
  private final String negative_class = "DEACCENTED";
  // The label of the correct class
  private final String positive_correction = "CORRECT";

  // The maximum bark index -- typically this is 20, but can be trained differently.
  private int high_bark;

  /**
   * Constructs a new SpectralPitchAccentDetector to calculate a corrected weighted majority voting decision.
   *
   * @param high the high bark index.
   */
  public SpectralPitchAccentDetector(int high) {
    high_bark = high;
  }

  /**
   * Calculates the distribution from a weighted sum of votes from each corrected spectral classifier
   *
   * @param testing_point The point to evaluate
   * @return the hypothesized distribution
   * @throws Exception if something goes wrong
   */
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    Distribution dist = new Distribution();
    for (int low = 0; low < high_bark; ++low) {
      for (int high = low + 1; high <= high_bark; ++high) {
        String prediction_attr =
            prediction_pattern.replace("##LOW##", Integer.toString(low)).replace("##HIGH##", Integer.toString(high));
        String conf_attr = prediction_confidence_pattern.replace("##LOW##", Integer.toString(low))
            .replace("##HIGH##", Integer.toString(high));
        String correction_attr =
            correction_pattern.replace("##LOW##", Integer.toString(low)).replace("##HIGH##", Integer.toString(high));
        String correction_conf_attr = correction_confidence_pattern.replace("##LOW##", Integer.toString(low))
            .replace("##HIGH##", Integer.toString(high));

        double positive_conf;
        double correct_conf;

        // Total confidence in a positive prediction is calculated as C*C' + (1-C)*(1-C')
        // where C is the confidence of a positive prediction
        // and C' is the confidence that the prediction is correct.
        if (testing_point.getAttribute(prediction_attr).equals(positive_class)) {
          positive_conf = (Double) testing_point.getAttribute(conf_attr);
        } else {
          positive_conf = 1 - (Double) testing_point.getAttribute(conf_attr);
        }

        if (testing_point.getAttribute(correction_attr).equals(positive_correction)) {
          correct_conf = (Double) testing_point.getAttribute(correction_conf_attr);
        } else {
          correct_conf = 1 - (Double) testing_point.getAttribute(correction_conf_attr);
        }

        double overall_conf = positive_conf * correct_conf + (1 - positive_conf) * (1 - correct_conf);

        dist.add(positive_class, overall_conf);
        dist.add(negative_class, 1 - overall_conf);
      }
    }

    // Distribution is technically a histogram until it is normalized
    dist.normalize();
    return dist;
  }

  /**
   * SpectralPitchAccentDetector must be constructed from other trained classifiers.  This method always throws an
   * Exception if its called.
   *
   * @param feature_set The training data
   * @throws Exception every time its called.
   */
  public void train(FeatureSet feature_set) throws Exception {
    throw new AuToBIException(
        "SpectralPitchAccentDetector is not trained.  It relies on externally trained classifiers.");
  }

  /**
   * Copy constructor for SpectralPitchAccentDetector.
   *
   * @return a new copy of the SpectralPitchAccentDetector
   */
  public AuToBIClassifier newInstance() {
    return new SpectralPitchAccentDetector(high_bark);
  }
}
