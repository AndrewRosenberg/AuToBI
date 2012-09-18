/*  AuToBIUtils.java

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

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

import edu.cuny.qc.speech.AuToBI.classifier.ClassWeightedWekaClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.WekaClassifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.AuToBIParameters;
import edu.cuny.qc.speech.AuToBI.core.AuToBITask;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import org.apache.oro.io.GlobFilenameFilter;
import weka.classifiers.functions.Logistic;

import org.apache.log4j.Logger;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;

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
    if (pattern != null)
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
    return filenames;
  }

  /**
   * Globs a single file from a list of full path names that match the pattern.
   * <p/>
   * Throws an error if the pattern matches multiple files.
   *
   * @param pattern The file pattern.
   * @return the matching file
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          if the pattern matches multiple files
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
   * Constructs merged hypotheses for phrase ending tones and pitch accents by merging hypotheses from the six detection
   * and classification tasks.
   *
   * @param words the words to analyse
   */
  public static void mergeAuToBIHypotheses(List<Word> words) {

    for (Word word : words) {

      // Assigns pitch accents to words.  If only accent detection is available, a binary True/False hypothesis
      // will be assigned.  If location and type hypotheses are available, the hypothesized type will be assigned.
      // Finally, if only type information is available, every word will be assigned its best guess for accent type.
      if (word.hasAttribute("hyp_pitch_accent_location")) {
        if (word.hasAttribute("hyp_pitch_accent_location_conf")) {
          Double conf = (Double) word.getAttribute("hyp_pitch_accent_location_conf");
          if (!word.getAttribute("hyp_pitch_accent_location").equals("ACCENTED")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_pitch_accent", "ACCENTED: " + conf);
        } else {
          word.setAttribute("hyp_pitch_accent", word.getAttribute("hyp_pitch_accent_location"));
        }
      }
      if (word.hasAttribute("hyp_pitch_accent_type")) {
        if (!word.hasAttribute("hyp_pitch_accent") || word.getAttribute("hyp_pitch_accent").equals("ACCENTED")) {
          word.setAttribute("hyp_pitch_accent", word.getAttribute("hyp_pitch_accent_type"));
        }
      }

      // Assigns phrase ending tones.
      if (word.hasAttribute("hyp_IP_location")) {
        if (word.hasAttribute("hyp_IP_location_conf")) {
          Double conf = (Double) word.getAttribute("hyp_IP_location_conf");
          if (!word.getAttribute("hyp_IP_location").equals("INTONATIONAL_BOUNDARY")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_phrase_boundary", "BOUNDARY: " + conf);
        } else {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_IP_location"));
        }
      }

      if (word.hasAttribute("hyp_ip_location")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("NONBOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_ip_location"));
        }
      }

      if (word.hasAttribute("hyp_boundary_tone")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTONATIONAL_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_boundary_tone"));
        }
      }

      if (word.hasAttribute("hyp_phrase_accent")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTERMEDIATE_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_phrase_accent"));
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
   * @param params AuToBIParameter that hold the parameters for the current execution of AuToBI
   * @return a hash associating parameters with AuToBITask objects
   */
  public static HashMap<String, AuToBITask> createTaskListFromParameters(AuToBIParameters params) {
    HashMap<String, AuToBITask> map = new HashMap<String, AuToBITask>();

    if (params.hasParameter("pitch_accent_detector")) {
      map.put("pitch_accent_detector", getPitchAccentDetectionTask());
    }
    if (params.hasParameter("pitch_accent_classifier")) {
      map.put("pitch_accent_classifier", getPitchAccentClassificationTask());
    }
    if (params.hasParameter("intonational_phrase_detector")) {
      map.put("intonational_phrase_detector", getIntonationalPhraseDetectionTask());
    }
    if (params.hasParameter("intermediate_phrase_detector")) {
      map.put("intermediate_phrase_detector", getIntermediatePhraseDetectionTask());
    }
    if (params.hasParameter("phrase_accent_classifier")) {
      map.put("phrase_accent_classifier", getPhraseAccentClassificationTask());
    }
    if (params.hasParameter("phrase_accent_boundary_tone_classifier")) {
      map.put("phrase_accent_boundary_tone_classifier", getPABTClassificationTask());
    }

    return map;
  }

  /**
   * Gets an AuToBITask containing the standard configuration of featureset, classifier, and true and hypothesized
   * feature name for pitch accent detection.
   *
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPitchAccentDetectionTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new ClassWeightedWekaClassifier(new Logistic()));
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
   * @return an appropriate AuToBITask
   */
  public static AuToBITask getPitchAccentClassificationTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new ClassWeightedWekaClassifier(new AdaBoostM1()));
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
  public static AuToBITask getIntonationalPhraseDetectionTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new WekaClassifier(new Logistic()));
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
  public static AuToBITask getIntermediatePhraseDetectionTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new ClassWeightedWekaClassifier(new Logistic()));
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
  public static AuToBITask getPhraseAccentClassificationTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new ClassWeightedWekaClassifier(new RandomForest()));
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
  public static AuToBITask getPABTClassificationTask() {
    AuToBITask task = new AuToBITask();
    task.setClassifier(new WekaClassifier(new RandomForest()));
    String hyp = "hyp_phrase_accent_boundary_tone";
    task.setHypFeature(hyp);
    task.setConfFeature(hyp + "_conf");
    task.setDistFeature(hyp + "_dist");
    task.setTrueFeature("nominal_PhraseAccentBoundaryTone");
    task.setFeatureSet(new PhraseAccentBoundaryToneClassificationFeatureSet());
    return task;
  }
}