/*  EnsembleSampledClassifier.java

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.PartitionUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * Ensemble Sampling Classifiers divide the training data into k samples.  Then k classifiers are trained. During
 * evaluation, each of the k classifiers generate hypohteses, which are combined using weighted majority voting.
 * <p/>
 * The k training samples are constructed such that each majority class token appears in one sample, while the remaining
 * minority class tokens appear in every sample.  Thus k is equal to the size of the majority class divided by the size
 * of the largest minority class.
 */
public class EnsembleSampledClassifier extends AuToBIClassifier {
  protected List<AuToBIClassifier> classifiers;  // the ensemble of trained classifiers
  protected AuToBIClassifier classifier;         // a placeholder for a single classifier


  /**
   * Constructs an EnsembleSampledClassifier based on the supplied classifier.
   *
   * @param c the classifier
   */
  public EnsembleSampledClassifier(AuToBIClassifier c) {
    this.classifier = c;
  }

  /**
   * Generate a distribution of hypotheses based on the ensemble of trained classifiers
   *
   * @param testing_point The point to evaluate
   * @return a distribution of hypotneses
   * @throws Exception if there is a classification problem
   */
  public Distribution distributionForInstance(Word testing_point) throws Exception {

    Distribution dist = new Distribution();
    for (AuToBIClassifier c : classifiers) {
      Distribution d = c.distributionForInstance(testing_point);

      for (String key : d.keySet()) {
        if (dist.containsKey(key)) {
          dist.put(key, dist.get(key) * d.get(key));
        } else {
          dist.put(key, d.get(key));
        }
      }
    }
    dist.normalize();
    return dist;
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
    return new EnsembleSampledClassifier(classifier);
  }

  public List<FeatureSet> constructEnsembleFeatureSets(FeatureSet training_set) throws AuToBIException {
    List<FeatureSet> training_sets = new ArrayList<FeatureSet>();

    // Identify the majority class.
    Distribution class_distribution =
        PartitionUtils.generateAttributeDistribution(training_set.getDataPoints(), training_set.getClassAttribute());

    String majority_class = null;
    Double majority_size = 0.0;
    Double second_largest_size = 0.0;
    for (String s : class_distribution.keySet()) {
      if (class_distribution.get(s) > majority_size) {
        second_largest_size = majority_size;
        majority_size = class_distribution.get(s);
        majority_class = s;
      }
    }

    // Assign folds to majority class data points -- each majority class data point exists in a single training set,
    // each other point exists in all of them.
    int num_folds = (int) Math.floor(majority_size / second_largest_size);
    List<Word> majority_class_points = PartitionUtils
        .getAttributeMatchingWords(training_set.getDataPoints(), training_set.getClassAttribute(), majority_class);
    PartitionUtils.assignFoldNum(majority_class_points, "ensemble_sampling_fold", num_folds);

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
    return training_sets;
  }
}
