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
package edu.cuny.qc.speech.AuToBI;

import java.util.Collection;
import java.util.Iterator;

/**
 * The results from an evaluation -- without storing instances
 */
public class EvaluationResults {
  private String[] classNames;         // an array of class labels
  private double[][] confusionMatrix;  // the confusion matrix


  /**
   * Constructs a new EvaluationResults object
   *
   * @param classNames      the class names
   * @param confusionMatrix the confusion matrix
   * @throws AuToBIException if there are inconsistencies between the length of class names and confusion matrix
   */
  public EvaluationResults(String[] classNames, double[][] confusionMatrix) throws AuToBIException {
    if (classNames.length != confusionMatrix.length) {
      throw new AuToBIException("inconsistent lengths of class name and confusion matrix");
    }
    if (classNames.length != confusionMatrix[0].length) {
      throw new AuToBIException("inconsistent lengths of class name and confusion matrix");
    }

    this.classNames = classNames;
    this.confusionMatrix = confusionMatrix;
  }

  /**
   * Constructs an empty EvalutionResults object with a confusion matrix set up to handle results
   * for class names.
   *
   * @param class_names the classes
   */
  public EvaluationResults(Collection<String> class_names) {
    this.classNames = new String[class_names.size()];
    Iterator<String> iter = class_names.iterator();
    this.confusionMatrix = new double[classNames.length][classNames.length];
    for (int i = 0; i < classNames.length; ++i) {
      classNames[i] = iter.next();
      for (int j = 0; j < classNames.length; ++j) {
        confusionMatrix[i][j] = 0;
      }
    }
  }

  /**
   * Constructs an empty EvalutionResults object with a confusion matrix set up to handle results
   * for class names.
   *
   * @param class_names the classes
   */
  public EvaluationResults(String[] class_names) {
    this.classNames = class_names;
    this.confusionMatrix = new double[classNames.length][classNames.length];
    for (int i = 0; i < classNames.length; ++i) {
      for (int j = 0; j < classNames.length; ++j) {
        confusionMatrix[i][j] = 0;
      }
    }
  }

  /**
   * Retrieves the class names
   *
   * @return the class names
   */
  public String[] getClassNames() {
    return classNames;
  }

  /**
   * Add a classified instance to the evaluation results
   *
   * @param hyp_class  the name of the hypothesized class
   * @param true_class the name of the true class
   * @throws AuToBIException if either class name is invalid
   */
  public void addInstance(String hyp_class, String true_class) throws AuToBIException {
    int i = lookupClassName(true_class);
    int j = lookupClassName(hyp_class);
    confusionMatrix[i][j]++;
  }

  /**
   * Add N classified instances to the evaluation results
   *
   * @param hyp_class  the name of the hypothesized class
   * @param true_class the name of the true class
   * @param n          The number of instances to add
   * @throws AuToBIException if either class name is invalid
   */
  public void addInstances(String hyp_class, String true_class, Integer n) throws AuToBIException {
    int i = lookupClassName(true_class);
    int j = lookupClassName(hyp_class);
    confusionMatrix[i][j] += n;
  }

  /**
   * Add instances from one EvaluationResults to this EvaluationResult
   *
   * @param results the EvaluationResults to add to this.
   * @throws AuToBIException If the two objects are incompatible.
   */
  public void add(EvaluationResults results) throws AuToBIException {
    for (String true_class : results.classNames) {
      for (String hyp_class : results.classNames) {
        this.addInstances(hyp_class, true_class, results.getInstances(hyp_class, true_class).intValue());
      }
    }
  }

  public Double getInstances(String hyp_class, String true_class) throws AuToBIException {
    int i = lookupClassName(true_class);
    int j = lookupClassName(hyp_class);
    return confusionMatrix[i][j];
  }

  /**
   * Get the number of classes in this result
   *
   * @return the number of classes
   */
  public int getNumClasses() {
    return classNames.length;
  }

  /**
   * Get the numebr of correctly classified instances
   *
   * @return the number of correctly classified instances
   */
  public int getNumCorrect() {
    int sum = 0;
    for (int i = 0; i < classNames.length; ++i) {
      sum += confusionMatrix[i][i];
    }
    return sum;
  }

  /**
   * The total number of instances stored in this object
   *
   * @return N
   */
  public int getNumInstances() {
    int n = 0;
    for (int i = 0; i < classNames.length; ++i) {
      for (int j = 0; j < classNames.length; ++j) {
        n += confusionMatrix[i][j];
      }
    }
    return n;
  }

  /**
   * Get the number of instances of a particular class
   *
   * @param class_name the class to query
   * @return the number of instances that are members of the class
   * @throws AuToBIException if class_name is invalid
   */
  public Integer getNumClassInstances(String class_name) throws AuToBIException {
    int i = lookupClassName(class_name);
    int sum = 0;
    for (int j = 0; j < classNames.length; ++j) {
      sum += confusionMatrix[i][j];
    }
    return sum;
  }

  /**
   * Get the number of instances classified as a class name
   *
   * @param class_name The class to query
   * @return the number of instances classified as class name
   * @throws AuToBIException if class_name is invalid
   */
  public Integer getNumClassifiedAs(String class_name) throws AuToBIException {
    int j = lookupClassName(class_name);
    int sum = 0;
    for (int i = 0; i < classNames.length; ++i) {
      sum += confusionMatrix[i][j];
    }
    return sum;
  }

  /**
   * Get the number of correctly classified instances with of particular class
   *
   * @param class_name the class to query
   * @return the number of correctly classified instances
   * @throws AuToBIException if class_name is invalid
   */
  public double getNumCorrectClassInstances(String class_name) throws AuToBIException {
    int i = lookupClassName(class_name);
    return confusionMatrix[i][i];
  }

  /**
   * Get the index of a class name
   *
   * @param class_name the class name
   * @return the index of class name
   * @throws AuToBIException if class_name is invalid
   */
  private int lookupClassName(String class_name) throws AuToBIException {
    for (int i = 0; i < classNames.length; ++i) {
      if (class_name.equals(classNames[i])) {
        return i;
      }
    }
    throw new AuToBIException("No value for class name: " + class_name);
  }

  /**
   * Return the percentage of correct classifications
   *
   * @return percentage correctly classified
   */
  public double getPctCorrect() {
    return getNumCorrect() / (1.0 * getNumInstances());
  }

  /**
   * Calculate the F Measure for a particular class (Beta = 1)
   *
   * @param class_name The class name
   * @return the f-measure for class name
   * @throws AuToBIException if class_name is invalid
   */
  public double getFMeasure(String class_name) throws AuToBIException {
    double precision = getPrecision(class_name);
    double recall = getRecall(class_name);
    if (precision + recall == 0) {
      return 0;
    } else {
      return (2 * precision * recall) / (precision + recall);
    }
  }

  /**
   * Calculate the precision for a particular class
   *
   * @param class_name The class name
   * @return the precision for class name
   * @throws AuToBIException if class_name is invalid
   */
  public double getPrecision(String class_name) throws AuToBIException {
    if (getNumClassifiedAs(class_name) == 0) {
      return 0;
    }
    return getNumCorrectClassInstances(class_name) / getNumClassifiedAs(class_name);
  }

  /**
   * Calculate the recall for a particular class
   *
   * @param class_name The class name
   * @return the recall for class name
   * @throws AuToBIException if class_name is invalid
   */
  public double getRecall(String class_name) throws AuToBIException {
    if (getNumClassInstances(class_name) == 0) {
      return 0;
    }
    return getNumCorrectClassInstances(class_name) / getNumClassInstances(class_name);
  }

  /**
   * Calculate the false positive (type 1 error) rate for class, class_name.
   * <p/>
   * The rate that negative instances are classified as class_name
   *
   * @param class_name the class to calculate the false positive rate over
   * @return the false positive rate
   * @throws AuToBIException if the class_name is invalid
   */
  public double getFalsePositiveRate(String class_name) throws AuToBIException {
    double false_positives = getNumClassifiedAs(class_name) - getInstances(class_name, class_name);
    double negative_instances = getNumInstances() - getNumClassInstances(class_name);

    return false_positives / negative_instances;
  }

  /**
   * Calculate the false negative (type II error) rate for class, class name.
   * <p/>
   * The rate that positive instances are classified as another class.
   *
   * @param class_name the class to calculate the false negative rate over
   * @return the false negative rate
   * @throws AuToBIException if the class_name is invalid.
   */
  public double getFalseNegativeRate(String class_name) throws AuToBIException {
    double false_negatives = getNumClassInstances(class_name) - getInstances(class_name, class_name);
    double positive_instances = getNumClassInstances(class_name);

    if (positive_instances == 0) {
      return 0;
    }
    return false_negatives / positive_instances;
  }

  /**
   * Calculate the weighted mean of minority class f-measures
   *
   * @param majority_class the name of the majority class
   * @return the weighted mean of minority class f-measures
   * @throws AuToBIException if the majority class name doesn't exist
   */
  public Double getWeightedMeanOfMinorityClassFmeasure(String majority_class) throws AuToBIException {
    int n = 0;
    double fmeasure = 0.0;
    for (String class_name : classNames) {
      if (!class_name.equals(majority_class)) {
        n += getNumClassInstances(class_name);
        if (!Double.isNaN(getFMeasure(class_name))) {
          fmeasure += getFMeasure(class_name) * getNumClassInstances(class_name);
        }
      }
    }
    return fmeasure / n;
  }

  /**
   * Calculates the combined f-measure of an evaluation.
   *
   * The combined f-measure is a weighted f-measure between the majority class f-measure and the weighted mean
   * of minority class f-measures.
   *
   * The two are combined using a weighted harmonic mean where the weight, beta, is N-maj/maj where maj is the size
   * of the majority class.
   *
   * @param majority_class The label of the majority class
   * @return the combined f-measure
   * @throws AuToBIException if the majority class does not exist
   */
  public Double getCombinedFMeasure(String majority_class) throws AuToBIException {
    Double maj_fmeasure = getFMeasure(majority_class);
    Double min_fmeasure = getWeightedMeanOfMinorityClassFmeasure(majority_class);
    Integer majority_size = getNumClassInstances(majority_class);
    Double beta = (getNumInstances() - majority_size) / majority_size.doubleValue();

    return (1 + beta) * maj_fmeasure * min_fmeasure / (beta * maj_fmeasure + min_fmeasure);
  }

  /**
   * Return the "balanced error rate" of the contingency matrix.
   * <p/>
   * This is a little bit of a misnomer -- it's actually unweighted average recall.
   * But that's the term coined in Read and Cox 2007
   *
   * @return BER value
   */
  public Double getBalancedErrorRate() {
    Double ber = 0.0;

    for (String class_name : getClassNames()) {
      try {
        ber += 1 - getRecall(class_name);
      } catch (AuToBIException e) {
        ber += 0;
      }
    }

    ber /= getNumClasses();
    return ber;
  }

  /**
   * Return the Mutual Information between the hypothesized and true classifications.
   *
   * @return Mutual Information
   */
  public Double getMutualInformation() {
    Double mi = 0.0;
    for (String class_x : getClassNames()) {
      for (String class_y : getClassNames()) {
        try {
          if (getNumClassInstances(class_x) > 0 && getNumClassifiedAs(class_y) > 0 &&
              getInstances(class_x, class_y) > 0) {
            Double p_xy = getInstances(class_x, class_y) / getNumInstances();
            Double p_x = getNumClassInstances(class_x).doubleValue() / getNumInstances();
            Double p_y = getNumClassifiedAs(class_y).doubleValue() / getNumInstances();
            mi += p_xy * Math.log(p_xy / (p_x * p_y));
          }
        } catch (AuToBIException e) {
          e.printStackTrace();
        }
      }
    }
    return mi;
  }

  /**
   * Constructs a string of the contingency matrix.
   *
   * @return the contingency matrix
   */
  private String printContingencyMatrix() {
    String s = "";
    s += String.format(" %1$10s", "");
    for (String c : getClassNames()) {
      s += String.format("%1$6s ", c.substring(0, Math.min(5, c.length())));
    }
    s += "\n";
    for (String c1 : getClassNames()) {
      s += "-" + String.format("%1$10s ", c1);
      for (String c2 : getClassNames()) {
        int count = 0;
        try {
          count = getInstances(c1, c2).intValue();
        } catch (AuToBIException e) {
          e.printStackTrace();
        }
        s += String.format("%1$6d ", count);
      }
      s += "\n";
    }
    return s;
  }

  /**
   * Constructs a string representation of the object.
   * @return the string
   */
  public String toString() {
    return printContingencyMatrix();
  }
}
