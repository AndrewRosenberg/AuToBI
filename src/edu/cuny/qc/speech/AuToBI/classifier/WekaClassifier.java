/*  WekaClassifier.java

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Feature;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Set;

/**
 * WekaClassifier is an AuToBI wrapper around a weka classifier.
 */
public class WekaClassifier extends AuToBIClassifier {

  private static final long serialVersionUID = 1633315748323749801L;

  private Classifier weka_classifier; // the weka classifier
  // Stored features are necessary for classifying a single data point.
  private Set<Feature> features;
  private String class_attribute;

  /**
   * Constructs a new WekaClassifier given a weka Classfiier object.
   *
   * @param classifier the weka Classifier.
   */
  public WekaClassifier(Classifier classifier) {
    this.weka_classifier = classifier;
    this.features = null;
    this.class_attribute = "";
  }

  /**
   * Evaluates the weka classifier on a single point.
   *
   * @param testing_point The point to evaluate
   * @return a distribution of the hypotheses.
   * @throws Exception
   */
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    Instance test_instance = ClassifierUtils.convertWordToInstance(testing_point, features, class_attribute);

    double[] distribution = weka_classifier.distributionForInstance(test_instance);

    Distribution d = new Distribution();
    for (int i = 0; i < test_instance.classAttribute().numValues(); ++i) {
      d.put(test_instance.classAttribute().value(i), distribution[i]);
    }
    return d;
  }

  /**
   * Trains the weka classifier based on training data supplied by a FeatureSet.
   *
   * @param feature_set The training data
   * @throws Exception if weka has a training problem.
   */
  public void train(FeatureSet feature_set) throws Exception {
    if (feature_set.getFeatures() == null || feature_set.getFeatures().size() == 0) {
      feature_set.constructFeatures();
    }
    setFeatures(feature_set.getFeatures());
    class_attribute = feature_set.getClassAttribute();

    Instances weka_instances = ClassifierUtils.convertFeatureSetToWekaInstances(feature_set);
    weka_classifier.buildClassifier(weka_instances);
  }

  /**
   * Constructs a copy of the object.
   *
   * @return a copy of the object.
   */
  public AuToBIClassifier newInstance() {
    try {
      return new WekaClassifier(Classifier.makeCopy(weka_classifier));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Sets the features used by the classifier
   *
   * @param features the features.
   */
  public void setFeatures(Set<Feature> features) {
    this.features = features;
  }

  /**
   * Constructs a string representation of the classifier.
   *
   * @return the string description of the classifier.
   */
  public String toString() {
    return weka_classifier.toString();
  }
}
