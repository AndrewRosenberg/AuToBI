/*  AuToBITrainer.java

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
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.SNPAssignmentFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureset.PitchAccentDetectionFeatureSet;
import edu.cuny.qc.speech.AuToBI.io.AuToBIWordReader;
import edu.cuny.qc.speech.AuToBI.io.BURNCReader;
import edu.cuny.qc.speech.AuToBI.io.TextGridReader;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * A Class to handle the training of autobi models, based on a set of training files, a FeatureSet describing the
 * required features, and an AuToBIClassifier to train.
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
  public void trainClassifier(Collection<String> filenames, FeatureSet fs, AuToBIClassifier classifier)
      throws Exception {
    for (String filename : filenames) {

      String file_stem = filename.substring(0, filename.lastIndexOf('.'));

      String wav_filename = file_stem + ".wav";

      AuToBIWordReader reader = null;
      if (filename.endsWith("TextGrid")) {
        reader = new TextGridReader(filename);
      } else if (filename.endsWith("ala")) {
        reader = new BURNCReader(filename.replace(".ala", ""));
      }

      WavReader wav_reader = new WavReader();
      try {
        WavData wav = wav_reader.read(wav_filename);
        SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);
        AuToBIUtils.log("Reading words from: " + filename);
        List<Word> words = reader.readWords();

        Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);
        autobi.unregisterAllFeatureExtractors();
        autobi.registerAllFeatureExtractors(spectrum, wav);
        autobi.registerFeatureExtractor(new SNPAssignmentFeatureExtractor("normalization_parameters", "speaker_id",
            AuToBIUtils.glob(autobi.getOptionalParameter("normalization_parameters"))));
        autobi.registerNullFeatureExtractor("speaker_id");

        PitchAccentDetectionFeatureSet current_fs = new PitchAccentDetectionFeatureSet();
        current_fs.setDataPoints(words);

        autobi.extractFeatures(current_fs);

        fs.getDataPoints().addAll(words);
      } catch (AuToBIException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (FeatureExtractorException e) {
        e.printStackTrace();
      }
    }

    AuToBIUtils.log("training classifier");
    fs.constructFeatures();
    classifier.train(fs);
  }

}
