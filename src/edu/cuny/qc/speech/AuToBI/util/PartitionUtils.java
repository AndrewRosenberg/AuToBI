/*  PartitionUtils.java

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.util.*;

/**
 * PartitionUtils is a set of static functions used for constructing partitions of data points.
 * <p/>
 * This contains the methods that are used in the construction of cross validation folds, and selecting data points that
 * match a particular attribute.  This latter functionality is useful for calculating corpus statistics.
 */
public class PartitionUtils {
  private static Random rng = new Random();  // a random number generator.

  // Utility functions cannot be constructed.
  private PartitionUtils() {
    throw new AssertionError();
  }

  /**
   * Randomly assigns a fold number to each point in data_points for cross validation
   * <p/>
   * Fold numbers range from 0 to num_folds-1.
   *
   * @param data_points  The data points to assign numbers to
   * @param feature_name The feature that will store the fold assignment
   * @param num_folds    The total number of folds to assign
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          if the number of folds is invalid
   */
  public static void assignFoldNum(List<Word> data_points, String feature_name, Integer num_folds)
      throws AuToBIException {
    if (num_folds < 1) {
      throw new AuToBIException("The number of folds must be positive.");
    }
    for (Region r : data_points) {
      int j = rng.nextInt(num_folds);
      r.setAttribute(feature_name, j);
    }
  }

  /**
   * Randomly assigns a fold number to each label in strings for cross validation.
   * <p/>
   * This is commonly used for cross validation over filenames rather than sets of words.
   * <p/>
   * The assignment is returns as a mapping between the string and the fold assignment which ranges between 0 and
   * num_folds-1.
   *
   * @param strings   the strings
   * @param num_folds the number of generated folds
   * @return a map from strings to fold numbers
   */
  public static HashMap<String, Integer> generateXValFoldAssignment(List<String> strings, int num_folds) {
    HashMap<String, Integer> h = new HashMap<String, Integer>();
    for (String string : strings) {
      int j = rng.nextInt(num_folds);
      h.put(string, j);
    }
    return h;
  }

  /**
   * Assigns stratified cross validation fold numbers to the data points.
   * <p/>
   * In stratified cross validation the class attribute distribution is (as closely as possible) reflected in each cross
   * validation fold.
   *
   * @param data_points     The data points.
   * @param feature_name    The feature to store the fold assignment on
   * @param num_folds       the numer of folds to construct
   * @param class_attribute the class attribute
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          If there is a region that does not have an associated class attribute
   */
  public static void assignStratifiedFoldNum(List<Word> data_points, String feature_name, Integer num_folds,
                                             String class_attribute) throws AuToBIException {
    Map<String, Integer> assigner = new HashMap<String, Integer>();

    for (Word r : data_points) {
      if (r.hasAttribute(class_attribute)) {
        String key = r.getAttribute(class_attribute).toString();
        Integer value = 0;
        if (assigner.containsKey(key)) {
          value = assigner.get(key);
        }
        r.setAttribute(feature_name, value);
        ++value;
        if (value.equals(num_folds)) {
          value = 0;
        }
        assigner.put(key, value);
      } else {
        throw new AuToBIException(
            "No class_attribute, " + class_attribute + ", feature assigned on Region: " + r.toString());
      }
    }
  }

  /**
   * Divides a list of data points into mutually exclusive lists of training and testing points The division is based on
   * a fold feature indicating the test set assignment of the data point Those points whose fold assignment is equal to
   * foldNum are placed in the testing set.
   *
   * @param dataPoints      The set of data points to be split
   * @param trainingPoints  The destination list for the training data
   * @param testingPoints   The destination list for the testing data
   * @param foldNum         The cross validation fold number
   * @param foldFeatureName The attribute name specifying the xval assignment of the data point
   */
  public static void splitData(List<Word> dataPoints, List<Word> trainingPoints, List<Word> testingPoints,
                               Integer foldNum, String foldFeatureName) throws AuToBIException {
    trainingPoints.clear();
    testingPoints.clear();

    for (Word word : dataPoints) {
      if (!word.hasAttribute(foldFeatureName))
        throw new AuToBIException("Word does not have a fold assignment stored in feature: " + foldFeatureName);
      if ((word.getAttribute(foldFeatureName)).equals(foldNum)) {
        testingPoints.add(word);
      } else {
        trainingPoints.add(word);
      }
    }
  }

  /**
   * Splits a list of strings into training and testing sets based on a previously generated fold assignment hash.
   * <p/>
   * Each string that has been assigned to the specified fold is included in the testing set, every other string is used
   * in the training set.
   * <p/>
   * If fold_num is outside of the range used in the fold_assignment hash, every string will be assigned to the training
   * set.
   *
   * @param strings          the initial set of strings
   * @param training_strings the strings assigned to the training set
   * @param testing_strings  the strings assigned to the testing set
   * @param fold_assignment  the mapping from strings to folds
   * @param fold_num         the specified fold.
   */
  public static void splitData(List<String> strings, List<String> training_strings, List<String> testing_strings,
                               HashMap<String, Integer> fold_assignment, int fold_num) {
    training_strings.clear();
    testing_strings.clear();

    for (String string : strings) {
      if (fold_assignment.get(string) == fold_num) {
        testing_strings.add(string);
      } else {
        training_strings.add(string);
      }
    }
  }

  /**
   * Return all data points that have a feature_name attribute with feature_value
   *
   * @param data_points   The data points to filter
   * @param feature_name  The feature to filter on
   * @param feature_value The value to filter
   * @return A list containing all data points that have feature_name equal to feature_value.
   */
  public static List<Word> getAttributeMatchingWords(List<Word> data_points, String feature_name,
                                                     Object feature_value) {
    List<Word> filtered_words = new ArrayList<Word>();
    for (Word w : data_points) {
      if (w.hasAttribute(feature_name) && w.getAttribute(feature_name).equals(feature_value)) {
        filtered_words.add(w);
      }
    }
    return filtered_words;
  }

  /**
   * Generates a distribution of attribute values that appear in data_points.
   *
   * @param data_points The set of words to analyze
   * @param attribute   The attribute to generate a distribution of
   * @return A map of containing the distribution.
   */
  public static Distribution generateAttributeDistribution(List<Word> data_points, String attribute) {
    Distribution class_histogram = new Distribution();
    for (Word w : data_points) {
      class_histogram.add(w.getAttribute(attribute).toString());
    }
    return class_histogram;
  }
}
