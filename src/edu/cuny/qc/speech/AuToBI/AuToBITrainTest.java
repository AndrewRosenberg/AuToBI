package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.classifier.WekaClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import weka.classifiers.functions.Logistic;

import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class AuToBITrainTest {
  public static void main(String[] args) {

    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      List<FormattedFile> training_files = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("training_files"));
      List<FormattedFile> testing_files = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("testing_files"));
      String task = autobi.getParameter("task");
      String model_file = autobi.getParameter("model_file");

      // Added here to comply to comply with the new feature registration changes to FeatureSetPropagator
      autobi.registerAllFeatureExtractors();
      autobi.registerNullFeatureExtractor("speaker_id");

      // implement getFeatureSet() so it returns an appropriate feature
      // set.
      FeatureSet training_fs = getFeatureSet(task);
      autobi.propagateFeatureSet(training_files, training_fs);

      FeatureSet testing_fs = getFeatureSet(task);
      autobi.propagateFeatureSet(testing_files, testing_fs);

      // any other classifier can be slotted in here.
      WekaClassifier classifier = new WekaClassifier(new Logistic());
      classifier.train(training_fs);

      // writing model file
      AuToBIUtils.log("writing model to: " + model_file);
      FileOutputStream fos;
      ObjectOutputStream out;
      try {
        fos = new FileOutputStream(model_file);
        out = new ObjectOutputStream(fos);
        out.writeObject(classifier);
        out.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      // prediction on test set
      ClassifierUtils.generatePredictions(classifier, "hyp", "DEFAULT", testing_fs);

      EvaluationResults er =
          ClassifierUtils.generateEvaluationResults("hyp", testing_fs.getClassAttribute(), testing_fs);

      EvaluationSummary es = new EvaluationSummary(er);

      AuToBIUtils.log("Test Results on test set\n" + es.toString());
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @return
   * @throws AuToBIException
   */
  private static FeatureSet getFeatureSet(String task) throws AuToBIException {
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
}