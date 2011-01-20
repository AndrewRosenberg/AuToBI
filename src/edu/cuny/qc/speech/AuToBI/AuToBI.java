/*  AuToBI.java

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

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.*;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import edu.cuny.qc.speech.AuToBI.io.AuToBIFileReader;
import edu.cuny.qc.speech.AuToBI.io.AuToBIFileWriter;
import edu.cuny.qc.speech.AuToBI.io.TextGridReader;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import edu.cuny.qc.speech.AuToBI.util.SubregionUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * This is the main class for the AuToBI system.
 * <p/>
 * AuToBI generates hypothesized ToBI tones based on manual segmentation of words and an audio file.
 * <p/>
 * The prediction is divided into six tasks. 1) detection of pitch accents 2) classification of pitch accent types 3)
 * detection of intonational phrase boundaries 4) classification of intonational phrase ending tones (phrase accent and
 * boundary tone pairs) 5) detection of intermediate phrase boundaries 6) classification of intermediate phrase ending
 * tones (phrase accents)
 * <p/>
 * Each of these tasks require distinct features to be extracted from the words that are being analyzed. To perform the
 * feature extraction, each task has an associated FeatureSet which describes the features required for classification.
 * AuToBI maintains a registry of FeatureExtractors describing the Features that they will calculate when processing a
 * word.  When extracting features for a FeatureSet, AuToBI only calls those FeatureExtractors necessary to generate the
 * required features.
 * <p/>
 * This class manages command line parameters, the execution of feature extractors and the generation of hypothesized
 * ToBI tones.
 *
 * @see FeatureSet
 * @see FeatureExtractor
 */
public class AuToBI {
  private AuToBIParameters params;  // Command line parameters

  // Task specific classifiers to generate hypothesized events
  private AuToBIClassifier pitch_accent_detector;
  private AuToBIClassifier intonational_phrase_boundary_detector;
  private AuToBIClassifier intermediate_phrase_boundary_detector;
  private AuToBIClassifier pitch_accent_classifier;
  private AuToBIClassifier phrase_accent_classifier;
  private AuToBIClassifier boundary_tone_classifier;

  // A map from feature names to the FeatureExtractors that generate them
  private Map<String, FeatureExtractor> feature_registry;

  // A set of FeatureExtractors that have already executed
  private Set<FeatureExtractor> executed_feature_extractors;

  // A map from input filenames to serialized speaker normalization parameter files.
  private Map<String, String> speaker_norm_file_mapping;

  // A map of the number of times each feature is needed.
  private HashMap<String, Integer> reference_count;

  // A set of features to delete on the next garbage collection call
  private Set<String> dead_features;

  /**
   * Constructs a new AuToBI object.
   */
  public AuToBI() {
    params = new AuToBIParameters();
    feature_registry = new HashMap<String, FeatureExtractor>();
    executed_feature_extractors = new HashSet<FeatureExtractor>();
    speaker_norm_file_mapping = new HashMap<String, String>();
  }

  /**
   * Parses command line parameters and sets up log4j logging.
   *
   * @param args command line arguments.
   */
  public void init(String[] args) {
    params.readParameters(args);
    if (params.hasParameter("log4j_config_file")) {
      try {
        PropertyConfigurator.configure(params.getParameter("log4j_config_file"));
      } catch (Exception e) {
        BasicConfigurator.configure();
        AuToBIUtils.log("Couldn't read -log4j_config_file. BasicConfigurator logging to console");
      }
    } else {
      BasicConfigurator.configure();
    }
  }

  /**
   * Gets the requested parameter.
   *
   * @param parameter the parameter name
   * @return the parameter value
   * @throws AuToBIException if the parameter was not set
   */
  public String getParameter(String parameter) throws AuToBIException {
    return params.getParameter(parameter);
  }

  /**
   * Gets the requested parameter.
   *
   * @param parameter the parameter name
   * @return the parameter value or null if the parameter was not set
   */
  public String getOptionalParameter(String parameter) {
    return params.getOptionalParameter(parameter);
  }

  /**
   * Gets the requested parameter with a default value if the parameter has not been set.
   *
   * @param parameter     the parameter name
   * @param default_value a default value
   * @return The parameter value or default_value if the parameter has not been set.
   */
  public String getOptionalParameter(String parameter, String default_value) {
    return params.getOptionalParameter(parameter, default_value);
  }

  /**
   * Determines if a parameter has been set or not.
   *
   * @param parameter the parameter name
   * @return true if the parameter has been set, false otherwise
   */
  public boolean hasParameter(String parameter) {
    return params.hasParameter(parameter);
  }

  /**
   * Registers a FeatureExtractor with AuToBI.
   * <p/>
   * A FeatureExtractor class is responsible for reporting what features it extracts.
   *
   * @param fe the feature extractor
   */
  public void registerFeatureExtractor(FeatureExtractor fe) {
    for (String feature_name : fe.getExtractedFeatures()) {
      feature_registry.put(feature_name, fe);
    }
  }

  /**
   * Clears the feature registry and the extracted features.
   */
  public void unregisterAllFeatureExtractors() {
    feature_registry = new HashMap<String, FeatureExtractor>();
    executed_feature_extractors = new HashSet<FeatureExtractor>();
  }

  /**
   * Extracts the features required for the feature set and optionally deletes intermediate features that may have been
   * generated in their processing
   *
   * @param fs the feature set
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeatures(FeatureSet fs) throws FeatureExtractorException, AuToBIException {
    initializeReferenceCounting(fs);
    extractFeature(fs.getClassAttribute(), fs);
    extractFeatures(fs.getRequiredFeatures(), fs);
  }

  /**
   * Extracts a set of features on data points stored in the given feature set
   *
   * @param features The requested features
   * @param fs       The feature set
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeatures(Set<String> features, FeatureSet fs)
      throws FeatureExtractorException, AuToBIException {
    for (String feature : features) {
      extractFeature(feature, fs);
    }
  }

  /**
   * Initializes the number of times a feature is required by a FeatureSet. This allows AuToBI to guarantee that every
   * stored feature is necessary, deleting unneeded intermediate features.
   * <p/>
   * After each feature is extracted its reference count is decremented.  When a feature has a reference count of zero,
   * it can safely be removed.
   *
   * @param fs the feature set
   * @throws AuToBIException if there are features required that do not have associated registered feature extractors.
   */
  public void initializeReferenceCounting(FeatureSet fs) throws AuToBIException {
    reference_count = new HashMap<String, Integer>();
    dead_features = new HashSet<String>();

    Stack<String> features = new Stack<String>();
    if (fs.getClassAttribute() != null)
      features.add(fs.getClassAttribute());
    features.addAll(fs.getRequiredFeatures());

    while (features.size() != 0) {
      String feature = features.pop();
      if (!feature_registry.containsKey(feature)) {
        throw new AuToBIException("No feature extractor registered for feature: " + feature);
      }

      incrementReferenceCount(feature);

      // Add required features that wouldn't have been extracted previously.
      if (feature_registry.get(feature) != null) {
        features.addAll(feature_registry.get(feature).getRequiredFeatures());
      }
    }
  }

  /**
   * Retrieves the remaining reference count for a given feature.
   *
   * @param feature the feature
   * @return the current reference count for the feature
   */
  public int getReferenceCount(String feature) {
    if (reference_count.containsKey(feature)) {
      return reference_count.get(feature);
    } else {
      return 0;
    }
  }

  /**
   * Increments the reference count for a given feature.
   *
   * @param feature the feature name
   */
  public void incrementReferenceCount(String feature) {
    if (!reference_count.containsKey(feature)) {
      reference_count.put(feature, 0);
    }
    reference_count.put(feature, reference_count.get(feature) + 1);

    // It is unlikely that a feature would get a new reference after being obliterated, but this guarantees
    // that there are no features with positive reference counts in the dead feature set.
    if (dead_features.contains(feature)) {
      dead_features.remove(feature);
    }
  }

  /**
   * Decrement the reference count for a given feature.
   *
   * @param feature the feature name
   */
  public void decrementReferenceCount(String feature) {
    if (reference_count.containsKey(feature)) {
      reference_count.put(feature, Math.max(0, reference_count.get(feature) - 1));
      if (reference_count.get(feature) == 0) {
        dead_features.add(feature);
      }
    }
  }

  /**
   * Extracts a single feature on data points stored in the given feature set.
   * <p/>
   * If the registered FeatureExtractor requires any features for processing, this will result in recursive calls to
   * extractFeatures(...).
   *
   * @param feature The requested feature
   * @param fs      The feature set
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeature(String feature, FeatureSet fs) throws FeatureExtractorException, AuToBIException {
    if (!feature_registry.containsKey(feature))
      throw new AuToBIException("No feature extractor registered for feature: " + feature);
    FeatureExtractor extractor = feature_registry.get(feature);
    AuToBIUtils.debug("Start Feature Extraction for: " + feature);
    if (extractor != null) {
      extractFeatures(extractor.getRequiredFeatures(), fs);

      if (!executed_feature_extractors.contains(extractor)) {
        AuToBIUtils.debug("running feature extraction for: " + feature);
        extractor.extractFeatures(fs.getDataPoints());
        AuToBIUtils.debug("extracted features using: " + extractor.getClass().getCanonicalName());
        executed_feature_extractors.add(extractor);
      }

      for (String rf : extractor.getRequiredFeatures()) {
        decrementReferenceCount(rf);
      }
      featureGarbageCollection(fs);
    }
    AuToBIUtils.debug("End Feature Extraction for: " + feature);
  }

  /**
   * Removes any features which are no longer referenced in the feature set.
   *
   * @param fs the feature set
   */
  public void featureGarbageCollection(FeatureSet fs) {
    for (String feature : dead_features) {
      AuToBIUtils.debug("Removing feature: " + feature);
      fs.removeFeature(feature);
    }
    dead_features.clear();
  }

  /**
   * Evaluates the performance of a particular task.
   *
   * @param task a task identifier
   * @param fs   the feature set
   * @return a string representation of the evaluation
   * @throws AuToBIException if there is no task corresponding to the identifier
   */
  private String evaluateTaskPerformance(String task, FeatureSet fs) throws AuToBIException {
    if (task.equals("pitch_accent_detection"))
      return ClassifierUtils.evaluateClassification("hyp_pitch_accent_location", "nominal_PitchAccent", fs);
    else if (task.equals("pitch_accent_classification"))
      return ClassifierUtils.evaluateClassification("hyp_pitch_accent_type", "nominal_PitchAccentType", fs);
    else if (task.equals("intonational_phrase_boundary_detection"))
      return ClassifierUtils.evaluateClassification("hyp_IP_location", "nominal_IntonationalPhraseBoundary", fs);
    else if (task.equals("intermediate_phrase_boundary_detection"))
      return ClassifierUtils.evaluateClassification("hyp_ip_location", "nominal_IntermediatePhraseBoundary", fs);
    else if (task.equals("boundary_tone_classification"))
      return ClassifierUtils.evaluateClassification("hyp_boundary_tone", "nominal_PhraseAccentBoundaryTone", fs);
    else if (task.equals("phrase_accent_classification"))
      return ClassifierUtils.evaluateClassification("hyp_phrase_accent", "nominal_PhraseAccentType", fs);
    else throw new AuToBIException("Undefined task: " + task);
  }

  /**
   * Generates predictions corresponding to a particular task
   *
   * @param task A task identifier
   * @param fs   The feature set
   * @throws AuToBIException if there is no task associated with the identifier.
   */
  public void generatePredictions(String task, FeatureSet fs) throws AuToBIException {
    if (task.equals("pitch_accent_detection"))
      ClassifierUtils.generatePredictions(pitch_accent_detector, "hyp_pitch_accent_location", "", fs);
    else if (task.equals("pitch_accent_classification"))
      ClassifierUtils.generatePredictions(pitch_accent_classifier, "hyp_pitch_accent_type", "", fs);
    else if (task.equals("intonational_phrase_boundary_detection"))
      ClassifierUtils.generatePredictions(intonational_phrase_boundary_detector, "hyp_IP_location", "", fs);
    else if (task.equals("intermediate_phrase_boundary_detection"))
      ClassifierUtils.generatePredictions(intermediate_phrase_boundary_detector, "hyp_ip_location", "", fs);
    else if (task.equals("boundary_tone_classification"))
      ClassifierUtils.generatePredictions(boundary_tone_classifier, "hyp_boundary_tone", "", fs);
    else if (task.equals("phrase_accent_classification"))
      ClassifierUtils.generatePredictions(phrase_accent_classifier, "hyp_phrase_accent", "", fs);
    else throw new AuToBIException("Undefined task: " + task);
  }

  /**
   * Generates predictions with confidence scores corresponding to a particular task
   *
   * @param task A task identifier
   * @param fs   The feature set
   * @throws AuToBIException if there is no task associated with the identifier.
   */
  public void generatePredictionsWithConfidenceScores(String task, FeatureSet fs) throws AuToBIException {
    if (task.equals("pitch_accent_detection"))
      ClassifierUtils.generatePredictionsWithConfidenceScores(pitch_accent_detector, "hyp_pitch_accent_location",
          "hyp_pitch_accent_location_conf", "", fs);
    else if (task.equals("pitch_accent_classification"))
      ClassifierUtils.generatePredictionsWithConfidenceScores(pitch_accent_classifier, "hyp_pitch_accent_type",
          "hyp_pitch_accent_type_conf", "", fs);
    else if (task.equals("intonational_phrase_boundary_detection"))
      ClassifierUtils
          .generatePredictionsWithConfidenceScores(intonational_phrase_boundary_detector, "hyp_IP_location",
              "hyp_IP_location_conf", "", fs);
    else if (task.equals("intermediate_phrase_boundary_detection"))
      ClassifierUtils
          .generatePredictionsWithConfidenceScores(intermediate_phrase_boundary_detector, "hyp_ip_location",
              "hyp_ip_location_conf", "", fs);
    else if (task.equals("boundary_tone_classification"))
      ClassifierUtils.generatePredictionsWithConfidenceScores(boundary_tone_classifier, "hyp_boundary_tone",
          "hyp_boundary_tone_conf", "", fs);
    else if (task.equals("phrase_accent_classification"))
      ClassifierUtils.generatePredictionsWithConfidenceScores(phrase_accent_classifier, "hyp_phrase_accent",
          "hyp_phrase_accent_conf", "", fs);
    else throw new AuToBIException("Undefined task: " + task);
  }

  /**
   * Retrieves an empty feature set for the given task.
   *
   * @param task a task identifier.
   * @return a corresponding FeatureSet object
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  public FeatureSet getTaskFeatureSet(String task) throws AuToBIException {
    if (task.equals("pitch_accent_detection"))
      return new PitchAccentDetectionFeatureSet();
    if (task.equals("pitch_accent_classification"))
      return new PitchAccentClassificationFeatureSet();
    if (task.equals("intonational_phrase_boundary_detection"))
      return new IntonationalPhraseBoundaryDetectionFeatureSet();
    if (task.equals("intermediate_phrase_boundary_detection"))
      return new IntermediatePhraseBoundaryDetectionFeatureSet();
    if (task.equals("boundary_tone_classification"))
      return new PhraseAccentBoundaryToneClassificationFeatureSet();
    if (task.equals("phrase_accent_classification"))
      return new PhraseAccentClassificationFeatureSet();
    throw new AuToBIException("No defined feature set for task: " + task);
  }

  /**
   * Loads classifiers from serialized objects.
   * <p/>
   * Only those classifiers which have been specified using the following command line parameters are loaded:
   * -pitch_accent_detector -pitch_accent_classifier -IP_detector -ip_detector -phrase_accent_classifier
   * -boundary_tone_classifier
   */
  public void loadClassifiers() {
    try {
      String pad_filename = getParameter("pitch_accent_detector");
      pitch_accent_detector = ClassifierUtils.readAuToBIClassifier(pad_filename);
    } catch (AuToBIException ignored) {
    }

    try {
      String pac_filename = getParameter("pitch_accent_classifier");
      pitch_accent_classifier = ClassifierUtils.readAuToBIClassifier(pac_filename);
    } catch (AuToBIException ignored) {
    }

    try {
      String intonational_phrase_detector_filename = getParameter("IP_detector");
      intonational_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intonational_phrase_detector_filename);
    } catch (AuToBIException ignored) {
    }

    try {
      String intermediate_phrase_detector_filename = getParameter("ip_detector");
      intermediate_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intermediate_phrase_detector_filename);
    } catch (AuToBIException ignored) {
    }

    try {
      String phrase_accent_classifier_name = getParameter("phrase_accent_classifier");
      phrase_accent_classifier =
          ClassifierUtils.readAuToBIClassifier(phrase_accent_classifier_name);
    } catch (AuToBIException ignored) {
    }

    try {
      String boundary_tone_classifier_name = getParameter("boundary_tone_classifier");
      boundary_tone_classifier =
          ClassifierUtils.readAuToBIClassifier(boundary_tone_classifier_name);
    } catch (AuToBIException ignored) {
    }
  }

  /**
   * Retrieves a list of classification task identifiers corresponding to the tasks to be performed.
   * <p/>
   * Only those tasks corresponding to loaded classifiers are executed.
   *
   * @return a list of task identifiers.
   */
  public List<String> getClassificationTasks() {
    ArrayList<String> classificationTasks = new ArrayList<String>();

    if (pitch_accent_detector != null)
      classificationTasks.add("pitch_accent_detection");
    if (pitch_accent_classifier != null)
      classificationTasks.add("pitch_accent_classification");
    if (intonational_phrase_boundary_detector != null)
      classificationTasks.add("intonational_phrase_boundary_detection");
    if (intermediate_phrase_boundary_detector != null)
      classificationTasks.add("intermediate_phrase_boundary_detection");
    if (boundary_tone_classifier != null)
      classificationTasks.add("boundary_tone_classification");
    if (phrase_accent_classifier != null)
      classificationTasks.add("phrase_accent_classification");

    return classificationTasks;
  }

  /**
   * Writes a TextGrid file containing words and hypothesized ToBI labels.
   *
   * @param words    The words
   * @param out_file The destination file
   * @throws IOException If there is a problem writing to the destination file.
   */
  public void writeTextGrid(List<Word> words, String out_file) throws IOException {
    String text_grid = "File type = \"ooTextFile\"\n";
    text_grid += "Object class = \"TextGrid\"\n";
    text_grid += "xmin = 0\n";
    text_grid += "xmax = " + words.get(words.size() - 1).getEnd() + "\n";
    text_grid += "tiers? <exists>\n";
    text_grid += "size = 3\n";
    text_grid += "item []:\n";
    text_grid += "item [1]:\n";
    text_grid += "class = \"IntervalTier\"\n";
    text_grid += "name = \"words\"\n";
    text_grid += "xmin = 0\n";
    text_grid += "xmax = " + words.get(words.size() - 1).getEnd() + "\n";
    text_grid += "intervals: size = " + words.size() + "\n";
    for (int i = 0; i < words.size(); ++i) {
      Word w = words.get(i);
      text_grid += "intervals [" + i + "]:\n";
      text_grid += "xmin = " + w.getStart() + "\n";
      text_grid += "xmax = " + w.getEnd() + "\n";
      text_grid += "text = \"" + w.getLabel() + "\"\n";
    }


    text_grid += "item [2]:\n";
    text_grid += "class = \"IntervalTier\"\n";
    text_grid += "name = \"pitch_accent_hypothesis\"\n";
    text_grid += "xmin = 0\n";
    text_grid += "xmax = " + words.get(words.size() - 1).getEnd() + "\n";
    text_grid += "intervals: size = " + words.size() + "\n";
    for (int i = 0; i < words.size(); ++i) {
      Word w = words.get(i);
      String text = "";
      if (w.hasAttribute("hyp_pitch_accent")) {
        text = w.getAttribute("hyp_pitch_accent").toString();
      }

      text_grid += "intervals [" + i + "]:\n";
      text_grid += "xmin = " + w.getStart() + "\n";
      text_grid += "xmax = " + w.getEnd() + "\n";
      text_grid += "text = \"" + text + "\"\n";
    }

    text_grid += "item [3]:\n";
    text_grid += "class = \"IntervalTier\"\n";
    text_grid += "name = \"phrase_hypothesis\"\n";
    text_grid += "xmin = 0\n";
    text_grid += "xmax = " + words.get(words.size() - 1).getEnd() + "\n";
    text_grid += "intervals: size = " + words.size() + "\n";
    for (int i = 0; i < words.size(); ++i) {
      Word w = words.get(i);

      String text = "";
      if (w.hasAttribute("hyp_phrase_boundary")) {
        text = w.getAttribute("hyp_phrase_boundary").toString();
      }

      text_grid += "intervals [" + i + "]:\n";
      text_grid += "xmin = " + w.getStart() + "\n";
      text_grid += "xmax = " + w.getEnd() + "\n";
      text_grid += "text = \"" + text + "\"\n";
    }

    AuToBIFileWriter writer = new AuToBIFileWriter(out_file);
    writer.write(text_grid);
    writer.close();
  }

  /**
   * Registers a large default set of feature extractors.
   *
   * @param wav_data The wave data
   * @throws FeatureExtractorException If there is a problem registering (not running) feature extractors.
   */
  public void registerAllFeatureExtractors(WavData wav_data)
      throws FeatureExtractorException {

    registerFeatureExtractor(new PitchAccentFeatureExtractor("nominal_PitchAccent"));
    registerFeatureExtractor(new PitchAccentTypeFeatureExtractor("nominal_PitchAccentType"));
    registerFeatureExtractor(new PhraseAccentFeatureExtractor("nominal_PhraseAccentType"));
    registerFeatureExtractor(new PhraseAccentBoundaryToneFeatureExtractor("nominal_PhraseAccentBoundaryTone"));
    registerFeatureExtractor(new IntonationalPhraseBoundaryFeatureExtractor("nominal_IntonationalPhraseBoundary"));
    registerFeatureExtractor(new IntermediatePhraseBoundaryFeatureExtractor("nominal_IntermediatePhraseBoundary"));

    registerFeatureExtractor(new PitchFeatureExtractor(wav_data, "f0"));
    registerFeatureExtractor(new LogContourFeatureExtractor("f0", "log_f0"));
    registerFeatureExtractor(new IntensityFeatureExtractor(wav_data, "I"));
    registerFeatureExtractor(new NormalizedContourFeatureExtractor("f0", "normalization_parameters"));
    registerFeatureExtractor(new NormalizedContourFeatureExtractor("log_f0", "normalization_parameters"));
    registerFeatureExtractor(new NormalizedContourFeatureExtractor("I", "normalization_parameters"));

    registerFeatureExtractor(new DeltaContourFeatureExtractor("f0"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("log_f0"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("I"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("norm_f0"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("norm_log_f0"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("norm_I"));

    registerFeatureExtractor(new ContourFeatureExtractor("f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("norm_f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_norm_f0"));

    registerFeatureExtractor(new ContourFeatureExtractor("log_f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("norm_log_f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_log_f0"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_norm_log_f0"));

    registerFeatureExtractor(new ContourFeatureExtractor("I"));
    registerFeatureExtractor(new ContourFeatureExtractor("norm_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_norm_I"));

    registerFeatureExtractor(new SpectrumFeatureExtractor(wav_data, "spectrum"));

    for (int low = 0; low <= 19; ++low) {
      for (int high = low + 1; high <= 20; ++high) {
        registerFeatureExtractor(new SpectralTiltFeatureExtractor("bark_tilt", "spectrum", low, high));
        registerFeatureExtractor(new SpectrumBandFeatureExtractor("bark", "spectrum", low, high));
      }
    }
    registerFeatureExtractor(new DurationFeatureExtractor());

    ////////////////////
    // Reset Features //
    ////////////////////
    registerFeatureExtractor(new ResetContourFeatureExtractor("f0", null));
    registerFeatureExtractor(new ResetContourFeatureExtractor("log_f0", null));
    registerFeatureExtractor(new ResetContourFeatureExtractor("I", null));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_f0", null));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_log_f0", null));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_I", null));

    registerFeatureExtractor(new SubregionResetFeatureExtractor("200ms"));
    registerFeatureExtractor(new SubregionResetFeatureExtractor("400ms"));

    registerFeatureExtractor(new ResetContourFeatureExtractor("f0", "200ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("log_f0", "200ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("I", "200ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_f0", "200ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_log_f0", "200ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_I", "200ms"));

    registerFeatureExtractor(new ResetContourFeatureExtractor("f0", "400ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("log_f0", "400ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("I", "400ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_f0", "400ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_log_f0", "400ms"));
    registerFeatureExtractor(new ResetContourFeatureExtractor("norm_I", "400ms"));

    ////////////////////////
    // Subregion Features //
    ////////////////////////
    registerFeatureExtractor(new PseudosyllableFeatureExtractor("pseudosyllable", wav_data));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_f0", "pseudosyllable"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("log_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_log_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_log_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_log_f0", "pseudosyllable"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_I", "pseudosyllable"));

    registerFeatureExtractor(new SubregionFeatureExtractor("200ms"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_f0", "200ms"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("log_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_log_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_log_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_log_f0", "200ms"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_I", "200ms"));

    List<String> difference_features = new ArrayList<String>();
    difference_features.add("duration__duration");
    for (String acoustic : new String[]{"f0", "log_f0", "I"}) {
      for (String norm : new String[]{"", "norm_"}) {
        for (String slope : new String[]{"", "delta_"}) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            difference_features.add(slope + norm + acoustic + "__" + agg);
          }
        }
      }
    }
    registerFeatureExtractor(new DifferenceFeatureExtractor(difference_features));

    try {
      if (hasParameter("spectral_pitch_accent_detector_collection")) {
        String pad_filename = getParameter("spectral_pitch_accent_detector_collection");
        PitchAccentDetectionClassifierCollection pacc;

        // Load PitchAccentDetectionClassifierCollection
        FileInputStream fis;
        ObjectInputStream in;
        fis = new FileInputStream(pad_filename);
        in = new ObjectInputStream(fis);
        Object o = in.readObject();
        if (o instanceof PitchAccentDetectionClassifierCollection) {
          pacc = (PitchAccentDetectionClassifierCollection) o;
        } else {
          throw new FeatureExtractorException(
              "Object read from -spectral_pitch_accent_detector_collection=" + pad_filename +
                  " is not a valid PitchAccentDetectionClassifierCollection");
        }

        // Register appropriate feature extractors for each classifier in the collection
        Integer high_bark = Integer.parseInt(getOptionalParameter("high_bark", "20"));
        for (int low = 0; low < high_bark; ++low) {
          for (int high = low + 1; high <= high_bark; ++high) {
            registerFeatureExtractor(
                new SpectrumPADFeatureExtractor(low, high, pacc.getPitchAccentDetector(low, high), this));
            registerFeatureExtractor(
                new CorrectionSpectrumPADFeatureExtractor(low, high, pacc.getCorrectionClassifier(low, high), this));
          }
        }
      }

    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new FeatureExtractorException(e.getMessage());
    } catch (IOException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
  }

  /**
   * Reads a speaker normalization mapping file.
   * <p/>
   * The file is a comma separated value text file containing pairs of textgrid filenames and their associated speaker
   * norm parameter filenames.
   * <p/>
   * Note: the filenames must be absolute filenames
   *
   * @param speaker_normalization_file The speaker normalization file
   * @throws IOException     If there is a problem reading the file
   * @throws AuToBIException If there is a problem with the formatting of the file
   */
  public void loadSpeakerNormalizationMapping(String speaker_normalization_file) throws IOException, AuToBIException {
    AuToBIFileReader reader = new AuToBIFileReader(speaker_normalization_file);

    String line;
    while ((line = reader.readLine()) != null) {
      String[] fields = line.split(",");
      if (fields.length != 2)
        throw new AuToBIException("Malformed speaker normalization mapping file: " + speaker_normalization_file + "(" +
            reader.getLineNumber() + ") : " + line);
      speaker_norm_file_mapping.put(fields[0], fields[1]);
    }
  }

  /**
   * Retrieves a stored speaker normalization parameter filename for a textgrid filename
   *
   * @param filename The textgrid filename
   * @return The associated speaker normalization parameter filename
   */
  public String getSpeakerNormParamFilename(String filename) {
    return speaker_norm_file_mapping.get(filename);
  }

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      String wav_filename = autobi.getParameter("wav_file");
      String tg_filename = autobi.getParameter("text_grid_file");
      String norm_param_filename = autobi.getOptionalParameter("normalization_parameters");
      WavReader reader = new WavReader();
      TextGridReader tg_reader = new TextGridReader(tg_filename, autobi.getOptionalParameter("words_tier_name"),
          autobi.getOptionalParameter("tones_tier_name"), autobi.getOptionalParameter("breaks_tier_name"));

      if (autobi.hasParameter("silence_regex")) {
        tg_reader.setSilenceRegex(autobi.getParameter("silence_regex"));
      }

      WavData wav = reader.read(wav_filename);
      PitchExtractor pitch_extractor = new PitchExtractor(wav);
      IntensityExtractor intensity_extractor = new IntensityExtractor(wav);
      SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);


      AuToBIUtils.log("Reading words from: " + tg_filename);
      List<Word> words = tg_reader.readWords();

      AuToBIUtils.log("Extracting acoustic information.");
      Contour pitch_values = pitch_extractor.soundToPitch();
      AuToBIUtils.debug("Extracted Pitch");
      Contour intensity_values = intensity_extractor.soundToIntensity();
      AuToBIUtils.debug("Extracted Intensity");

      /** TODO move this normalization parameter generation or loading switch into a feature extractor **/
      SpeakerNormalizationParameter norm_params = null;

      if (norm_param_filename != null) {
        norm_params =
            SpeakerNormalizationParameterGenerator.readSerializedParameters(norm_param_filename);
      }

      // If stored normalization data is unavailable generate normalization data from the input file.
      if (norm_params == null) {
        norm_params = new SpeakerNormalizationParameter();

        norm_params.insertPitch(pitch_values);
        norm_params.insertIntensity(intensity_values);
      }

      for (Region r : words) {
        r.setAttribute("normalization_parameters", norm_params);
      }

      autobi.loadClassifiers();

      AuToBIUtils.log("Registering Feature Extractors");
      autobi.registerAllFeatureExtractors(wav);
      autobi.registerNullFeatureExtractor("speaker_id");
      autobi.registerNullFeatureExtractor("normalization_parameters");

      for (String task : autobi.getClassificationTasks()) {
        AuToBIUtils.log("Running Hypothesis task -- " + task);

        FeatureSet fs = autobi.getTaskFeatureSet(task);
        fs.setDataPoints(words);
        autobi.extractFeatures(fs);

        fs.constructFeatures();

        autobi.generatePredictions(task, fs);
        AuToBIUtils.info(autobi.evaluateTaskPerformance(task, fs));

        if (autobi.hasParameter("arff_file")) {
          fs.writeArff(autobi.getParameter("arff_file"), "test");
        }
      }

      if (autobi.hasParameter("out_file")) {
        AuToBIUtils.mergeAuToBIHypotheses(words);
        String hypothesis_file = autobi.getParameter("out_file");
        AuToBIUtils.info("Writing hypotheses to " + hypothesis_file);
        autobi.writeTextGrid(words, hypothesis_file);
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  /**
   * Registers a null feature extractor with the registry.
   * <p/>
   * This implies that the feature will be manually set by the user outside the typical feature extraction process.
   * <p/>
   * This is used to satisfy feature requirements for feature extractors without writing a feature extractor for the
   * requirement.  This is often used for features that are assigned by a file reader.
   *
   * @param s the feature name
   */
  public void registerNullFeatureExtractor(String s) {
    this.feature_registry.put(s, null);
  }
}
