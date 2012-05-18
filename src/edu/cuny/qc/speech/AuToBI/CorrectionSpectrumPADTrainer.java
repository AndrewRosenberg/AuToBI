/*  CorrectionSpectrumPADTrainer.java

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
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.MatchingFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureextractor.XValSpectrumPADFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureset.CorrectionSpectrumPADFeatureSet;
import edu.cuny.qc.speech.AuToBI.featureset.SpectrumPADFeatureSet;
import edu.cuny.qc.speech.AuToBI.io.TextGridReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import weka.classifiers.trees.J48;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

/**
 * Entry point class to train and store Correcting Classifiers for Spectral Pitch Accent Detection.
 * <p/>
 * The main function defined in this class generates a single correcting classifier for one spectral region defined by
 * the parameters -low_bark and -high_bark.
 */
public class CorrectionSpectrumPADTrainer {

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      Integer low = Integer.parseInt(autobi.getParameter("low_bark"));
      Integer high = Integer.parseInt(autobi.getParameter("high_bark"));
      Integer num_folds = Integer.parseInt(autobi.getOptionalParameter("num_folds", "10"));
      CorrectionSpectrumPADFeatureSet fs = new CorrectionSpectrumPADFeatureSet(low, high);

      String model_file = autobi.getParameter("model_file");
      String speaker_normalization_file = autobi.getParameter("speaker_normalization_filename");
      for (String filename : AuToBIUtils.glob(autobi.getParameter("training_filenames"))) {

        autobi.loadSpeakerNormalizationMapping(speaker_normalization_file);

        TextGridReader tg_reader = new TextGridReader(filename);

        try {
          AuToBIUtils.log("Reading words from: " + filename);
          List<Word> words = tg_reader.readWords();

          AuToBIUtils.log("Extracting acoustic information.");

          // If stored normalization data is unavailable generate normalization data from the input file.
          autobi.registerAllFeatureExtractors();

          // At training the feature set requires nominal_PitchAccent to establish the "correct" class.
          fs.getRequiredFeatures().add("nominal_PitchAccent");

          // register a feature extractors specific for the XVal predictions
          autobi.registerFeatureExtractor(
              new XValSpectrumPADFeatureExtractor(low, high, num_folds, new SpectrumPADFeatureSet(low, high)));
          autobi.registerFeatureExtractor(
              new MatchingFeatureExtractor("nominal_PitchAccent", "nominal_bark_" + low + "_" + high + "__prediction",
                  "nominal_PitchAccentCorrect"));

          CorrectionSpectrumPADFeatureSet current_fs =
              new CorrectionSpectrumPADFeatureSet(low, high);
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

      try {
        autobi.extractFeatures(fs);
      } catch (FeatureExtractorException e) {
        AuToBIUtils.error(e.getMessage());
      }

      fs.getRequiredFeatures().remove("nominal_PitchAccent");

      fs.constructFeatures();

      AuToBIUtils.log("training classifier");
      AuToBIClassifier classifier = new WekaClassifier(new J48());

      classifier.train(fs);

      AuToBIUtils.debug(classifier.toString());

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

      // serialize model
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}