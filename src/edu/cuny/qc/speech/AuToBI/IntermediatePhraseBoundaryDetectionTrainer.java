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

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.WekaClassifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.SNPAssignmentFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureset.IntermediatePhraseBoundaryDetectionFeatureSet;
import edu.cuny.qc.speech.AuToBI.io.TextGridReader;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import org.apache.log4j.BasicConfigurator;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

import weka.classifiers.functions.Logistic;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * IntermediatePhraseBoundaryDetectionTrainer is used to train and serialize models that distinguish (intonational
 * phrase medial) intermediate phrase boundaries from phrase internal word boundaries.
 */
public class IntermediatePhraseBoundaryDetectionTrainer {

  public static void main(String[] args) {
    BasicConfigurator.configure();
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    WavReader reader = new WavReader();

    IntermediatePhraseBoundaryDetectionFeatureSet fs = new IntermediatePhraseBoundaryDetectionFeatureSet();

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
          List<Word> tmp = tg_reader.readWords();

          AuToBIUtils.log("Extracting acoustic information.");

          Spectrum spectrum = spectrum_extractor.getSpectrum(0.01, 0.02);
          autobi.unregisterAllFeatureExtractors();
          autobi.registerAllFeatureExtractors(spectrum, wav);
          autobi.registerFeatureExtractor(new SNPAssignmentFeatureExtractor("normalization_parameters", "speaker_id",
              AuToBIUtils.glob(autobi.getOptionalParameter("normalization_parameters"))));
          autobi.registerNullFeatureExtractor("speaker_id");

          IntermediatePhraseBoundaryDetectionFeatureSet current_fs =
              new IntermediatePhraseBoundaryDetectionFeatureSet();


          current_fs.setDataPoints(tmp);

          autobi.extractFeatures(current_fs, false);
          List<Word> words = new ArrayList<Word>();
          for (Word w : tmp) {
            if (!w.isIntonationalPhraseFinal())
              words.add(w);
          }
          current_fs.setDataPoints(words);
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

      if (autobi.hasParameter("arff_file")) {
        String arff_file = autobi.getParameter("arff_file");
        AuToBIUtils.log("writing arff file to: " + arff_file);
        fs.writeArff(arff_file, "IntermediatePhraseBoundary");
      }

      AuToBIUtils.log("training classifier");
      AuToBIClassifier classifier = new WekaClassifier(new Logistic());

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
