/*  IntermediatePhraseBoundaryTrainer.java

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

import weka.classifiers.meta.AdaBoostM1;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.BasicConfigurator;

/**
 * IntonationalPhraseBoundaryDetectionTrainer is used to train and serialize models that distinguish intonational phrase
 * boundaries from phrase internal word boundaries.
 * <p/>
 * Note: intermediatephrase boundaries are not considered phrase final by this classification task.
 */
public class IntonationalPhraseBoundaryDetectionTrainer {

  public static void main(String[] args) {
    BasicConfigurator.configure();
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    WavReader wav_reader = new WavReader();

    IntonationalPhraseBoundaryDetectionFeatureSet fs = new IntonationalPhraseBoundaryDetectionFeatureSet();

    try {
      String model_file = autobi.getParameter("model_file");
      for (String filename : AuToBIUtils.glob(autobi.getParameter("training_filenames"))) {

        String file_stem = filename.substring(0, filename.lastIndexOf('.'));

        String wav_filename = file_stem + ".wav";

        AuToBIWordReader reader = null;
        if (filename.endsWith("TextGrid")) {
          reader = new TextGridReader(filename);
        } else if (filename.endsWith("ala")) {
          reader = new BURNCReader(filename.replace(".ala", ""));
        }
        
        WavData wav = wav_reader.read(wav_filename);
        SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);

        try {
          AuToBIUtils.log("Reading words from: " + filename);
          List<Word> words = reader.readWords();

          AuToBIUtils.log("Extracting acoustic information.");

          Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);

          autobi.unregisterAllFeatureExtractors();
          autobi.registerAllFeatureExtractors(spectrum, wav);
          autobi.registerFeatureExtractor(new SNPAssignmentFeatureExtractor("normalization_parameters", "speaker_id",
              AuToBIUtils.glob(autobi.getOptionalParameter("normalization_parameters"))));
          autobi.registerNullFeatureExtractor("speaker_id");

          IntonationalPhraseBoundaryDetectionFeatureSet current_fs =
              new IntonationalPhraseBoundaryDetectionFeatureSet();
          current_fs.setDataPoints(words);

          autobi.extractFeatures(current_fs, false);
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
      AuToBIClassifier classifier = new WekaClassifier(new AdaBoostM1());

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
