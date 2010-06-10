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

import weka.classifiers.functions.SMO;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

/**
 * PhraseAccentBoundaryToneClassificationTrainer is responsible for training and serializing a classfier to distinguish
 * pairs of phrase accents and boundary tones.
 *
 * Only intonational phrase final words are used in this classification.  That is, the destection of phrase boundaries
 * is distinct from the classification of phrase ending tones.
 */
public class PhraseAccentBoundaryToneClassificationTrainer {

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    WavReader reader = new WavReader();

    PhraseAccentBoundaryToneClassificationFeatureSet fs = new PhraseAccentBoundaryToneClassificationFeatureSet();

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
          List<Word> tmp_words = tg_reader.readWords();

          List<Word> words = new ArrayList<Word>();
          for (Word w : tmp_words) {
            if (w.isIntonationalPhraseFinal())
              words.add(w);
          }

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

          PhraseAccentBoundaryToneClassificationFeatureSet current_fs =
              new PhraseAccentBoundaryToneClassificationFeatureSet();
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
      AuToBIClassifier classifier = new WekaClassifier(new SMO());

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