/*  AuToBIUtils.java

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

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

import de.bwaldvogel.liblinear.SolverType;
import edu.cuny.qc.speech.AuToBI.AuToBI;
import edu.cuny.qc.speech.AuToBI.classifier.LibLinearClassifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.AuToBIParameters;
import edu.cuny.qc.speech.AuToBI.core.AuToBITask;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import org.apache.oro.io.GlobFilenameFilter;

import org.apache.log4j.Logger;

/**
 * Stores general utility functions for AuToBI.
 * <p/>
 * Currently handles the resolution of filenames, an interface to log4j logging and string manipulation functions.
 */
public class AuToBIUtils {

  // Utility classes cannot be constructed.
  private AuToBIUtils() {
    throw new AssertionError();
  }

  // A log4j logger
  private static Logger logger = Logger.getLogger(AuToBIUtils.class);

  /**
   * Logs a message to the logger.
   *
   * @param s the message
   */
  public static void log(String s) {
    logger.info(s);
  }

  /**
   * Sends a debug message to the logger.
   *
   * @param s the message
   */
  public static void debug(String s) {
    logger.debug(s);
  }

  /**
   * Sends a warning message to the logger.
   *
   * @param s the message
   */
  public static void warn(String s) {
    logger.warn(s);
  }

  /**
   * Sends an error message to the logger.
   *
   * @param s the message
   */
  public static void error(String s) {
    logger.error(s);
  }

  /**
   * Sends an information message to the logger.
   *
   * @param s the message
   */
  public static void info(String s) {
    logger.info(s);
  }

  /**
   * Globs a file pattern into a list of full path names that match the pattern.
   *
   * @param pattern The file pattern
   * @return a list of matching files.
   */
  public static List<String> glob(String pattern) {
    File dir;

    ArrayList<String> filenames = new ArrayList<String>();
    if (pattern != null) {
      for (String file_pattern : pattern.split(",")) {
        file_pattern = file_pattern.replaceAll("~", System.getProperty("user.home"));
        if (!file_pattern.startsWith("/")) {
          file_pattern = System.getProperty("user.dir") + "/" + file_pattern;
        }
        file_pattern = file_pattern.substring(1, file_pattern.length());
        dir = new File("/");

        for (String file_name : getFileList(dir, file_pattern))
          filenames.add(file_name);
      }
    }
    return filenames;
  }

  /**
   * Globs a single file from a list of full path names that match the pattern.
   * <p/>
   * Throws an error if the pattern matches multiple files.
   *
   * @param pattern The file pattern.
   * @return the matching file
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException if the pattern matches multiple files
   */
  public static String globSingleFile(String pattern) throws AuToBIException {
    if (pattern.contains(",")) {
      throw new AuToBIException("Pattern, " + pattern + ", matches multiple files");
    }

    ArrayList<String> filenames = new ArrayList<String>();
    String file_pattern = pattern.replaceAll("~", System.getProperty("user.home"));
    if (!file_pattern.startsWith("/")) {
      file_pattern = System.getProperty("user.dir") + "/" + file_pattern;
    }
    file_pattern = file_pattern.substring(1, file_pattern.length());
    File dir = new File("/");

    for (String file_name : getFileList(dir, file_pattern))
      filenames.add(file_name);

    if (filenames.size() > 1) {
      throw new AuToBIException("Pattern, " + pattern + ", matches multiple files");
    }
    if (filenames.size() == 0) {
      throw new AuToBIException("Pattern, " + pattern + ", does not match any files");
    }
    return filenames.get(0);
  }

  /**
   * Convert a collection to a string with elements joined by a delimiter.
   * <p/>
   * e.g. ["a" "b" "c"] -> "a,b,c"
   *
   * @param collection the collection
   * @param delimiter  the delimiter
   * @return a string containing the elements of the collection
   */
  public static String join(Collection<?> collection, String delimiter) {
    if (collection.isEmpty()) return "";
    Iterator<?> iter = collection.iterator();
    StringBuilder buffer = new StringBuilder(iter.next().toString());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next().toString());
    return buffer.toString();
  }

  /**
   * Recursively generates the list of files within directory that match the file_pattern
   *
   * @param dir          The directory
   * @param file_pattern The filename pattern
   * @return A list of file names  in the directory that match the patter
   */
  public static List<String> getFileList(File dir, String file_pattern) {
    ArrayList<String> file_names = new ArrayList<String>();
    String[] path_elements = file_pattern.split("/");

    if (path_elements[0].equals(".")) {
      String next_file_pattern = construct_path_string(path_elements);
      file_names.addAll(getFileList(dir, next_file_pattern));
    } else if (path_elements[0].equals("..")) {// traverse up one directory
      String next_file_pattern = construct_path_string(path_elements);
      file_names.addAll(getFileList(dir.getParentFile(), next_file_pattern));
    } else {
      GlobFilenameFilter filter = new GlobFilenameFilter(path_elements[0]);
      File[] files = dir.listFiles((FilenameFilter) filter);
      for (File f : files) {
        if (path_elements.length > 1) {// traverse sub directories
          if (f.exists() && f.isDirectory()) {
            String next_file_pattern = construct_path_string(path_elements);
            file_names.addAll(getFileList(f, next_file_pattern));
          }
        } else {
          if (f.exists()) {
            file_names.add(f.getAbsolutePath());
          }
        }
      }
    }
    return file_names;
  }

  /**
   * Constructs a string representation of a path from an array of path elements
   * <p/>
   * e.g.
   * <p/>
   * ["" "usr" "local" "bin"] -> "/usr/local/bin"
   *
   * @param path_elements an array of path components
   * @return a string representation of the path
   */
  private static String construct_path_string(String[] path_elements) {
    String next_file_pattern = "";
    for (int i = 1; i < path_elements.length; ++i) {
      next_file_pattern += path_elements[i] + "/";
    }
    next_file_pattern = next_file_pattern.substring(0, next_file_pattern.length() - 1);
    return next_file_pattern;
  }

  /**
   * Parses a feature name into its moniker and paramers.
   * <p/>
   * The format of a feature name is moniker[param1,...,paramN], where params can themselves be features
   *
   * @param feature the feature name
   * @return a list containing the moniker followed by its parameters.
   */
  public static List<String> parseFeatureName(String feature) throws AuToBIException {
    if (feature.contains("[") && !feature.endsWith("]")) {
      throw new AuToBIException("Invalid feature name format: " + feature);
    }
    if (feature.replaceAll("\\[", "").length() != feature.replaceAll("\\]", "").length()) {
      throw new AuToBIException("Invalid feature name format: " + feature);
    }

    // Note: I'm sure there's a way to do this with a regular expression, but dealing with
    // matching brackets is frustrating
    ArrayList<String> ret = new ArrayList<String>();
    if (!feature.contains("[")) {
      ret.add(feature);
    } else {
      int openBracket = feature.indexOf('[');
      String moniker = feature.substring(0, openBracket);
      ret.add(moniker);

      // Parameters will keep the final closing bracket to allow the for loop below to recognize the
      // end of string.
      String parameters = feature.substring(openBracket + 1, feature.length());
      if (parameters.length() == 0) {
        throw new AuToBIException("Invalid feature name format: " + feature);
      }
      //ret.add(parameters);
      int start = 0;
      int open = 0;
      int close = 0;
      for (int i = 0; i < parameters.length(); ++i) {
        if (open == close && (parameters.charAt(i) == ',' || parameters.charAt(i) == ']')) {
          String f = parameters.substring(start, i);
          if (f.length() == 0) {
            throw new AuToBIException("Invalid feature name format: " + feature);
          }
          ret.add(f);
          start = i + 1;
        }
        if (parameters.charAt(i) == '[') {
          open++;
        }
        if (parameters.charAt(i) == ']') {
          close++;
        }
      }
    }
    return ret;
  }

  /**
   * Constructs merged hypotheses for phrase ending tones and pitch accents by merging hypotheses from the six detection
   * and classification tasks.
   *
   * @param autobi an AuToBI object to manage the task variables
   * @param words  the words to analyse
   */
  public static void mergeAuToBIHypotheses(AuToBI autobi, List<Word> words) throws AuToBIException {
    // TODO: deprecate this function and move the behavior into smaller utility functions
    // Convert each word to the desired string, one at a time.  Call these utilities from writeTextGridString
    // instead of using this as a pre-processing step.
    for (Word word : words) {
      // Assigns pitch accents to words.  If only accent detection is available, a binary True/False hypothesis
      // will be assigned.  If location and type hypotheses are available, the hypothesized type will be assigned.
      // Finally, if only type information is available, every word will be assigned its best guess for accent type.

      //TODO: make sure this works even if getHypothesizeFeature doesn't have a feature for this.
      if (autobi.getTasks().containsKey("pitch_accent_detection") &&
          word.hasAttribute(autobi.getHypothesizedFeature("pitch_accent_detection"))) {
        if (word.hasAttribute(autobi.getConfidenceFeature("pitch_accent_detection"))) {
          Double conf = (Double) word.getAttribute(autobi.getConfidenceFeature("pitch_accent_detection"));
          if (!word.getAttribute(autobi.getHypothesizedFeature("pitch_accent_detection")).equals("ACCENTED")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_pitch_accent", "ACCENTED: " + conf);
        } else {
          word.setAttribute("hyp_pitch_accent", word.getAttribute(autobi.getHypothesizedFeature(
              "pitch_accent_detection")));
        }
      }
      if (autobi.getTasks().containsKey("pitch_accent_classification") &&
          word.hasAttribute(autobi.getHypothesizedFeature("pitch_accent_classification"))) {
        if (!word.hasAttribute("hyp_pitch_accent") || word.getAttribute("hyp_pitch_accent").equals("ACCENTED")) {
          word.setAttribute("hyp_pitch_accent", word.getAttribute(autobi.getHypothesizedFeature(
              "pitch_accent_classification")));
        }
      }

      // Assigns phrase ending tones.
      if (autobi.getTasks().containsKey("intonational_phrase_boundary_detection") &&
          word.hasAttribute(autobi.getHypothesizedFeature("intonational_phrase_boundary_detection"))) {
        if (word.hasAttribute(autobi.getConfidenceFeature("intonational_phrase_boundary_detection"))) {
          Double conf =
              (Double) word.getAttribute(autobi.getConfidenceFeature("intonational_phrase_boundary_detection"));
          if (!word.getAttribute(autobi.getHypothesizedFeature("intonational_phrase_boundary_detection"))
              .equals("INTONATIONAL_BOUNDARY")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_phrase_boundary", "BOUNDARY: " + conf);
        } else {
          word.setAttribute("hyp_phrase_boundary",
              word.getAttribute(autobi.getHypothesizedFeature("intonational_phrase_boundary_detection")));
        }
      }

      if (autobi.getTasks().containsKey("intermediate_phrase_boundary_detection") &&
          word.hasAttribute(autobi.getHypothesizedFeature("intermediate_phrase_boundary_detection"))) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("NONBOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary",
              word.getAttribute(autobi.getHypothesizedFeature("intermediate_phrase_boundary_detection")));
        }
      }

      if (autobi.getTasks().containsKey("phrase_accent_boundary_tone_classification") &&
          word.hasAttribute(autobi.getHypothesizedFeature("phrase_accent_boundary_tone_classification"))) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTONATIONAL_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary",
              word.getAttribute(
                  autobi.getHypothesizedFeature("phrase_accent_boundary_tone_classification").replace("x", "%")));
        }
      }

      if (autobi.getTasks().containsKey("phrase_accent_classification") &&
          word.hasAttribute(autobi.getHypothesizedFeature("phrase_accent_classification"))) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTERMEDIATE_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary",
              word.getAttribute(autobi.getHypothesizedFeature("phrase_accent_classification")));
        }
      }
    }
  }

  /**
   * Constructs a hash of AuToBITasks associating a task identifier from AuToBIParameters with an AuToBITask describing
   * the necessary FeatureSet and AuToBIClassifier to use in training a classifier for this task.
   * <p/>
   * The parameters are:
   * <p/>
   * pitch_accent_detector
   * <p/>
   * pitch_accent_classifier
   * <p/>
   * intonational_phrase_detector
   * <p/>
   * intermediate_phrase_detector
   * <p/>
   * phrase_accent_classifier
   * <p/>
   * phrase_accent_boundary_tone_classifier
   *
   * @param params     AuToBIParameter that hold the parameters for the current execution of AuToBI
   * @param serialized if true, try to read serialized classifiers, else, initialize default classifiers
   * @return a hash associating parameters with AuToBITask objects
   */
  public static HashMap<String, AuToBITask> createTaskListFromParameters(AuToBIParameters params,
                                                                         boolean serialized) {
    HashMap<String, AuToBITask> map = new HashMap<String, AuToBITask>();

    try {
      if (params.hasParameter("pitch_accent_detector")) {
        map.put("pitch_accent_detection",
            getPitchAccentDetectionTask(serialized ? params.getParameter("pitch_accent_detector") : null));
        params.setParameter("pitch_accent_detection",
            params.getParameter("pitch_accent_detector"));
      }
      if (params.hasParameter("pitch_accent_classifier")) {
        map.put("pitch_accent_classification",
            getPitchAccentClassificationTask(serialized ? params.getParameter("pitch_accent_classifier") : null));
        params.setParameter("pitch_accent_classification",
            params.getParameter("pitch_accent_classifier"));
      }
      if (params.hasParameter("intonational_phrase_boundary_detector")) {
        map.put("intonational_phrase_boundary_detection", getIntonationalPhraseDetectionTask(
            serialized ? params.getParameter("intonational_phrase_boundary_detector") : null));
        params.setParameter("intonational_phrase_boundary_detection",
            params.getParameter("intonational_phrase_boundary_detector"));

        // Use rhapsodie specific feature set.
        if (params.booleanParameter("rhapsodie", false)) {
          map.get("intonational_phrase_boundary_detection")
              .setFeatureSet(new RhapIntonationalPhraseBoundaryDetectionFeatureSet());
        }
      }
      if (params.hasParameter("intermediate_phrase_boundary_detector")) {
        map.put("intermediate_phrase_boundary_detection", getIntermediatePhraseDetectionTask(
            serialized ? params.getParameter("intermediate_phrase_boundary_detector") : null));
        params.setParameter("intermediate_phrase_boundary_detection",
            params.getParameter("intermediate_phrase_boundary_detector"));
      }
      if (params.hasParameter("phrase_accent_classifier")) {
        map.put("phrase_accent_classification",
            getPhraseAccentClassificationTask(serialized ? params.getParameter("phrase_accent_classifier") : null));
        params.setParameter("phrase_accent_classification",
            params.getParameter("phrase_accent_classifier"));
      }
      if (params.hasParameter("phrase_accent_boundary_tone_classifier")) {
        map.put("phrase_accent_boundary_tone_classification", getPABTClassificationTask(
            serialized ? params.getParameter("phrase_accent_boundary_tone_classifier") : null));
        params.setParameter("phrase_accent_boundary_tone_classification",
            params.getParameter("phrase_accent_boundary_tone_classifier"));
      }

    } catch (AuToBIException e) {
      AuToBIUtils.error("Unexpected Exception thrown: " + e.getMessage());
    }

    return map;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for pitch accent detection.
   *
   * @param filename a serialized filename.  If null, construct a default classifier.
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPitchAccentDetectionTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // Formerly class weighted weka logistic.
      task.setClassifier(new LibLinearClassifier(true));
    }
    String hyp = "hyp_pitch_accent_location";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_PitchAccent");
    task.setFeatureSet(new PitchAccentDetectionFeatureSet());
    return task;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for pitch accent classification.
   *
   * @param filename the filename to read a serialized classifier from. if null a default untrained classifier is
   *                 allocated
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPitchAccentClassificationTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // Formerly class weighted weka Ada Boost.
      task.setClassifier(new LibLinearClassifier(SolverType.L2R_LR, false));
    }
    String hyp = "hyp_pitch_accent_type";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_PitchAccentType");
    task.setFeatureSet(new PitchAccentClassificationFeatureSet());
    return task;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for intonational phrase boundary detection.
   *
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getIntonationalPhraseDetectionTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // Formerly weka logistic
      task.setClassifier(new LibLinearClassifier());
    }
    String hyp = "hyp_intonational_phrase_boundary";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_IntonationalPhraseBoundary");
    task.setFeatureSet(new IntonationalPhraseBoundaryDetectionFeatureSet());
    return task;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for intermediate phrase boundary detection.
   *
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getIntermediatePhraseDetectionTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // Formerly class weighted weka logistic
      task.setClassifier(new LibLinearClassifier());
    }
    String hyp = "hyp_intermediate_phrase_boundary";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_IntermediatePhraseBoundary");
    task.setFeatureSet(new IntermediatePhraseBoundaryDetectionFeatureSet());
    return task;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for phrase accent classification.
   *
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPhraseAccentClassificationTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // formerly weka class weighted random forest
      task.setClassifier(new LibLinearClassifier());
    }
    String hyp = "hyp_phrase_accent";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_PhraseAccent");
    task.setFeatureSet(new PhraseAccentClassificationFeatureSet());
    return task;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for classification of phrase accent boundary tone pairs.
   *
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPABTClassificationTask(String filename) {
    AuToBITask task = new AuToBITask();
    if (filename != null) {
      task.setClassifier(ClassifierUtils.readAuToBIClassifier(filename));
    } else {
      // formerly weka random forest
      task.setClassifier(new LibLinearClassifier());
    }
    String hyp = "hyp_phrase_accent_boundary_tone";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_PhraseAccentBoundaryTone");
    task.setFeatureSet(new PhraseAccentBoundaryToneClassificationFeatureSet());
    return task;
  }


  /**
   * Makes an AuToBI feature name from String parameters.
   * <p/>
   * This is a utility function to support loops over feature types and and names.
   * <p/>
   * makeFeatureName("f0") = "f0"
   * makeFeatureName("log", "f0") = "log[f0]"
   * makeFeatureName("minus", "A", "B") = "minus[A,B]"
   *
   * @param params the parameters
   * @return an AuToBI formatted set of feature names.
   */
  public static String makeFeatureName(String... params) {
    if (params.length == 0) {
      return "";
    }
    if (params.length == 1) {
      return params[0];
    }

    // This allows the coherence of makeFeatureName("log", "f0) and makeFeatureName("", "f0")
    if (params.length == 2 && params[0].isEmpty()) {
      return params[1];
    }

    StringBuilder sb = new StringBuilder(params[0]);
    sb.append("[");
    for (int i = 1; i < params.length; ++i) {
      if (i > 1) {
        sb.append(',');
      }
      sb.append(params[i]);
    }
    sb.append("]");
    return sb.toString();
  }
}