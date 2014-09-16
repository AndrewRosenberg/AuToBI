/* AuToBITrainTest.java

  Copyright 2014 Andrew Rosenberg
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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AuToBITrainTest {
  public static void main(String[] args) {

    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      List<FormattedFile> training_files;
      List<FormattedFile> testing_files;

      FormattedFile.Format format = null;

      if (autobi.hasParameter("format")) {
        if (autobi.getOptionalParameter("format", "none").equals("rhapsodie")) {
          format = FormattedFile.Format.RHAPSODIE;
        } else if (autobi.getOptionalParameter("format", "none").equals("cprom")) {
          format = FormattedFile.Format.CPROM;
        } else {
          AuToBIUtils.warn(
              "Unrecognized or empty -format parameter. Defaulting to standard format.\n\t Current valid formats are:" +
                  " " +
                  "rhapsodie, cprom");
        }
      }
      training_files = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("training_files"), format);
      testing_files = AuToBIReaderUtils.globFormattedFiles(autobi.getParameter("testing_files"), format);


      HashMap<String, AuToBITask> tasks = AuToBIUtils.createTaskListFromParameters(autobi.getParameters(), false);

      if (tasks.size() == 0) {
        AuToBIUtils.warn(
            "No AuToBI tasks have been specified. Please indicate one or more tasks using one of the following:\n" +
                "-pitch_accent_detector\n-pitch_accent_classifier\n-intontational_phrase_boundary_detector\n" +
                "-intermediate_phrase_boundary_detector\n-phrase_accent_classifier\n" +
                "-phrase_accent_boundary_tone_classifier");
        return;
      }

      for (String task_label : tasks.keySet()) {
        AuToBITask task = tasks.get(task_label);
        AuToBITrainer trainer = new AuToBITrainer(autobi);
        try {

          // Tone classification tasks ignore those points that do not have any associated prosodic event
          if (task_label.equals("pitch_accent_classification")) {
            autobi.getParameters().setParameter("attribute_omit", "nominal_PitchAccentType:NOACCENT");
          } else if (task_label.equals("phrase_accent_boundary_tone_classification")) {
            autobi.getParameters().setParameter("attribute_omit", "nominal_PhraseAccentBoundaryTone:NOTONE");
          } else if (task_label.equals("phrase_accent_classification")) {
            autobi.getParameters().setParameter("attribute_omit", "nominal_PhraseAccent:NOTONE");
          } else if (task_label.equals("intermediate_phrase_boundary_detection")) {
            autobi.getParameters()
                .setParameter("attribute_omit", "nominal_IntermediatePhraseBoundary:INTONATIONAL_BOUNDARY");
          } else {
            autobi.getParameters().setParameter("attribute_omit", "");
          }
          trainer.trainClassifier(training_files, task.getFeatureSet(), task.getClassifier());

        } catch (Exception e) {
          e.printStackTrace();
          AuToBIUtils.error("Error training classifier for " + task_label);
          continue;
        }
        FeatureSet testing_fs = task.getFeatureSet().newInstance();
        testing_fs.getDataPoints().clear();
        autobi.propagateFeatureSet(testing_files, testing_fs);

        if (autobi.hasParameter("testing_arff_file")) {
          testing_fs.writeArff(autobi.getParameter("testing_arff_file"), "AuToBIGenerated");
        }

        if (autobi.hasParameter("testing_liblinear_file")) {
          testing_fs.writeLibLinear(autobi.getParameter("testing_liblinear_file"));
        }

        // writing model file
        AuToBIUtils.log("writing model to: " + autobi.getParameter(task_label));
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
          fos = new FileOutputStream(autobi.getParameter(task_label));
          out = new ObjectOutputStream(fos);
          out.writeObject(task.getClassifier());
          out.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        // prediction on test set
        ClassifierUtils.generatePredictions(task.getClassifier(), "hyp", "DEFAULT", testing_fs);

        EvaluationResults er =
            ClassifierUtils.generateEvaluationResults("hyp", testing_fs.getClassAttribute(), testing_fs);

        EvaluationSummary es = new EvaluationSummary(er);

        AuToBIUtils.log("Test Results on test set\n" + es.toString());
      }
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}