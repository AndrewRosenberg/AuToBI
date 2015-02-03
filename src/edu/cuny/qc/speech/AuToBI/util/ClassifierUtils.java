/*  ClassifierUtils.java

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
package edu.cuny.qc.speech.AuToBI.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.bwaldvogel.liblinear.*;
import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.WeightFunction;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.core.Feature;
import weka.core.*;

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
   * Writes an AuToBIClassifier to a file.
   *
   * @param filename the filename to write the classifier to.
   * @param c        the classifier to store
   */
  public static void writeAuToBIClassifier(String filename, AuToBIClassifier c) throws IOException {
    AuToBIUtils.log("writing model to: " + filename);
    FileOutputStream fos;
    ObjectOutputStream out;
    fos = new FileOutputStream(filename);
    out = new ObjectOutputStream(fos);
    out.writeObject(c);
    out.close();

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
    ArrayList<Attribute> attributes = generateWekaAttributes(features);
    return constructWekaInstance(attributes, point, class_attribute);
  }

  /**
   * Generate a FastVector of weka Attributes from a set of features.
   *
   * @param features the set of features
   * @return a FastVector of weka attributes
   */
  public static ArrayList<Attribute> generateWekaAttributes(Set<Feature> features) {
    ArrayList<Attribute> attributes = new ArrayList<Attribute>();

    for (Feature f : features) {
      String attribute_name = f.getName();
      if (f.isNominal()) {
        List<String> attribute_values = new ArrayList<String>();
        for (String s : f.getNominalValues()) {
          attribute_values.add(s);
        }
        attributes.add(new Attribute(attribute_name, attribute_values, attributes.size()));
      } else if (f.isString()) {
        attributes.add(new weka.core.Attribute(attribute_name, (List<String>) null, attributes.size()));
      } else {
        attributes.add(new weka.core.Attribute(attribute_name, attributes.size()));
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
    ArrayList<Attribute> attributes = generateWekaAttributes(feature_set.getFeatures());
    Instances instances = new Instances("AuToBI_feature_set", attributes, feature_set.getDataPoints().size());
    for (Word w : feature_set.getDataPoints()) {
      Instance inst = ClassifierUtils.assignWekaAttributes(instances, w);
      instances.add(inst);
    }

    ClassifierUtils.setWekaClassAttribute(instances, feature_set.getClassAttribute());
    return instances;
  }

  /**
   * Converts a feature set object to a weka Instances object.
   * <p/>
   * Use wekas instance weighting capability to assign weights for each data point.
   *
   * @param feature_set the feature set to convert
   * @param fn          a weight function
   * @return a weka instances object
   */
  public static Instances convertFeatureSetToWeightedWekaInstances(FeatureSet feature_set,
                                                                   WeightFunction fn) {
    ArrayList<Attribute> attributes = generateWekaAttributes(feature_set.getFeatures());
    Instances instances = new Instances("AuToBI_feature_set", attributes, feature_set.getDataPoints().size());
    for (Word w : feature_set.getDataPoints()) {
      Instance inst = ClassifierUtils.assignWekaAttributes(instances, w);
      inst.setWeight(fn.weight(w));
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
  protected static Instance constructWekaInstance(ArrayList<Attribute> attributes, Word data_point,
                                                  String class_attribute) {
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
        instance[i] = Utils.missingValue();
      }
    }

    Instance inst = new DenseInstance(1, instance);
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
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException IF there is an inconsistency in the evalution
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
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException IF there is an inconsistency in the evalution
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
      if (w.hasAttribute("__ignore__") && ((Boolean) w.getAttribute("__ignore__"))) {
        continue;
      }
      if (!w.hasAttribute(hyp_feature)) {
        AuToBIUtils.warn("Word, " + w + ", has no hypothesized attribute: " + hyp_feature);
      } else if (!w.hasAttribute(true_feature)) {
        AuToBIUtils.warn("Word, " + w + ", has no true attribute: " + hyp_feature);
      } else {
        eval.addInstance(w.getAttribute(hyp_feature).toString(), w.getAttribute(true_feature).toString());
      }
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

  /**
   * Converts a FeatureSet to a list of LibLinear labels.
   *
   * @param feature_set  The feature set
   * @param class_values An array of class values to describe the indexing of the labels.
   * @return a list of doubles corresponding to labels.
   */
  public static double[] convertFeatureSetToLibLinearLabels(FeatureSet feature_set,
                                                            String[] class_values) {
    String class_attribute = feature_set.getClassAttribute();
    double[] labels = new double[feature_set.getDataPoints().size()];
    int i = 0;
    for (Word w : feature_set.getDataPoints()) {
      String s = w.getAttribute(class_attribute).toString();
      labels[i] = java.util.Arrays.asList(class_values).indexOf(s) + 1;
      i++;
    }
    return labels;
  }

  /**
   * Converts a FeatureSet to a list of LibLinear Feature[] descriptions.
   *
   * @param feature_set the feature set to convert
   * @param feature_map a map of features to indices
   * @return a list of Feature[] descriptions.
   */
  public static de.bwaldvogel.liblinear.Feature[][] convertFeatureSetToLibLinearFeatures(FeatureSet feature_set,
                                                                                         HashBiMap<Feature,
                                                                                             Integer> feature_map)
      throws
      AuToBIException {
    int n = feature_set.getDataPoints().size();
    de.bwaldvogel.liblinear.Feature[][] features = new de.bwaldvogel.liblinear.Feature[n][];
    int i = 0;
    for (Word w : feature_set.getDataPoints()) {
      features[i] = convertWordToLibLinearFeatures(w, feature_map);
      i++;
    }

    return features;
  }

  /**
   * Converts a FeatureSet to a list of LibLinear Feature[] descriptions.
   *
   * @param feature_set the feature set to convert
   * @return a list of Feature[] descriptions.
   */
  public static de.bwaldvogel.liblinear.Feature[][] convertFeatureSetToLibLinearFeatures(FeatureSet feature_set)
      throws
      AuToBIException {

    HashBiMap<Feature, Integer> feature_map = HashBiMap.create();
    int i = 1;
    for (Feature f : feature_set.getFeatures()) {
      feature_map.put(f, i);
      i++;
    }

    return convertFeatureSetToLibLinearFeatures(feature_set, feature_map);
  }

  public static de.bwaldvogel.liblinear.Feature[] convertWordToLibLinearFeatures(Word w,
                                                                                 HashBiMap<Feature,
                                                                                     Integer> feature_map) throws
      AuToBIException {

    ArrayList<FeatureNode> fs = new ArrayList<FeatureNode>();

    BiMap<Integer, Feature> map_feature = feature_map.inverse();

    for (int i = 1; i < feature_map.size(); i++) {
      Feature feature = map_feature.get(i);
      if (w.hasAttribute(feature.getName())) {
        if (feature.isString()) {
          throw new AuToBIException("Feature, " + feature.getName() +
              " is a 'string' feature.  LibLinear does not support this feature type.");
        } else if (feature.isNominal()) {
          double idx = feature.getNominalIndex((String) w.getAttribute(feature.getName()));
          fs.add(new FeatureNode(i, idx));
        } else {
          Double value = (Double) w.getAttribute(feature.getName());
          if (!Double.isNaN(value)) {
            fs.add(new FeatureNode(i, value));
          }
        }
      }
    }

    return fs.toArray(new de.bwaldvogel.liblinear.Feature[fs.size()]);
  }

  /**
   * Normalizes features using precomputed statistics to 0 mean and unit variance.
   *
   * @param features    input (unscaled) features
   * @param feature_map a map of features to indices
   * @return normalized features
   */
  public static de.bwaldvogel.liblinear.Feature[][] normalizeLibLinearFeatures(
      de.bwaldvogel.liblinear.Feature[][] features, BiMap<Integer, Feature> feature_map,
      HashMap<String, Aggregation> norm_params) {

    de.bwaldvogel.liblinear.Feature[][] f_out = features.clone();

    int i = 0;
    for (de.bwaldvogel.liblinear.Feature[] f : features) {
      f_out[i] = normalizeLibLinearFeatures(f, feature_map, norm_params);
      i++;
    }

    return f_out;
  }

  /**
   * Normalizes features so they have 0 mean and unit variance.
   *
   * @param features    input (unscaled) features
   * @param feature_map a map of features to indices
   * @return normalized features
   */
  public static de.bwaldvogel.liblinear.Feature[] normalizeLibLinearFeatures(
      de.bwaldvogel.liblinear.Feature[] features, BiMap<Integer, Feature> feature_map,
      HashMap<String, Aggregation> norm_params) {

    de.bwaldvogel.liblinear.Feature[] f_out = features.clone();

    for (de.bwaldvogel.liblinear.Feature fn : f_out) {
      if (!feature_map.containsKey(fn.getIndex())) {
        fn.setValue(0);
      } else {
        Aggregation agg = norm_params.get(feature_map.get(fn.getIndex()).getName());
        if (agg.getSize() < 2) {
          fn.setValue(0);
        } else {
          double u = agg.getMean();
          double sd = agg.getStdev();
          fn.setValue((fn.getValue() - u) / sd);
          if (Double.isNaN(fn.getValue())) {
            fn.setValue(0);
          }
        }
      }
    }

    return f_out;
  }

  /**
   * Generates normalization parameters based on the data availble in a feature set.
   *
   * @param feature_set the feature set to analyze
   * @return a hash containing aggregations to be used for normaliation
   */
  public static HashMap<String, Aggregation> generateNormParams(FeatureSet feature_set) {
    HashMap<String, Aggregation> norm_params = new HashMap<String, Aggregation>();

    for (Word w : feature_set.getDataPoints()) {
      for (String f : feature_set.getFeatureNames()) {

        if (!norm_params.containsKey(f)) {
          norm_params.put(f, new Aggregation());
        }
        if (w.hasAttribute(f)) {
          Object v = w.getAttribute(f);
          if (v instanceof Number) {
            Double value = ((Number) v).doubleValue();
            if (!Double.isNaN(value)) {
              norm_params.get(f).insert(((Number) v).doubleValue());
            }
          }
        }
      }
    }

    return norm_params;
  }

  /**
   * Creates an invertible mapping from Feature names to liblinear compatible feature indices.
   *
   * @param feature_set the feature set
   * @return a bidirectional map between feature names and indices.
   */
  public static HashBiMap<Feature, Integer> generateFeatureMap(FeatureSet feature_set) {
    HashBiMap<Feature, Integer> feature_map = HashBiMap.create();
    int idx = 1;
    for (edu.cuny.qc.speech.AuToBI.core.Feature f : feature_set.getFeatures()) {
      feature_map.put(f, idx);
      idx++;
    }
    return feature_map;
  }
}
