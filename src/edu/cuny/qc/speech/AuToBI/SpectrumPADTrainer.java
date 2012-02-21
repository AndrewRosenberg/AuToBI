/*  SpectrumPADTrainer.java

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
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureset.SpectrumPADFeatureSet;
import edu.cuny.qc.speech.AuToBI.io.TextGridReader;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import weka.classifiers.trees.J48;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

/**
 * SpectrumPADTrainer is used to train pitch accent detection classifiers based on a specified spectral region. 
 */
public class SpectrumPADTrainer {

  private AuToBI autobi;  // An associated AuToBI object to manage parameters.

  /**
   * Constructs a new SpectrumPADTrainer.
   *
   * @param autobi an associated AuToBI object
   */
  public SpectrumPADTrainer(AuToBI autobi) {
    this.autobi = autobi;
  }

  /**
   * Trains a new SpectrumPADClassifier based on a set of filenames and a spectral region indicated by low and high
   * bark boundaries.
   *
   * @param filenames the training filenames
   * @param low       the low bark boundary
   * @param high      the high bark boundary
   * @return a trained SpectrumPADCLassifier
   * @throws Exception if something goes wrong with the training
   */
  private AuToBIClassifier trainSpectrumPADClassifier(List<String> filenames, Integer low, Integer high)
      throws Exception {
    WavReader reader = new WavReader();
    SpectrumPADFeatureSet fs = new SpectrumPADFeatureSet(low, high);
    for (String filename : filenames) {

      String file_stem = filename.substring(0, filename.lastIndexOf('.'));

      String wav_filename = file_stem + ".wav";

      TextGridReader tg_reader = new TextGridReader(filename);

      WavData wav = reader.read(wav_filename);
      SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);
      try {
        AuToBIUtils.log("Reading words from: " + filename);
        List<Word> words = tg_reader.readWords();

        AuToBIUtils.log("Extracting acoustic information.");
        Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);

        autobi.registerAllFeatureExtractors();

        SpectrumPADFeatureSet current_fs =
            new SpectrumPADFeatureSet(low, high);
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

    fs.constructFeatures();

    AuToBIUtils.log("training classifier");
    AuToBIClassifier classifier = new WekaClassifier(new J48());

    classifier.train(fs);

    autobi.unregisterAllFeatureExtractors();
    return classifier;
  }

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      String speaker_normalization_file = autobi.getParameter("speaker_normalization_filename");
      autobi.loadSpeakerNormalizationMapping(speaker_normalization_file);

      SpectrumPADTrainer trainer = new SpectrumPADTrainer(autobi);

      if (autobi.hasParameter("train_all")) {
        String model_stem = autobi.getParameter("model_stem");
        for (int low = 0; low < 20; ++low) {
          for (int high = low + 1; high <= 20; ++high) {
            String model_file = model_stem + low + "_" + high + ".model";
            AuToBIClassifier classifier =
                trainer
                    .trainSpectrumPADClassifier(AuToBIUtils.glob(autobi.getParameter("training_filenames")), low, high);

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

            AuToBIUtils.info("Memory used:" + (
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) / (
                1024.0 * 1024.0) + "MB");
          }
        }

      } else {
        String model_file = autobi.getParameter("model_file");
        Integer low = Integer.parseInt(autobi.getParameter("low_bark"));
        Integer high = Integer.parseInt(autobi.getParameter("high_bark"));

        AuToBIClassifier classifier =
            trainer.trainSpectrumPADClassifier(AuToBIUtils.glob(autobi.getParameter("training_filenames")), low, high);

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
      }
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