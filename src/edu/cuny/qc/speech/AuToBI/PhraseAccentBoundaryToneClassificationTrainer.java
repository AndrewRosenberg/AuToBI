/*  PhraseAccentBoundaryToneClassificationTrainer.java

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
import edu.cuny.qc.speech.AuToBI.classifier.WekaClassifier;
import edu.cuny.qc.speech.AuToBI.featureset.PhraseAccentBoundaryToneClassificationFeatureSet;
import edu.cuny.qc.speech.AuToBI.io.FormattedFile;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import weka.classifiers.functions.SMO;

import java.util.Collection;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * PhraseAccentBoundaryToneClassificationTrainer is responsible for training and serializing a classfier to distinguish
 * pairs of phrase accents and boundary tones.
 * <p/>
 * Only intonational phrase final words are used in this classification.  That is, the destection of phrase boundaries
 * is distinct from the classification of phrase ending tones.
 */
@Deprecated
public class PhraseAccentBoundaryToneClassificationTrainer extends AuToBITrainer {
  /**
   * Constructs a new AuToBITrainer with an associated AuToBI object to manage parameters and feature extraction.
   *
   * @param autobi an AuToBI object.
   */
  public PhraseAccentBoundaryToneClassificationTrainer(AuToBI autobi) {
    super(autobi);
  }

  /**
   * Trains a PitchAccentDetection classifier.
   *
   * @param filenames The filenames to use for training
   * @return A classifier to detect pitch accents
   * @throws Exception if there is a problem with the classifier training.
   */
  public AuToBIClassifier trainClassifier(Collection<FormattedFile> filenames) throws Exception {
    PhraseAccentBoundaryToneClassificationFeatureSet padfs = new PhraseAccentBoundaryToneClassificationFeatureSet();
    AuToBIClassifier classifier = new WekaClassifier(new SMO());

    trainClassifier(filenames, padfs, classifier);
    return classifier;
  }

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    PhraseAccentBoundaryToneClassificationTrainer trainer = new PhraseAccentBoundaryToneClassificationTrainer(autobi);

    try {
      String model_file = autobi.getParameter("model_file");
      autobi.getParameters().setParameter("attribute_omit", "nominal_PhraseAccentBoundaryTone:NOTONE");
      List<FormattedFile> files =
          AuToBIReaderUtils.globFormattedFiles(autobi.getOptionalParameter("training_filenames"));
      files.addAll(
          AuToBIReaderUtils
              .globFormattedFiles(autobi.getOptionalParameter("cprom_filenames"), FormattedFile.Format.CPROM));
      AuToBIClassifier classifier = trainer.trainClassifier(files);

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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}