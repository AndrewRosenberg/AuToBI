/**
 ClassWeightedLibLinear.java

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

import de.bwaldvogel.liblinear.*;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Feature;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * ClassWeightedLibLinear is a wrapper around a liblinear classifier with class based weights.
 */
public class ClassWeightedLibLinear extends AuToBIClassifier {

  private final SolverType solver;
  private final double C;
  private final double eps;
  protected Model classifier;
  // Stored features are necessary for classifying a single data point.
  protected Set<Feature> features;
  protected String class_attribute;
  protected String[] class_values;
//  protected Parameter parameter;

  public ClassWeightedLibLinear() {
    Linear.setDebugOutput(null);

    this.solver = SolverType.L2R_LR;
    this.C = 1.0;
    this.eps = 0.01;

  }

  @Override
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    de.bwaldvogel.liblinear.Feature[] instance =
        ClassifierUtils.convertWordToLibLinearFeatures(testing_point, features);

    double[] prob_estimates = new double[class_values.length];
    Linear.predictProbability(classifier, instance, prob_estimates);

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
    features = feature_set.getFeatures();
    class_attribute = feature_set.getClassAttribute();

    // Construct a list of valid values for the class attribtue.
    Feature class_feature = feature_set.getFeature(class_attribute);
    class_values = new String[class_feature.getNominalValues().size()];
    int j = 0;
    for (String s : class_feature.getNominalValues()) {
      class_values[j] = s;
      j++;
    }

    // Set up the liblinear problem
    Problem problem = new Problem();
    problem.l = feature_set.getDataPoints().size();
    problem.n = features.size();

    problem.x = ClassifierUtils.normalizeLibLinearFeatures(
        ClassifierUtils.convertFeatureSetToLibLinearFeatures(feature_set));
    problem.y = ClassifierUtils.convertFeatureSetToLibLinearLabels(feature_set, class_values);

    // calculate and set weights
    Map<String, Double> map =
        ClassBasedWeightFunctionTrainer.getClassWeightMapping(feature_set.getDataPoints(), class_attribute,
            ClassBasedWeightFunctionTrainer.WeightType.LINEAR);

    double[] weights = new double[class_values.length];
    int[] labels = new int[class_values.length];
    for (int i = 0; i < class_values.length; ++i) {
      labels[i] = i;
      weights[i] = map.get(class_values[i]);
    }
    parameter.setWeights(weights, labels);


    classifier = Linear.train(problem, parameter);
  }

  @Override
  public AuToBIClassifier newInstance() {
    ClassWeightedLibLinear c = new ClassWeightedLibLinear();
    c.classifier = classifier;
    c.features = new HashSet<Feature>(features);
    c.class_values = class_values.clone();
    c.class_attribute = class_attribute;
    return c;
  }
}
