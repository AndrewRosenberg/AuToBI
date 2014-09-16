/*  PitchAccentClassifierCollectionGenerator.java

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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * PitchAccentCalssifierCollectionGenerator has the task of reading a set of PitchAccentClassifiers and constructing
 * and serializing a PitchAccentClassifierCollection object.
 * <p/>
 * This is a useful utility function when stitching together ensemble members that have been trained in distinct
 * executions. However, it should be made more general to support any ensemble.
 */
public class PitchAccentClassifierCollectionGenerator {

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    PitchAccentDetectionClassifierCollection pacc = new PitchAccentDetectionClassifierCollection();

    try {
      String classifier_stem = autobi.getParameter("classifier_stem");
      String correction_stem = autobi.getParameter("correction_stem");
      String out_file = autobi.getParameter("out_file");

      for (int low = 0; low < 20; ++low) {
        for (int high = low + 1; high <= 20; ++high) {
          AuToBIClassifier classifier =
              ClassifierUtils.readAuToBIClassifier(classifier_stem + low + "_" + high + ".model");
          AuToBIClassifier corrector =
              ClassifierUtils.readAuToBIClassifier(correction_stem + low + "_" + high + ".model");

          pacc.setPitchAccentDetector(low, high, classifier);
          pacc.setCorrectionClassifier(low, high, corrector);
        }
      }

      AuToBIUtils.log("writing collection to: " + out_file);
      FileOutputStream fos;
      ObjectOutputStream out;
      try {
        fos = new FileOutputStream(out_file);
        out = new ObjectOutputStream(fos);
        out.writeObject(pacc);
        out.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
