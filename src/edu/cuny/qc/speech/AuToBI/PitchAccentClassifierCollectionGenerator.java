/*  PitchAccentClassifierCollectionGenerator.java

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
 *
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
