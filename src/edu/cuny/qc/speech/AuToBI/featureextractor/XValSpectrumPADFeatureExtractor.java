/*  XValSpectrumPADFeatureExtractor.java

    Copyright 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.*;
import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.WekaClassifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureset.SpectrumPADFeatureSet;
import edu.cuny.qc.speech.AuToBI.util.PartitionUtils;
import weka.classifiers.trees.J48;

import java.util.List;
import java.util.ArrayList;

/**
 * The XValSpectrumPADFeatureExtractor is used to generate cross vaidated predictions of spectral pitch accent detection
 * (PAD) hypotheses.  These xval hypotheses are used in training correction classfiers.
 */
public class XValSpectrumPADFeatureExtractor extends FeatureExtractor {
  private final String ACCENTED_VALUE = "ACCENTED";  // the accented value
  private final String FOLD_ASSIGNMENT_FEATURE = "FOLD_ASSIGNMENT";
  // an feature to store fold assignment information on
  private int low;  // the bottom of the frequency region
  private int high; // the top of the frequency region
  private int num_folds;  // the number of folds used in the hypothesis generation.
  private AuToBI autobi;  // an AuToBI object to run the feature extraction.

  /**
   * Constructs a new XValSpectrumFeatureExtractor for a specific spectral region.
   *
   * @param low       the bottom of the spectral region.
   * @param high      the top of the spectral region.
   * @param num_folds the number of folds to be used in the generation
   * @param autobi    an AuToBI object to manage the feature extraction
   */
  public XValSpectrumPADFeatureExtractor(int low, int high, int num_folds, AuToBI autobi) {
    this.low = low;
    this.high = high;
    this.num_folds = num_folds;
    this.autobi = autobi;

    extracted_features.add("nominal_bark_" + low + "_" + high + "__prediction");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence_accented");
  }

  /**
   * Generates cross validated pitch accent detection hypotheses using energy information drawn from the assigned
   * spectral region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Construct a feature set.
    SpectrumPADFeatureSet fs = new SpectrumPADFeatureSet(low, high);

    // Extract spectrum features.
    fs.setDataPoints((List<Word>) regions);
    try {
      autobi.extractFeatures(fs);
      PartitionUtils
          .assignStratifiedFoldNum((List<Word>) regions, FOLD_ASSIGNMENT_FEATURE, num_folds, fs.getClassAttribute());
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }

    // Train n-fold cross validated prediction features.
    for (int fold_num = 0; fold_num < num_folds; ++fold_num) {
      AuToBIClassifier classifier = new WekaClassifier(new J48());

      SpectrumPADFeatureSet training_fs = new SpectrumPADFeatureSet(low, high);


      List<Word> training_regions = new ArrayList<Word>();
      List<Word> testing_regions = new ArrayList<Word>();
      try {
        PartitionUtils
            .splitData((List<Word>) regions, training_regions, testing_regions, fold_num, FOLD_ASSIGNMENT_FEATURE);
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      training_fs.setDataPoints(training_regions);
      training_fs.constructFeatures();
      try {
        classifier.train(training_fs);
      } catch (Exception e) {
        throw new FeatureExtractorException(e.getMessage());
      }

      for (Word w : testing_regions) {
        try {
          Distribution result = classifier.distributionForInstance(w);

          w.setAttribute("nominal_bark_" + low + "_" + high + "__prediction", result.getKeyWithMaximumValue());
          w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence",
              result.get(result.getKeyWithMaximumValue()));
          w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence_accented", result.get(ACCENTED_VALUE));
        } catch (Exception e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
    }
  }
}