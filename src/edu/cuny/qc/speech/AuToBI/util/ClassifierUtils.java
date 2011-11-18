/*  ClassifierUtils.java

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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.FastVector;
import weka.core.Attribute;

import java.util.*;
import java.io.*;


/**
 * A class containing static utility functions for classifiers.
 */
public class ClassifierUtils {

  // Utility classes cannot be initialized.
  private ClassifierUtils() {
    throw new AssertionError();
  }

  /**
   * Loads a serialized AuToBIClassifier from a file.
   *
   * @param filename the filename
   * @return the stored AuToBIClassifier
   */
  public static AuToBIClassifier readAuToBIClassifier(String filename) {
    FileInputStream fis;
    ObjectInputStream in;
    try {
      fis = new FileInputStream(filename);
      in = new ObjectInputStream(fis);
      Object o = in.readObject();
      if (o instanceof AuToBIClassifier) {
        return (AuToBIClassifier) o;
      }
    } catch (IOException e) {
      AuToBIUtils.error(e.getMessage());
    } catch (ClassNotFoundException e) {
      AuToBIUtils.error(e.getMessage());
    }
    return null;
  }


  /**
   * Converts a single point to a weka Instance.
   * <p/>
   * This conversion requires a FeatureSet to determine the features to include in the instance.
   *
   * @param point the data point.
   * @param fs    the containing feature set.
   * @return a weka instance of the point.
   * @throws Exception if something goes wrong
   */
  public static Instance convertWordToInstance(Word point, FeatureSet fs) throws Exception {
    return convertWordToInstance(point, fs.getFeatures(), fs.getClassAttribute());
  }

  /**
   * Converts a single point to a weka instance
   *
   * @param point           the data point
   * @param features        the features to include on the data point
   * @param class_attribute the class attribute
   * @return a weka instance of the point
   * @throws Exception if something goes wrong
   */
  public static Instance convertWordToInstance(Word point, Set<Feature> features, String class_attribute)
      throws Exception {
    FastVector attributes = generateWekaAttributes(features);
    return constructWekaInstance(attributes, point, class_attribute);
  }

  /**
   * Generate a FastVector of weka Attributes from a set of features.
   *
   * @param features the set of features
   * @return a FastVector of weka attributes
   */
  public static FastVector generateWekaAttributes(Set<Feature> features) {
    FastVector attributes = new FastVector();

    for (Feature f : features) {
      String attribute_name = f.getName();
      if (f.isNominal()) {
        FastVector attribute_values = new FastVector();
        for (String s : f.getNominalValues()) {
          attribute_values.addElement(s);
        }
        attributes.addElement(new Attribute(attribute_name, attribute_values, attributes.size()));
      } else if (f.isString()) {
        attributes.addElement(new weka.core.Attribute(attribute_name, (FastVector) null, attributes.size()));
      } else {
        attributes.addElement(new weka.core.Attribute(attribute_name, attributes.size()));
      }
    }
    return attributes;
  }

  /**
   * Converts a feature set object to a weka Instances object
   * <p/>
   * The class is set to the last attribute.
   *
   * @param feature_set the feature set to convert
   * @return a weka instances object
   * @throws Exception If the arff file can't be written or read.
   */
  public static Instances convertFeatureSetToWekaInstances(FeatureSet feature_set) throws Exception {
    FastVector attributes = generateWekaAttributes(feature_set.getFeatures());
    Instances instances = new Instances("AuToBI_feature_set", attributes, feature_set.getDataPoints().size());
    for (Word w : feature_set.getDataPoints()) {
      Instance inst = ClassifierUtils.assignWekaAttributes(instances, w);
      instances.add(inst);
    }

    ClassifierUtils.setWekaClassAttribute(instances, feature_set.getClassAttribute());
    return instances;
  }

  /**
   * Constructs a data point to a weka instance given a FastVector of weka attribute and a class attribute.
   *
   * @param attributes      a FastVector of weka attributes
   * @param data_point      the data point to convert
   * @param class_attribute the class attribute
   * @return a weka instance.
   */
  protected static Instance constructWekaInstance(FastVector attributes, Word data_point, String class_attribute) {
    Instances instances = new Instances("single_instance_set", attributes, 0);

    setWekaClassAttribute(instances, class_attribute);
    return assignWekaAttributes(instances, data_point);
  }

  /**
   * Given a (possibly empty) Instances object containing the required weka Attributes, generates a weka Instance for a
   * single data point.
   *
   * @param instances  the weka Instances object containing attributes
   * @param data_point the data point to convert
   * @return a weka instance with assigned attributes
   */
  protected static Instance assignWekaAttributes(Instances instances, Word data_point) {
    double[] instance = new double[instances.numAttributes()];

    for (int i = 0; i < instances.numAttributes(); ++i) {
      Attribute attribute = instances.attribute(i);
      if (data_point.hasAttribute(attribute.name()) &&
          !data_point.getAttribute(attribute.name()).toString().equals("?")) {
        switch (attribute.type()) {
          case Attribute.NOMINAL:
            int index = attribute.indexOfValue(data_point.getAttribute(attribute.name()).toString());
            instance[i] = (double) index;
            break;
          case Attribute.NUMERIC:
            // Check if value is really a number.
            try {
              instance[i] = Double.valueOf(data_point.getAttribute(attribute.name()).toString());
            } catch (NumberFormatException e) {
              AuToBIUtils.error("Number expected for feature: " + attribute.name());
            }
            break;
          case Attribute.STRING:
            instance[i] = attribute.addStringValue(data_point.getAttribute(attribute.name()).toString());
            break;
          default:
            AuToBIUtils.error("Unknown attribute type");
        }
      } else {
        instance[i] = Instance.missingValue();
      }
    }

    Instance inst = new Instance(1, instance);
    inst.setDataset(instances);
    return inst;
  }

  /**
   * Assigns a class attribute to a weka Instances object.
   * <p/>
   * If no class attribute is given, or if the class attribute is not found in the list of attributes, the last
   * attribute is set to the class attribute.
   *
   * @param instances       the instances object
   * @param class_attribute the desired class attribute.
   */
  static void setWekaClassAttribute(Instances instances, String class_attribute) {
    if (class_attribute != null) {
      int i = 0;
      boolean set = false;
      while (i < instances.numAttributes() && !set) {
        Attribute attr = instances.attribute(i);
        if (class_attribute.equals(attr.name())) {
          instances.setClassIndex(i);
          set = true;
        }
        ++i;
      }
      if (!set) {
        instances.setClassIndex(instances.numAttributes() - 1);
      }
    } else {
      instances.setClassIndex(instances.numAttributes() - 1);
    }
  }

  /**
   * Evaluates classification results by comparing the values of the hypothesized and true features.
   *
   * @param hyp_feature  The hypothesized feature name
   * @param true_feature The true feature name
   * @param fs           The feature set to be evaluated
   * @return a string representation of the evaluation
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          IF there is an inconsistency in the evalution
   */
  public static String evaluateClassification(String hyp_feature, String true_feature, FeatureSet fs)
      throws AuToBIException {
    EvaluationSummary eval = new EvaluationSummary(generateEvaluationResults(hyp_feature, true_feature, fs));
    return eval.toString();
  }

  /**
   * Generates an EvaluationResults object by comparing the values of the hypothesized and true features.
   * <p/>
   * EvaluationResults objects store contingency tables for the classification.
   *
   * @param hyp_feature  The hypothesized feature name
   * @param true_feature The true feature name
   * @param fs           The feature set to be evaluated
   * @return a string representation of the evaluation
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          IF there is an inconsistency in the evalution
   */
  public static EvaluationResults generateEvaluationResults(String hyp_feature, String true_feature, FeatureSet fs)
      throws AuToBIException {
    Feature class_attribute = new Feature(true_feature);
    class_attribute.generateNominalValues(fs.getDataPoints());

    Feature hyp_attribute = new Feature(hyp_feature);
    hyp_attribute.generateNominalValues(fs.getDataPoints());

    Set<String> sorted_values = new LinkedHashSet<String>();
    sorted_values.addAll(class_attribute.getNominalValues());
    sorted_values.addAll(hyp_attribute.getNominalValues());

    EvaluationResults eval = new EvaluationResults(sorted_values);

    for (Word w : fs.getDataPoints()) {
      eval.addInstance(w.getAttribute(hyp_feature).toString(), w.getAttribute(true_feature).toString());
    }
    return eval;
  }

  /**
   * Generates predictions for a set of words using the supplied classifier.
   * <p/>
   * Results are stored in hyp_attribute. If the classifier throws an error, the default_value is assigned as the
   * hypothesis
   *
   * @param classifier    the classifier to generate predictions
   * @param hyp_attribute the destination attribute for the hypotheses
   * @param default_value the default classification value
   * @param fs            the featureset to generate predictions for.
   */
  public static void generatePredictions(AuToBIClassifier classifier, String hyp_attribute, String default_value,
                                         FeatureSet fs) {
    for (Word w : fs.getDataPoints()) {
      try {
        String result = classifier.classify(w);
        w.setAttribute(hyp_attribute, result);
      } catch (Exception e) {
        w.setAttribute(hyp_attribute, default_value);
        AuToBIUtils.warn(
            "Classifier threw an exception. Assigning default value, " + default_value + ", to word, " + w.toString() +
                "\n" + e.getMessage());
      }
    }
  }

  /**
   * Generates predictions for a set of words using the supplied classifier.
   * <p/>
   * Results are stored in hyp_attribute. If the classifier throws an error, the default_value is assigned as the
   * hypothesis.
   * <p/>
   * Confidence scores are stored in a separate attribute.
   *
   * @param classifier     the classifier to generate predictions
   * @param hyp_attribute  the destination attribute for the hypotheses
   * @param conf_attribute the destination attribute for confidence in the hypothesis
   * @param default_value  the default classification value
   * @param fs             the featureset to generate predictions for.
   */
  public static void generatePredictionsWithConfidenceScores(AuToBIClassifier classifier, String hyp_attribute,
                                                             String conf_attribute, String default_value,
                                                             FeatureSet fs) {
    for (Word w : fs.getDataPoints()) {
      try {
        Distribution dist = classifier.distributionForInstance(w);
        String result = dist.getKeyWithMaximumValue();
        Double conf = dist.get(result);
        w.setAttribute(hyp_attribute, result);
        w.setAttribute(conf_attribute, conf);
      } catch (Exception e) {
        w.setAttribute(hyp_attribute, default_value);
        w.setAttribute(conf_attribute, 0.5);
        AuToBIUtils.warn(
            "Classifier threw an exception. Assigning default value, " + default_value + ", to word, " + w.toString() +
                "\n" + e.getMessage());

      }
    }
  }

  /**
   * Generates a prediction distribution for a set of words using the supplied classifier.
   * <p/>
   * Results are stored in dist_attribute. If the classifier throws an error, the default_value is assigned as the
   * hypothesis.
   * <p/>
   * Confidence scores are stored in a separate attribute.
   *
   * @param classifier     the classifier to generate predictions
   * @param dist_attribute the destination attribute for the hypotheses
   * @param default_value  the default value to return if the classifier throws an exception
   * @param fs             the featureset to generate predictions for.
   */
  public static void generatePredictionDistribution(AuToBIClassifier classifier, String dist_attribute,
                                                    String default_value, FeatureSet fs) {
    for (Word w : fs.getDataPoints()) {
      try {
        Distribution dist = classifier.distributionForInstance(w);
        w.setAttribute(dist_attribute, dist);
      } catch (Exception e) {
        w.setAttribute(dist_attribute, default_value);
        AuToBIUtils.warn(
            "Classifier threw an exception. Assigning default value, " + default_value + ", to word, " + w.toString() +
                "\n" + e.getMessage());
      }
    }
  }
}
