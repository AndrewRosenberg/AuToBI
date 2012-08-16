/*  AuToBI.java

    Copyright (c) 2009-2012 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.CurveShapeFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.CurveShapeLikelihoodFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.PVALFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import edu.cuny.qc.speech.AuToBI.util.WordReaderUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;

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

  // A map from feature names to the FeatureExtractors that generate them
  private Map<String, FeatureExtractor> feature_registry;

  // A set of FeatureExtractors that have already executed
  protected Set<FeatureExtractor> executed_feature_extractors;

  // A map from input filenames to serialized speaker normalization parameter files.
  private Map<String, String> speaker_norm_file_mapping;

  // A map of the number of times each feature is needed.
  private HashMap<String, Integer> reference_count;

  // A set of features to delete on the next garbage collection call
  private Set<String> dead_features;

  // A list of AuToBITasks to be executed.
  protected HashMap<String, AuToBITask> tasks;

  /**
   * Constructs a new AuToBI object.
   */
  public AuToBI() {
    params = new AuToBIParameters();
    feature_registry = new HashMap<String, FeatureExtractor>();
    executed_feature_extractors = new HashSet<FeatureExtractor>();
    speaker_norm_file_mapping = new HashMap<String, String>();
    tasks = new HashMap<String, AuToBITask>();
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
   * Gets the requested boolean parameter with default value if the parameter is not set.
   *
   * @param parameter     the parameter name
   * @param default_value the default boolean value if not explicitly set.
   * @return the parameter value or null if the parameter was not set
   */
  public Boolean getBooleanParameter(String parameter, boolean default_value) {
    return params.booleanParameter(parameter, default_value);
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
   * Sets the Feature Registry
   *
   * @param registry new feature registry
   */
  public void setFeatureRegistry(Map<String, FeatureExtractor> registry) {
    this.feature_registry = registry;
  }

  /**
   * Retrieves the feature registry
   *
   * @return the feature registry
   */
  public Map<String, FeatureExtractor> getFeatureRegistry() {
    return feature_registry;
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
      if (feature_registry.containsKey(feature_name)) {
        AuToBIUtils.warn("Feature Registry already contains feature: " + feature_name);
      }
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
    if (fs.getClassAttribute() != null)
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
      // Recursively extract the features required by the current FeatureExtractor.
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
      fs.removeFeatureFromDataPoints(feature);
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
  protected String evaluateTaskPerformance(String task, FeatureSet fs) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      return ClassifierUtils.evaluateClassification(autobi_task.getHypFeature(), autobi_task.getTrueFeature(), fs);
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Generates predictions corresponding to a particular task
   *
   * @param task A task identifier
   * @param fs   The feature set
   * @throws AuToBIException if there is no task associated with the identifier.
   */
  public void generatePredictions(String task, FeatureSet fs) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      ClassifierUtils
          .generatePredictions(autobi_task.getClassifier(),
              autobi_task.getHypFeature(),
              autobi_task.getDefaultValue(),
              fs);
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Generates predictions with confidence scores corresponding to a particular task
   *
   * @param task A task identifier
   * @param fs   The feature set
   * @throws AuToBIException if there is no task associated with the identifier.
   */
  public void generatePredictionsWithConfidenceScores(String task, FeatureSet fs) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      ClassifierUtils
          .generatePredictionsWithConfidenceScores(autobi_task.getClassifier(),
              autobi_task.getHypFeature(),
              autobi_task.getConfFeature(),
              autobi_task.getDefaultValue(),
              fs);
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Retrieves an empty feature set for the given task.
   *
   * @param task a task identifier.
   * @return a corresponding FeatureSet object
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  public FeatureSet getTaskFeatureSet(String task) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      if (autobi_task.getFeatureSet() == null) {
        throw new AuToBIException("Task, " + task + ", does not have an associated feature set");
      }
      return autobi_task.getFeatureSet().newInstance();
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Retrieves a default hypothesized feature name set for the given task.
   *
   * @param task a task identifier.
   * @return a string for the hypothesized name
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  public String getHypotheizedFeature(String task) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      return autobi_task.getHypFeature();
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Retrieves a default feature distribution name set for the given task.
   *
   * @param task a task identifier.
   * @return a string for the hypothesized name
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  public String getDistributionFeature(String task) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      return autobi_task.getDistFeature();
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Retrieves a previously loaded AuToBIClassifier for the given task.
   *
   * @param task a task identifier.
   * @return a corresponding FeatureSet object
   * @throws AuToBIException If there is no FeatureSet defined for the task identifier
   */
  public AuToBIClassifier getTaskClassifier(String task) throws AuToBIException {
    if (tasks.containsKey(task)) {
      AuToBITask autobi_task = tasks.get(task);
      return autobi_task.getClassifier();
    } else throw new AuToBIException(
        "Task, " + task + ", is unavailable. Either no classifier has been set, or there is some other problem");
  }

  /**
   * Constructs a FeatureSet from a collection of filenames.
   * <p/>
   * This function handles both the file io of loading the set of data points and wav data, and the feature extraction
   * routine.
   *
   * @param filenames the filenames containing data points.
   * @param fs        an empty feature set to propagate
   * @throws UnsupportedAudioFileException if the wav file doesn't work out
   */
  public void propagateFeatureSet(Collection<FormattedFile> filenames, FeatureSet fs)
      throws UnsupportedAudioFileException {

    if (fs.getClassAttribute() == null) {
      AuToBIUtils.warn("FeatureSet has null class attribute.  Classification experiments will generate errors.");
    }

    Set<Pair<String, String>> attr_omit = new HashSet<Pair<String, String>>();
    Set<String> temp_features = new HashSet<String>();
    if (hasParameter("attribute_omit") && getOptionalParameter("attribute_omit", "").contains(":")) {
      try {
        String[] omission = getParameter("attribute_omit").split(",");
        for (String pair : omission) {
          String[] av_pair = pair.split(":");
          attr_omit.add(new Pair<String, String>(av_pair[0], av_pair[1]));

          if (!fs.getRequiredFeatures().contains(av_pair[0])) {
            temp_features.add(av_pair[0]);
            fs.insertRequiredFeature(av_pair[0]);
          }

        }
      } catch (AuToBIException e) {
        e.printStackTrace();
      }
    }

    ExecutorService threadpool = newFixedThreadPool(Integer.parseInt(getOptionalParameter("num_threads", "1")));
    List<Future<FeatureSet>> results = new ArrayList<Future<FeatureSet>>();
    for (FormattedFile filename : filenames) {
      results.add(threadpool.submit(new FeatureSetPropagator(this, filename, fs)));
    }

    for (Future<FeatureSet> new_fs : results) {
      if (new_fs != null) {

        List<Word> words;
        try {
          words = new_fs.get().getDataPoints();

          // Attribute omission by attribute values.
          // This allows a user to omit data points with particular attributes, for
          // example, to classify only phrase ending words.
          if (attr_omit.size() > 0) {
            for (Word w : words) {
              boolean include = true;
              for (Pair<String, String> e : attr_omit) {
                if (w.hasAttribute(e.first) && w.getAttribute(e.first).equals(e.second)) {
                  include = false;
                }
              }
              if (include) {
                fs.getDataPoints().add(w);
              }
            }
          } else {
            fs.getDataPoints().addAll(words);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }

    for (String f : temp_features) {
      fs.getRequiredFeatures().remove(f);
    }

    threadpool.shutdown();

    fs.constructFeatures();
  }

  /**
   * Initializes the Autobi task list.  This is driven by loading classifiers from serialized objects.
   * <p/>
   * When a classifier is correctly loaded a corresponding task object is created to handle the appropriate bookkeeping
   * <p/>
   * Only those classifiers which have been specified using the following command line parameters are loaded:
   * -pitch_accent_detector -pitch_accent_classifier -IP_detector -ip_detector -phrase_accent_classifier
   * -boundary_tone_classifier
   */
  public void initializeAuToBITasks() {
    try {
      String pad_filename = getParameter("pitch_accent_detector");
      AuToBIClassifier pitch_accent_detector = ClassifierUtils.readAuToBIClassifier(pad_filename);
      AuToBITask task = new AuToBITask();
      task.setClassifier(pitch_accent_detector);
      task.setTrueFeature("nominal_PitchAccent");
      task.setHypFeature("hyp_pitch_accent");
      task.setConfFeature("hyp_pitch_accent_conf");
      task.setDistFeature("hyp_pitch_accent_dist");
      task.setFeatureSet(new PitchAccentDetectionFeatureSet());
      tasks.put("pitch_accent_detection", task);
    } catch (AuToBIException ignored) {
    }

    try {
      String pac_filename = getParameter("pitch_accent_classifier");
      AuToBIClassifier pitch_accent_classifier = ClassifierUtils.readAuToBIClassifier(pac_filename);
      AuToBITask task = new AuToBITask();
      task.setClassifier(pitch_accent_classifier);
      task.setTrueFeature("nominal_PitchAccentType");
      task.setHypFeature("hyp_pitch_accent_type");
      task.setConfFeature("hyp_pitch_accent_type_conf");
      task.setDistFeature("hyp_pitch_accent_type_dist");
      task.setFeatureSet(new PitchAccentClassificationFeatureSet());
      tasks.put("pitch_accent_classification", task);
    } catch (AuToBIException ignored) {
    }

    try {
      String intonational_phrase_detector_filename = getParameter("IP_detector");
      AuToBIClassifier intonational_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intonational_phrase_detector_filename);
      AuToBITask task = new AuToBITask();
      task.setClassifier(intonational_phrase_boundary_detector);
      task.setTrueFeature("nominal_IntonationalPhraseBoundary");
      task.setHypFeature("hyp_IP_location");
      task.setConfFeature("hyp_IP_location_conf");
      task.setDistFeature("hyp_IP_location_dist");
      task.setFeatureSet(new IntonationalPhraseBoundaryDetectionFeatureSet());
      tasks.put("intonational_phrase_boundary_detection", task);
    } catch (AuToBIException ignored) {
    }

    try {
      String intermediate_phrase_detector_filename = getParameter("ip_detector");
      AuToBIClassifier intermediate_phrase_boundary_detector =
          ClassifierUtils.readAuToBIClassifier(intermediate_phrase_detector_filename);
      AuToBITask task = new AuToBITask();
      task.setClassifier(intermediate_phrase_boundary_detector);
      task.setTrueFeature("nominal_IntermediatePhraseBoundary");
      task.setHypFeature("hyp_ip_location");
      task.setConfFeature("hyp_ip_location_conf");
      task.setDistFeature("hyp_ip_location_dist");
      task.setFeatureSet(new IntermediatePhraseBoundaryDetectionFeatureSet());
      tasks.put("intermediate_phrase_boundary_detection", task);
    } catch (AuToBIException ignored) {
    }

    try {
      String phrase_accent_classifier_name = getParameter("phrase_accent_classifier");
      AuToBIClassifier phrase_accent_classifier =
          ClassifierUtils.readAuToBIClassifier(phrase_accent_classifier_name);
      AuToBITask task = new AuToBITask();
      task.setClassifier(phrase_accent_classifier);
      task.setTrueFeature("nominal_PhraseAccent");
      task.setHypFeature("hyp_phrase_accent");
      task.setConfFeature("hyp_phrase_accent_conf");
      task.setDistFeature("hyp_phrase_accent_dist");
      task.setFeatureSet(new PhraseAccentClassificationFeatureSet());
      tasks.put("phrase_accent_classification", task);
    } catch (AuToBIException ignored) {
    }

    try {
      String boundary_tone_classifier_name = getParameter("boundary_tone_classifier");
      AuToBIClassifier boundary_tone_classifier =
          ClassifierUtils.readAuToBIClassifier(boundary_tone_classifier_name);
      AuToBITask task = new AuToBITask();
      task.setClassifier(boundary_tone_classifier);
      task.setTrueFeature("nominal_PhraseAccentBoundaryTone");
      task.setHypFeature("hyp_pabt");
      task.setConfFeature("hyp_pabt_conf");
      task.setDistFeature("hyp_pabt_dist");
      task.setFeatureSet(new PhraseAccentBoundaryToneClassificationFeatureSet());
      tasks.put("boundary_tone_classification", task);
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
  public Set<String> getClassificationTasks() {
    return tasks.keySet();
  }

  /**
   * Writes a TextGrid file containing words and hypothesized ToBI labels.
   *
   * @param words    The words
   * @param out_file The destination file
   * @throws IOException If there is a problem writing to the destination file.
   */
  public void writeTextGrid(List<Word> words, String out_file) throws IOException {
    String text_grid = generateTextGridString(words);

    AuToBIFileWriter writer = new AuToBIFileWriter(out_file);
    writer.write(text_grid);
    writer.close();
  }

  /**
   * Generates a TextGrid representation of the words and hypothesized ToBI labels.
   *
   * @param words the words to output
   * @return a string representing the textgrid contents of the words.
   */
  public String generateTextGridString(List<Word> words) {
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
      text_grid += "intervals [" + (i + 1) + "]:\n";
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
      if (getBooleanParameter("distributions", false)) {
        String det_dist_feature = tasks.get("pitch_accent_detection").getDistFeature();
        String class_dist_feature = tasks.get("pitch_accent_classification").getDistFeature();
        if (w.hasAttribute(det_dist_feature)) {
          text = w.getAttribute(det_dist_feature).toString();
        }
        if (w.hasAttribute(class_dist_feature)) {
          text += w.getAttribute(class_dist_feature).toString();
        }
      } else {
        if (w.hasAttribute(tasks.get("pitch_accent_detection").getHypFeature())) {
          text = w.getAttribute(tasks.get("pitch_accent_detection").getHypFeature()).toString();
        }
      }

      text_grid += "intervals [" + (i + 1) + "]:\n";
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
      if (getBooleanParameter("distributions", false)) {
        if (w.hasAttribute(tasks.get("intonational_phrase_boundary_detection").getDistFeature())) {
          text = w.getAttribute(tasks.get("intonational_phrase_boundary_detection").getDistFeature()).toString();
        }
        if (w.hasAttribute(tasks.get("intermediate_phrase_boundary_detection").getDistFeature())) {
          text = w.getAttribute(tasks.get("intermediate_phrase_boundary_detection").getDistFeature()).toString();
        }
        if (w.hasAttribute(tasks.get("boundary_tone_classification").getDistFeature())) {
          text += w.getAttribute(tasks.get("boundary_tone_classification").getDistFeature()).toString();
        }
        if (w.hasAttribute(tasks.get("phrase_accent_classification").getDistFeature())) {
          text += w.getAttribute(tasks.get("phrase_accent_classification").getDistFeature()).toString();
        }
      } else {
        if (w.hasAttribute("hyp_phrase_boundary")) {
          text = w.getAttribute("hyp_phrase_boundary").toString();
        }
      }

      text_grid += "intervals [" + (i + 1) + "]:\n";
      text_grid += "xmin = " + w.getStart() + "\n";
      text_grid += "xmax = " + w.getEnd() + "\n";
      text_grid += "text = \"" + text + "\"\n";
    }
    return text_grid;
  }

  /**
   * Registers a large default set of feature extractors.
   *
   * @throws FeatureExtractorException If there is a problem registering (not running) feature extractors.
   */
  public void registerAllFeatureExtractors()
      throws FeatureExtractorException {

    /** TODO: This approach needs to be rethought.  As AuToBI's capabilities are increased, this function grows, and the
     * size of the "default" feature registry increases.
     *
     * There are two approaches that seem possible:
     * 1. Move this functionality to a config file.  Only those feature extractors that are in the config file are
     * constructed and registered.  However, this won't help the issue of a cumbersome feature registry.
     *
     * 2. On-demand feature registry propagation.  The idea here would be that you would need to know which
     * features can be extracted from which feature extractor.  But this information can be stored in a config file.
     * Rather than constructing objects for every possible feature extractor, at extraction time, read the mapping of
     * feature names and FeatureExtractor object names (and parameters).  "Register" each of those feature extractors
     * that are needed -- and recurse up the required features dependency graph.  The run feature extraction as usual.
     *
     * This will keep the registry small, though there is more overhead when you read the config file.  The other issue
     * is how is config file gets propagated.  Can it be done automatically? or does the author of a new feature extractor
     * need to write it manually?  Or one better, can we scan the featureextractor contents in memory?  This last step is
     * a reach goal and will need to be a part of the next version.  I believe that it will require a more precise templating
     * of derived feature names.  This will require re-writing a lot of the feature extractors.  While worth it, this
     * will take some person hours.  Possibly a good
     */
    registerNullFeatureExtractor("wav");
    String[] acoustic_features = new String[]{"f0", "log_f0", "I"};

    registerFeatureExtractor(new PitchAccentFeatureExtractor("nominal_PitchAccent"));
    registerFeatureExtractor(new PitchAccentTypeFeatureExtractor("nominal_PitchAccentType"));
    registerFeatureExtractor(new PhraseAccentFeatureExtractor("nominal_PhraseAccentType"));
    registerFeatureExtractor(new PhraseAccentBoundaryToneFeatureExtractor("nominal_PhraseAccentBoundaryTone"));
    registerFeatureExtractor(new IntonationalPhraseBoundaryFeatureExtractor("nominal_IntonationalPhraseBoundary"));
    registerFeatureExtractor(new IntermediatePhraseBoundaryFeatureExtractor("nominal_IntermediatePhraseBoundary"));

    registerFeatureExtractor(new PitchFeatureExtractor("f0"));
    registerFeatureExtractor(new LogContourFeatureExtractor("f0", "log_f0"));
    registerFeatureExtractor(new IntensityFeatureExtractor("I"));

    if (hasParameter("normalization_parameters")) {
      String known_speaker = null;
      if (getBooleanParameter("known_speaker", false)) {
        known_speaker = "speaker_id";
      }
      try {
        registerFeatureExtractor(new SNPAssignmentFeatureExtractor("normalization_parameters", known_speaker,
            AuToBIUtils.glob(getOptionalParameter("normalization_parameters"))));
      } catch (AuToBIException e) {
        AuToBIUtils.error(e.getMessage());
      }
    } else {
      registerFeatureExtractor(new NormalizationParameterFeatureExtractor("normalization_parameters"));
    }

    for (String acoustic : acoustic_features) {
      registerFeatureExtractor(new NormalizedContourFeatureExtractor(acoustic, "normalization_parameters"));
    }

    // Register Subregion feature extractors
    registerFeatureExtractor(new PseudosyllableFeatureExtractor("pseudosyllable"));
    registerFeatureExtractor(new SubregionFeatureExtractor("200ms"));

    // Register Delta Contour Extractors
    for (String acoustic : acoustic_features) {
      for (String norm : new String[]{"", "norm_"}) {
        registerFeatureExtractor(new DeltaContourFeatureExtractor(norm + acoustic));
      }
    }

    // Register subregion contour extractors
    for (String acoustic : acoustic_features) {
      for (String norm : new String[]{"", "norm_"}) {
        for (String slope : new String[]{"", "delta_"}) {
          for (String subregion : new String[]{"pseudosyllable", "200ms"}) {
            registerFeatureExtractor(new SubregionContourExtractor(slope + norm + acoustic, subregion));
          }
        }
      }
    }

    // Register Contour Feature Extractors
    for (String acoustic : acoustic_features) {
      for (String norm : new String[]{"", "norm_"}) {
        for (String slope : new String[]{"", "delta_"}) {
          for (String subregion : new String[]{"", "_pseudosyllable", "_200ms"}) {
            registerFeatureExtractor(new ContourFeatureExtractor(slope + norm + acoustic + subregion));
          }
        }
      }
    }

    registerFeatureExtractor(new SpectrumFeatureExtractor("spectrum"));

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

    // Difference Features
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

    // Feature Extractors developed based on Mishra et al. INTERSPEECH 2012 and Rosenberg SLT 2012
    registerFeatureExtractor(new AUContourFeatureExtractor("log_f0"));
    registerFeatureExtractor(new AUContourFeatureExtractor("I"));

    try {
      registerFeatureExtractor(new SubregionFeatureExtractor("400ms"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
    difference_features = new ArrayList<String>();

    for (String c : new String[]{"rnorm_I", "norm_log_f0", "norm_log_f0rnorm_I"}) {
      for (String delta : new String[]{"delta_", ""}) {
        if (!c.equals("norm_log_f0")) {
          for (String subregion : new String[]{"pseudosyllable", "200ms", "400ms"}) {
            registerFeatureExtractor(new SubregionContourExtractor(delta + c, subregion));
            registerFeatureExtractor(new ContourFeatureExtractor(delta + c + "_" + subregion));
          }
        }
        for (String subregion : new String[]{"", "_pseudosyllable", "_200ms", "_400ms"}) {
          registerFeatureExtractor(new PVALFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new CurveShapeFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new CurveShapeLikelihoodFeatureExtractor(delta + c + subregion));

          registerFeatureExtractor(new HighLowComponentFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new HighLowDifferenceFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new ContourCenterOfGravityFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new AUContourFeatureExtractor(delta + c + subregion));
          registerFeatureExtractor(new TiltFeatureExtractor(delta + c + subregion));
        }

        if (!c.equals("norm_log_f0")) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            difference_features.add(delta + c + "__" + agg);
          }
        }
      }
    }
    registerFeatureExtractor(new DifferenceFeatureExtractor(difference_features));

    registerFeatureExtractor(new VoicingRatioFeatureExtractor("log_f0"));

    // Range normalization for I
    registerFeatureExtractor(new RangeNormalizedContourFeatureExtractor("I", "normalization_parameters"));

    // Combined contour
    registerFeatureExtractor(new CombinedContourFeatureExtractor("norm_log_f0", "rnorm_I", 1));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("norm_log_f0rnorm_I"));
    registerFeatureExtractor(new DeltaContourFeatureExtractor("rnorm_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_rnorm_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("rnorm_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("norm_log_f0rnorm_I"));
    registerFeatureExtractor(new ContourFeatureExtractor("delta_norm_log_f0rnorm_I"));

    // Skew
    registerFeatureExtractor(new SkewFeatureExtractor("norm_log_f0", "rnorm_I"));

    registerFeatureExtractor(new RatioFeatureExtractor("norm_log_f0__area", "rnorm_I__area"));
    registerFeatureExtractor(new FeatureDifferenceFeatureExtractor("norm_log_f0__area", "rnorm_I__area"));

    // Contour peak
    registerFeatureExtractor(new RatioFeatureExtractor("norm_log_f0__PVLocation", "rnorm_I__PVLocation"));
    registerFeatureExtractor(
        new FeatureDifferenceFeatureExtractor("norm_log_f0__PVLocation", "rnorm_I__PVLocation"));

    // Contour RMSE and Contour Error
    registerFeatureExtractor(new ContourDifferenceFeatureExtractor("norm_log_f0", "rnorm_I"));

    // Combined curve likelihood
    registerFeatureExtractor(new TwoWayCurveLikelihoodShapeFeatureExtractor("norm_log_f0", "rnorm_I"));
  }

  @Deprecated
  private void registerPitchAccentCollectionFeatureExtractors() throws FeatureExtractorException {
    try {
      String pad_filename = getParameter("spectral_pitch_accent_detector_collection");
      PitchAccentDetectionClassifierCollection pacc;

      /**
       * TODO: this shouldn't happen here.  This ensemble classifier should be an AuToBI classifier that is
       * loaded from a special case pitch accent detection AuToBITask
       */
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
              new SpectrumPADFeatureExtractor(low, high, pacc.getPitchAccentDetector(low, high),
                  new SpectrumPADFeatureSet(low, high)));
          registerFeatureExtractor(
              new CorrectionSpectrumPADFeatureExtractor(low, high, pacc.getCorrectionClassifier(low, high),
                  new CorrectionSpectrumPADFeatureSet(low, high)));
        }
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
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
      speaker_norm_file_mapping.put(fields[0].trim(), fields[1].trim());
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

    autobi.run();
  }

  public void run() {
    try {
      if (hasParameter("input_file") && hasParameter("cprom_file")) {
        throw new AuToBIException(
            "Both -input_file and -cprom_file are entered.  Only one input file may be specified.");
      }

      String wav_filename = getParameter("wav_file");
      WavReader reader = new WavReader();
      WavData wav = reader.read(wav_filename);

      String filename = getOptionalParameter("input_file");
      AuToBIWordReader word_reader;
      FormattedFile file;

      if (hasParameter("input_file")) {
        // Let the FormattedFile constructor determine the file based on the extension or other file name conventions
        file = new FormattedFile(getOptionalParameter("input_file"));
        word_reader = WordReaderUtils.getAppropriateReader(file, getParameters());
      } else if (hasParameter("cprom_file")) {
        // Since both C-Prom files and other TextGrid files use the ".TextGrid" extension, the user needs to specify cprom files explicitly
        file = new FormattedFile(getOptionalParameter("cprom_file"), FormattedFile.Format.CPROM);
        word_reader = WordReaderUtils.getAppropriateReader(file, getParameters());
      } else {
        AuToBIUtils.info(
            "No -input_file or -cprom_file filename specified.  Generating segmentation based on acoustic pseudosyllabification.");
        wav.setFilename(wav_filename);
        if (hasParameter("silence_threshold")) {
          Double threshold = Double.parseDouble(getParameter("silence_threshold"));
          word_reader = new PseudosyllableWordReader(wav, threshold);
        } else {
          word_reader = new PseudosyllableWordReader(wav);
        }
      }
      AuToBIUtils.log("Reading words from: " + filename);

      if (word_reader == null) {
        AuToBIUtils.error("Unable to create wordreader for file: " + filename + "\n\tCheck the file extension.");
        return;
      }

      if (hasParameter("silence_regex")) {
        word_reader.setSilenceRegex(getParameter("silence_regex"));
      }

      List<Word> words = word_reader.readWords();

      FeatureSet autobi_fs = new FeatureSet();
      autobi_fs.setDataPoints(words);
      for (Word w : words) {
        w.setAttribute("wav", wav);
      }
      initializeAuToBITasks();

      AuToBIUtils.log("Registering Feature Extractors");
      registerAllFeatureExtractors();
      registerNullFeatureExtractor("speaker_id");

      for (AuToBITask task : tasks.values()) {
        FeatureSet fs = task.getFeatureSet();
        AuToBIClassifier classifier = task.getClassifier();

        String hyp_feature = task.getHypFeature();
        registerFeatureExtractor(new HypothesizedEventFeatureExtractor(hyp_feature, classifier, fs));
        autobi_fs.insertRequiredFeature(hyp_feature);

        if (getBooleanParameter("distributions", false)) {
          String dist_feature = task.getDistFeature();
          registerFeatureExtractor(new HypothesizedDistributionFeatureExtractor(dist_feature, classifier, fs));
          autobi_fs.insertRequiredFeature(dist_feature);
        }

        autobi_fs.insertRequiredFeature(fs.getClassAttribute());
      }

      extractFeatures(autobi_fs);
      autobi_fs.constructFeatures();

      for (String task : getClassificationTasks()) {
        AuToBIUtils.info(evaluateTaskPerformance(task, autobi_fs));
      }

      if (hasParameter("out_file")) {
        AuToBIUtils.mergeAuToBIHypotheses(words);
        String hypothesis_file = getParameter("out_file");
        AuToBIUtils.info("Writing hypotheses to " + hypothesis_file);
        writeTextGrid(words, hypothesis_file);
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

  /**
   * Gets command line parameters.
   *
   * @return an AuToBIParameters object
   */
  public AuToBIParameters getParameters() {
    return params;
  }

  /**
   * Sets AuToBIParameters.
   *
   * @param params the parameters.
   */
  public void setParameters(AuToBIParameters params) {
    this.params = params;
  }
}
