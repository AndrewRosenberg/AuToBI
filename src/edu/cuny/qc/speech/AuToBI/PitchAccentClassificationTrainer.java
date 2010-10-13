/*  PitchAccentClassificationTrainer.java

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

import weka.classifiers.functions.SMO;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

/**
 * PitchAccentClassificationTrainer trains and serializes a pitch accent classification model.
 */
public class PitchAccentClassificationTrainer {

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    WavReader reader = new WavReader();

    PitchAccentClassificationFeatureSet fs = new PitchAccentClassificationFeatureSet();

    try {
      String model_file = autobi.getParameter("model_file");
      for (String filename : AuToBIUtils.glob(autobi.getParameter("training_filenames"))) {

        String file_stem = filename.substring(0, filename.lastIndexOf('.'));

        String wav_filename = file_stem + ".wav";

        TextGridReader tg_reader = new TextGridReader(filename);

        WavData wav = reader.read(wav_filename);
        SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);
        try {
          AuToBIUtils.log("Reading words from: " + filename);
          List<Word> tmp_words = tg_reader.readWords();

          List<Word> words = new ArrayList<Word>();
          for (Word w : tmp_words) {
            if (w.isAccented())
              words.add(w);
          }

          AuToBIUtils.log("Extracting acoustic information.");

          Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);

          autobi.unregisterAllFeatureExtractors();
          autobi.registerAllFeatureExtractors(spectrum, wav);
          autobi.registerFeatureExtractor(new SNPAssignmentFeatureExtractor("normalization_parameters", "speaker_id",
              AuToBIUtils.glob(autobi.getOptionalParameter("normalization_parameters"))));
          autobi.registerNullFeatureExtractor("speaker_id");

          PitchAccentClassificationFeatureSet current_fs =
              new PitchAccentClassificationFeatureSet();
          current_fs.setDataPoints(words);

          AuToBIUtils.info("Extracting Features.");
          autobi.extractFeatures(current_fs);
          current_fs.garbageCollection();

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
      AuToBIClassifier classifier = new EnsembleSampledClassifier(new WekaClassifier(new SMO()));

      classifier.train(fs);

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