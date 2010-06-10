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
 * IntonationalPhraseBoundaryDetectionTrainer is used to train and serialize models that distinguish
 * intonational phrase boundaries from phrase internal word boundaries.
 *
 * Note: intermediatephrase boundaries are not considered phrase final by this classification task.
 */
public class IntonationalPhraseBoundaryDetectionTrainer {

  public static void main(String[] args) {
    BasicConfigurator.configure();
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    WavReader reader = new WavReader();

    IntonationalPhraseBoundaryDetectionFeatureSet fs = new IntonationalPhraseBoundaryDetectionFeatureSet();

    try {
      String model_file = autobi.getParameter("model_file");
      String speaker_normalization_file = autobi.getParameter("speaker_normalization_filename");
      for (String filename : AuToBIUtils.glob(autobi.getParameter("training_filenames"))) {

        String file_stem = filename.substring(0, filename.lastIndexOf('.'));

        String wav_filename = file_stem + ".wav";
        autobi.loadSpeakerNormalizationMapping(speaker_normalization_file);
        String norm_param_filename = autobi.getSpeakerNormParamFilename(filename);

        TextGridReader tg_reader = new TextGridReader(filename);

        WavData wav = reader.read(wav_filename);
        PitchExtractor pitch_extractor = new PitchExtractor(wav);
        IntensityExtractor intensity_extractor = new IntensityExtractor(wav);
        SpectrumExtractor spectrum_extractor = new SpectrumExtractor(wav);
        List<TimeValuePair> pitch_values = null;
        try {
          AuToBIUtils.log("Reading words from: " + filename);
          List<Word> words = tg_reader.readWords();

          
          AuToBIUtils.log("Extracting acoustic information.");

          pitch_values = pitch_extractor.soundToPitch();
          List<TimeValuePair> intensity_values = intensity_extractor.soundToIntensity();
          Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);

          SpeakerNormalizationParameter norm_params =
              SpeakerNormalizationParameterGenerator.readSerializedParameters(norm_param_filename);

          // If stored normalization data is unavailable generate normalization data from the input file.
          if (norm_params == null) {
            norm_params = new SpeakerNormalizationParameter();
            norm_params.insertPitch(pitch_values);
            norm_params.insertIntensity(intensity_values);
          }
          autobi.registerAllFeatureExtractors(pitch_values, intensity_values, spectrum, wav, norm_params);

          IntonationalPhraseBoundaryDetectionFeatureSet current_fs =
              new IntonationalPhraseBoundaryDetectionFeatureSet();
          current_fs.setDataPoints(words);
                                                                           
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
