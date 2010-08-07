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

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Driving class for the AuToBI system.
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
 * <p/>
 * Note: The way feature extraction handles intermediate or extraneous features that are not required by a FeatureSet
 * needs revision.  In the next revision, I'll move to a reference counting approach.  At feature extraction, determine
 * how many components (the FeatureSet and FeatureExtractors) require a feature.  After running each feature extractor,
 * decrement the reference count of its required features.  If any features have a reference count of zero, remove them
 * from the data points.
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

  // A feature registry for Extractors that need to be run after all other FeatureExtractors
  @Deprecated
  private Map<String, FeatureExtractor> deferred_feature_registry;

  // A set of FeatureExtractors that have already executed
  private Set<FeatureExtractor> executed_feature_extractors;

  // A map from input filenames to serialized speaker normalization parameter files.
  private Map<String, String> speaker_norm_file_mapping;


  /**
   * Constructs a new AuToBI object.
   */
  public AuToBI() {
    params = new AuToBIParameters();
    feature_registry = new HashMap<String, FeatureExtractor>();
    deferred_feature_registry = new HashMap<String, FeatureExtractor>();
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
   * Registers a deferred FeatureExtractor with AuToBI.
   * <p/>
   * Deferred FeatureExtractors are run after all other FeatureExtractors
   * <p/>
   * A FeatureExtractor class is responsible for reporting what features it extracts.
   *
   * @param fe the feature extractor
   */
  @Deprecated
  public void registerDeferredFeatureExtractor(FeatureExtractor fe) {
    for (String feature_name : fe.getExtractedFeatures()) {
      deferred_feature_registry.put(feature_name, fe);
    }
  }

  /**
   * Extracts the features required for the feature set.
   *
   * @param fs the feature set
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeatures(FeatureSet fs) throws FeatureExtractorException, AuToBIException {
    extractFeatures(fs, true);
  }

  /**
   * Extracts the features required for the feature set and optionally deletes intermediate features that may have been
   * generated in their processing
   *
   * @param fs             the feature set
   * @param clear_features should unrequired features be deleted after extraction
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeatures(FeatureSet fs, boolean clear_features) throws FeatureExtractorException, AuToBIException {
    extractFeature(fs.class_attribute, fs, clear_features);
    extractFeatures(fs.getRequiredFeatures(), fs, clear_features);
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
    extractFeatures(features, fs, true);
  }

  /**
   * Extracts a set of features on data points stored in the given feature set
   *
   * @param features       The requested features
   * @param fs             The feature set
   * @param clear_features should unrequired features be deleted after extraction
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeatures(Set<String> features, FeatureSet fs, boolean clear_features)
      throws FeatureExtractorException, AuToBIException {
    for (String feature : features) {
      extractFeature(feature, fs, clear_features);
    }
  }

  /**
   * Extracts a single feature on data points stored in the given feature set.
   *
   * @param feature The requested feature
   * @param fs      The feature set
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeature(String feature, FeatureSet fs)
      throws FeatureExtractorException, AuToBIException {
    extractFeature(feature, fs, true);
  }

  /**
   * Extracts a single feature on data points stored in the given feature set.
   * <p/>
   * If the registered FeatureExtractor requires any features for processing, this will result in recursive calls to
   * extractFeatures(...).
   *
   * @param feature        The requested feature
   * @param fs             The feature set
   * @param clear_features should unrequired features be deleted after extraction
   * @throws FeatureExtractorException If any of the FeatureExtractors have a problem
   * @throws AuToBIException           If there are other problems
   */
  public void extractFeature(String feature, FeatureSet fs, boolean clear_features)
      throws FeatureExtractorException, AuToBIException {
    if (!feature_registry.containsKey(feature) && !deferred_feature_registry.containsKey(feature))
      throw new AuToBIException("No feature extractor registered for feature: " + feature);
    FeatureExtractor extractor = feature_registry.get(feature);
    if (!executed_feature_extractors.contains(extractor) && extractor != null) {
      executed_feature_extractors.add(extractor);
      AuToBIUtils.debug("running feature extraction for: " + feature);
      fs.addIntermediateFeatures(extractor.getRequiredFeatures());
      extractFeatures(extractor.getRequiredFeatures(), fs, false);
      extractor.extractFeatures(fs.getDataPoints());

      if (clear_features)
        // Clear any auxilliary attributes
        fs.garbageCollection();
    }
  }

  @Deprecated
  public void extractDeferredFeatures(FeatureSet fs) throws FeatureExtractorException, AuToBIException {
    extractDeferredFeatures(fs.class_attribute, fs);
    extractDeferredFeatures(fs.getRequiredFeatures(), fs);
  }

  @Deprecated
  public void extractDeferredFeatures(Set<String> features, FeatureSet fs)
      throws FeatureExtractorException, AuToBIException {
    for (String feature : features) {
      extractDeferredFeatures(feature, fs);
    }
  }

  @Deprecated
  public void extractDeferredFeatures(String feature, FeatureSet fs)
      throws FeatureExtractorException, AuToBIException {
    if (!feature_registry.containsKey(feature) && !deferred_feature_registry.containsKey(feature))
      throw new AuToBIException("No feature extractor registered for feature: " + feature);
    FeatureExtractor extractor = deferred_feature_registry.get(feature);
    if (!executed_feature_extractors.contains(extractor) && extractor != null) {
      executed_feature_extractors.add(extractor);
      extractDeferredFeatures(extractor.getRequiredFeatures(), fs);
      AuToBIUtils.debug("running deferred feature extraction for: " + feature);
      extractor.extractFeatures(fs.getDataPoints());

      // Clear any auxilliary attributes from the data points before moving on
      fs.garbageCollection();
    }
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
  private void generatePredictions(String task, FeatureSet fs) throws AuToBIException {
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
   * Retrieves an empty feature set for the given task.
   *
   * @param task a task identifier.
   * @return a corresponding FeatureSet object
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  private FeatureSet getTaskFeatureSet(String task) throws AuToBIException {
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
  private void loadClassifiers() {
    try {
      String pad_filename = getParameter("pitch_accent_detector");
      pitch_accent_detector = ClassifierUtils.readAuToBIClassifier(pad_filename);
    } catch (AuToBIException e) {
    }

    try {
      String pac_filename = getParameter("pitch_accent_classifier");
      pitch_accent_classifier = ClassifierUtils.readAuToBIClassifier(pac_filename);
    } catch (AuToBIException e) {
    }

    try {
      String intonational_phrase_detector_filename = getParameter("IP_detector");
      intonational_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intonational_phrase_detector_filename);
    } catch (AuToBIException e) {
    }

    try {
      String intermediate_phrase_detector_filename = getParameter("ip_detector");
      intermediate_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intermediate_phrase_detector_filename);
    } catch (AuToBIException e) {
    }

    try {
      String phrase_accent_classifier_name = getParameter("phrase_accent_classifier");
      phrase_accent_classifier =
          ClassifierUtils.readAuToBIClassifier(phrase_accent_classifier_name);
    } catch (AuToBIException e) {
    }

    try {
      String boundary_tone_classifier_name = getParameter("boundary_tone_classifier");
      boundary_tone_classifier =
          ClassifierUtils.readAuToBIClassifier(boundary_tone_classifier_name);
    } catch (AuToBIException e) {
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
   * <p/>
   * Currently requires pitch and intensity contours and the spectrum to be calculated outside the FeatureExtractor
   * construct. This will be changed in a future version, with FeatureExtractors used to generate these acoustic
   * qualities.
   *
   * @param spectrum    The specturm of the wave file
   * @param wav_data    The wave data
   * @param norm_params speaker normalization parameters for the wave file
   * @throws FeatureExtractorException If there is a problem registering feature extractors.
   */
  public void registerAllFeatureExtractors(Spectrum spectrum, WavData wav_data,
                                           SpeakerNormalizationParameter norm_params)
      throws FeatureExtractorException {

    registerFeatureExtractor(new PitchAccentFeatureExtractor("nominal_PitchAccent"));
    registerFeatureExtractor(new PitchAccentTypeFeatureExtractor("nominal_PitchAccentType"));
    registerFeatureExtractor(new PhraseAccentFeatureExtractor("nominal_PhraseAccentType"));
    registerFeatureExtractor(new PhraseAccentBoundaryToneFeatureExtractor("nominal_PhraseAccentBoundaryTone"));
    registerFeatureExtractor(new IntonationalPhraseBoundaryFeatureExtractor("nominal_IntonationalPhraseBoundary"));
    registerFeatureExtractor(new IntermediatePhraseBoundaryFeatureExtractor("nominal_IntermediatePhraseBoundary"));


    registerFeatureExtractor(new PitchFeatureExtractor(wav_data, "f0"));
    registerFeatureExtractor(new IntensityFeatureExtractor(wav_data, "I"));
    registerFeatureExtractor(new NormalizedTimeValuePairFeatureExtractor("f0", norm_params));
    registerFeatureExtractor(new NormalizedTimeValuePairFeatureExtractor("I", norm_params));

    registerFeatureExtractor(new DeltaTimeValuePairFeatureExtractor("f0"));
    registerFeatureExtractor(new DeltaTimeValuePairFeatureExtractor("I"));
    registerFeatureExtractor(new DeltaTimeValuePairFeatureExtractor("norm_f0"));
    registerFeatureExtractor(new DeltaTimeValuePairFeatureExtractor("norm_I"));


    registerFeatureExtractor(new TimeValuePairFeatureExtractor("f0"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("norm_f0"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("delta_f0"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("delta_norm_f0"));

    registerFeatureExtractor(new TimeValuePairFeatureExtractor("I"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("norm_I"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("delta_I"));
    registerFeatureExtractor(new TimeValuePairFeatureExtractor("delta_norm_I"));

    for (int low = 0; low <= 19; ++low) {
      for (int high = low + 1; high <= 20; ++high) {
        registerFeatureExtractor(new SpectralTiltFeatureExtractor("bark_tilt", spectrum, low, high));
        registerFeatureExtractor(new SpectrumFeatureExtractor("bark", spectrum, low, high));
      }
    }
    registerFeatureExtractor(new DurationFeatureExtractor());

    ////////////////////
    // Reset Features //
    ////////////////////
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("f0", null));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("I", null));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_f0", null));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_I", null));

    registerFeatureExtractor(new SubregionResetFeatureExtractor("200ms"));
    registerFeatureExtractor(new SubregionResetFeatureExtractor("400ms"));

    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("f0", "200ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("I", "200ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_f0", "200ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_I", "200ms"));

    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("f0", "400ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("I", "400ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_f0", "400ms"));
    registerFeatureExtractor(new ResetTimeValuePairFeatureExtractor("norm_I", "400ms"));

    ////////////////////////
    // Subregion Features //
    ////////////////////////
    registerFeatureExtractor(new PseudosyllableFeatureExtractor("pseudosyllable", wav_data));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_f0", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_f0", "pseudosyllable"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_I", "pseudosyllable"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_I", "pseudosyllable"));

    registerFeatureExtractor(new SubregionFeatureExtractor("200ms"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_f0", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_f0", "200ms"));

    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("norm_I", "200ms"));
    registerFeatureExtractor(new SubregionTimeValuePairFeatureExtractor("delta_norm_I", "200ms"));

    List<String> difference_features = new ArrayList<String>();
    difference_features.add("duration__duration");
    for (String acoustic : new String[]{"f0", "I"}) {
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

      WavData wav = reader.read(wav_filename);
      PitchExtractor pitch_extractor = new PitchExtractor(wav);
      IntensityExtractor intensity_extractor = new IntensityExtractor(wav);
      SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);


      AuToBIUtils.log("Reading words from: " + tg_filename);
      List<Word> words = tg_reader.readWords();

      AuToBIUtils.log("Extracting acoustic information.");
      List<TimeValuePair> pitch_values = pitch_extractor.soundToPitch();
      AuToBIUtils.debug("Extracted Pitch");
      List<TimeValuePair> intensity_values = intensity_extractor.soundToIntensity();
      AuToBIUtils.debug("Extracted Intensity");
      Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);
      AuToBIUtils.debug("Extracted Spectrum");

      Syllabifier syllabifier = new Syllabifier();
      List<Region> pseudosyllables = syllabifier.generatePseudosyllableRegions(wav);

      SubregionUtils.alignLongestSubregionsToWords(words, pseudosyllables, "pseudosyllable");
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

      autobi.loadClassifiers();

      AuToBIUtils.log("Registering Feature Extractors");
      autobi.registerAllFeatureExtractors(spectrum, wav, norm_params);

      for (String task : autobi.getClassificationTasks()) {
        AuToBIUtils.log("Running Hypothesis task -- " + task);

        FeatureSet fs = autobi.getTaskFeatureSet(task);
        fs.setDataPoints(words);
        autobi.extractFeatures(fs, false);

        autobi.generatePredictions(task, fs);
        AuToBIUtils.info(autobi.evaluateTaskPerformance(task, fs));
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
}
