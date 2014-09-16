/*  MinimumClassEnsembleSampledClassifier.java

    Copyright (c) 2011-2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.PartitionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensemble Sampling Classifiers divide the training data into k samples.  Then k classifiers are trained. During
 * evaluation, each of the k classifiers generate hypotheses, which are combined using weighted majority voting.
 * <p/>
 * The k training samples are constructed such that each majority class token appears in one sample, while the remaining
 * minority class tokens appear in every sample.  Thus k is equal to the size of the majority class divided by the size
 * of the smallest class.
 */
public class MinimumClassEnsembleSampledClassifier extends EnsembleSampledClassifier {

  /**
   * Constructs an EnsembleSampledClassifier based on the supplied classifier.
   *
   * @param c the classifier
   */
  public MinimumClassEnsembleSampledClassifier(AuToBIClassifier c) {
    super(c);
  }

  /**
   * Train an ensemble of classifiers.
   *
   * @param feature_set The training data
   * @throws Exception if there is a training problem
   */
  public void train(FeatureSet feature_set) throws Exception {
    classifiers = new ArrayList<AuToBIClassifier>();
    List<FeatureSet> training_sets = constructEnsembleFeatureSets(feature_set);

    for (FeatureSet fs : training_sets) {
      AuToBIClassifier c = classifier.newInstance();
      c.train(fs);
      classifiers.add(c);
    }
  }

  /**
   * Construct a copy of the ensemble classifier.
   *
   * @return a new EnsembleSampledClassifierInstance
   */
  public AuToBIClassifier newInstance() {
    return new MinimumClassEnsembleSampledClassifier(classifier);
  }

  @Override
  public List<FeatureSet> constructEnsembleFeatureSets(FeatureSet training_set) {
    List<FeatureSet> training_sets = new ArrayList<FeatureSet>();

    // Identify the majority class.
    Distribution class_distribution =
        PartitionUtils.generateAttributeDistribution(training_set.getDataPoints(), training_set.getClassAttribute());


    Double majority_size = 0.0;
    Double smallest_size = Double.MAX_VALUE;
    for (String s : class_distribution.keySet()) {
      if (class_distribution.get(s) > majority_size) {

        majority_size = class_distribution.get(s);
      }
      if (class_distribution.get(s) < smallest_size) {
        smallest_size = class_distribution.get(s);
      }
    }

    // Assign folds to majority class data points -- each majority class data point exists in a single training set,
    // each other point exists in all of them.
    int num_folds = (int) Math.floor(majority_size / smallest_size);

    try {
      PartitionUtils.assignStratifiedFoldNum(training_set.getDataPoints(), "ensemble_sampling_fold", num_folds,
          training_set.getClassAttribute());

      // Generate training sets.
      for (int i = 0; i < num_folds; ++i) {
        FeatureSet sampled_training_set = training_set.newInstance();
        for (Word w : training_set.getDataPoints()) {
          if (!w.hasAttribute("ensemble_sampling_fold") || w.getAttribute("ensemble_sampling_fold").equals(i)) {
            sampled_training_set.getDataPoints().add(w);
          }
        }

        training_sets.add(sampled_training_set);
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }

    return training_sets;
  }
}
