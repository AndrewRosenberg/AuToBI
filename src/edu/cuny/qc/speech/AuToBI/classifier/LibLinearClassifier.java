/**
 LibLinearClassifier.java

 Copyright (c) 2014 Andrew Rosenberg

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

import com.google.common.collect.HashBiMap;
import de.bwaldvogel.liblinear.*;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * LibLinearClassifier is a wrapper around a liblinear classifier with class based weights.
 */
public class LibLinearClassifier extends AuToBIClassifier {

  private final SolverType solver;
  private final double C;
  private final double eps;
  private final boolean class_weighting;
  private HashMap<String, Aggregation> norm_map;
  private HashBiMap<edu.cuny.qc.speech.AuToBI.core.Feature, Integer> feature_map;

  protected Model classifier;

  protected String class_attribute;
  protected String[] class_values;

  public LibLinearClassifier() {
    this(false);
  }

  public LibLinearClassifier(boolean class_weighting) {
    this(SolverType.L2R_LR, class_weighting);
  }

  public LibLinearClassifier(SolverType solver, boolean class_weighting) {
    Linear.setDebugOutput(null);

    this.solver = solver;
    this.C = 1.0;
    this.eps = 0.01;
    this.class_weighting = class_weighting;

    this.norm_map = new HashMap<String, Aggregation>();
    this.feature_map = HashBiMap.create();
  }

  @Override
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    de.bwaldvogel.liblinear.Feature[] raw_instance =
        ClassifierUtils.convertWordToLibLinearFeatures(testing_point, feature_map);

    de.bwaldvogel.liblinear.Feature[] instance =
        ClassifierUtils.normalizeLibLinearFeatures(raw_instance, feature_map.inverse(), norm_map);

    double[] prob_estimates = new double[class_values.length];
//    double h = Linear.predictProbability(classifier, instance, prob_estimates);

    Distribution d = new Distribution();
    for (int i = 0; i < class_values.length; ++i)
      d.put(class_values[i], prob_estimates[i]);

    return d;
  }

  @Override
  /**
   * Trains a LibLinear model based on a FeatureSet
   *
   * @param feature_set the feature set
   */
  public void train(FeatureSet feature_set) throws Exception {
    Parameter parameter = new Parameter(solver, C, eps);
    feature_map = ClassifierUtils.generateFeatureMap(feature_set);
    class_attribute = feature_set.getClassAttribute();

    // Construct a list of valid values for the class attribtue.
    edu.cuny.qc.speech.AuToBI.core.Feature class_feature = feature_set.getFeature(class_attribute);
    class_values = new String[class_feature.getNominalValues().size()];
    int j = 0;
    for (String s : class_feature.getNominalValues()) {
      class_values[j] = s;
      j++;
    }

    // Set up the liblinear problem
    Problem problem = new Problem();
    problem.l = feature_set.getDataPoints().size();
    problem.n = feature_map.size();

    // convert feature set
    de.bwaldvogel.liblinear.Feature[][] raw_x =
        ClassifierUtils.convertFeatureSetToLibLinearFeatures(feature_set, feature_map);

    // calculate (and store) normalization parameters
    norm_map = ClassifierUtils.generateNormParams(feature_set);

    // normalize features
    problem.x = ClassifierUtils.normalizeLibLinearFeatures(raw_x, feature_map.inverse(), norm_map);
    problem.y = ClassifierUtils.convertFeatureSetToLibLinearLabels(feature_set, class_values);

    if (class_weighting) {
      // calculate and set weights
      Map<String, Double> map =
          ClassBasedWeightFunctionTrainer.getClassWeightMapping(feature_set.getDataPoints(), class_attribute,
              ClassBasedWeightFunctionTrainer.WeightType.LINEAR);

      double[] weights = new double[class_values.length];
      int[] labels = new int[class_values.length];
      for (int i = 0; i < class_values.length; ++i) {
        labels[i] = i + 1;
        weights[i] = map.get(class_values[i]);
      }
      parameter.setWeights(weights, labels);
    }

    // TODO xval tune parameters

    classifier = Linear.train(problem, parameter);

  }


  @Override
  public AuToBIClassifier newInstance() {
    LibLinearClassifier c = new LibLinearClassifier();
    c.classifier = classifier;
    c.class_values = class_values.clone();
    c.class_attribute = class_attribute;
    for (edu.cuny.qc.speech.AuToBI.core.Feature f : feature_map.keySet()) {
      c.feature_map.put(f, feature_map.get(f));
    }
    c.norm_map = (HashMap<String, Aggregation>) norm_map.clone();
    return c;
  }
}
