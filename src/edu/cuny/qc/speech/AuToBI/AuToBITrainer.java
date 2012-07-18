/*  AuToBITrainer.java

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
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * A Class to handle the training of autobi models, based on a set of training files, a FeatureSet describing the
 * required features, and an AuToBIClassifier to train.
 * <p/>
 * This class is used to drive the training of all AuToBI classifiers.  AuToBITask objects store the information
 * required to train classifiers, and are used to maintain consistency around AuToBI.     This parameterization allows
 * this class to treat all tasks identically.
 * <p/>
 * The only difference between AuToBI tasks is that the prosodic event classification tasks are trained only on data
 * points where that prosodic event actualy occurs.  There
 *
 * @see edu.cuny.qc.speech.AuToBI.core.FeatureSet
 * @see edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier
 */
public class AuToBITrainer {
  private AuToBI autobi;  // An AuToBI object to store parameters and handle the feature extraction.

  /**
   * Constructs a new AuToBITrainer with an associated AuToBI object to manage parameters and feature extraction.
   *
   * @param autobi an AuToBI object.
   */
  public AuToBITrainer(AuToBI autobi) {
    this.autobi = autobi;
  }

  /**
   * Trains an AuToBI classifier.
   * <p/>
   * Reads a set of filenames. Extract a set of features described by a FeatureSet object. Trains a classifier.  An
   * empty classifier to be trained should be passed in.  This allows a user to specify the type of classifier.
   *
   * @param filenames  The set of training files
   * @param fs         The FeatureSet describing the required features for the task
   * @param classifier The classifier to train
   * @throws Exception If there is a problem with the classifier.train function.
   */
  public void trainClassifier(Collection<FormattedFile> filenames, FeatureSet fs, AuToBIClassifier classifier)
      throws Exception {
    if (filenames.size() == 0)
      throw new AuToBIException("No filenames specified for training. Aborting.");
    try {
      autobi.registerAllFeatureExtractors();
      autobi.registerNullFeatureExtractor("speaker_id");

      autobi.propagateFeatureSet(filenames, fs);

      AuToBIUtils.log("training classifier");
      classifier.train(fs);
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    List<FormattedFile> filenames;
    try {
      filenames = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("training_filenames"));
    } catch (AuToBIException e) {
      try {
        filenames = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("cprom_filenames"));
      } catch (AuToBIException e1) {
        AuToBIUtils.error("No training files specified with -training_filenames or -cprom_filenames");
        return;
      }
    }

    HashMap<String, AuToBITask> tasks = AuToBIUtils.createTaskListFromParameters(autobi.getParameters());

    for (String task_label : tasks.keySet()) {
      AuToBITask task = tasks.get(task_label);
      AuToBITrainer trainer = new AuToBITrainer(autobi);
      try {

        // Tone classification tasks ignore those points that do not have any associated prosodic event
        if (task_label.equals("phrase_accent_classifier")) {
          autobi.getParameters().setParameter("attribute_omit", "nominal_PitchAccentType:NOTONE");
        } else if (task_label.equals("phrase_accent_boundary_tone_classifier")) {
          autobi.getParameters().setParameter("attribute_omit", "nominal_PhraseAccentBoundaryTone:NOTONE");
        } else if (task_label.equals("phrase_accent_classifier")) {
          autobi.getParameters().setParameter("attribute_omit", "nominal_PhraseAccent:NOTONE");
        } else {
          autobi.getParameters().setParameter("attribute_omit", "");
        }
        trainer.trainClassifier(filenames, task.getFeatureSet(), task.getClassifier());
      } catch (Exception e) {
        AuToBIUtils.error("Error training classifier for " + task_label);
        continue;
      }

      String output_file;
      try {
        output_file = autobi.getParameter(task_label);
      } catch (AuToBIException e) {
        AuToBIUtils.error("Problem reading classifier filename parameter: " + e.getMessage());
        continue;
      }
      try {
        ClassifierUtils.writeAuToBIClassifier(output_file, task.getClassifier());
      } catch (IOException e) {
        AuToBIUtils.error("Could not write AuToBIClassifier: " + e.getMessage());
      }
    }
  }
}
