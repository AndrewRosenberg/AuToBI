/*  FeatureSetPropagator.java

    Copyright (c) 2009-2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.AuToBI;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * FeatureSetPropagator allows for multithreading in reading data sets and extracting features.
 */
public class FeatureSetPropagator implements Callable<FeatureSet> {
  private AuToBI autobi;
  private FormattedFile file;
  private final FeatureSet target_fs;

  public FeatureSetPropagator(AuToBI autobi, FormattedFile file, FeatureSet fs) {
    this.autobi = new AuToBI();
    this.autobi.setParameters(autobi.getParameters());
    this.file = file;
    this.target_fs = fs;
  }

  public FeatureSet call() {
    String filename = file.getFilename();

    String file_stem = filename.substring(0, filename.lastIndexOf('.'));

    String wav_filename = file_stem + ".wav";

    AuToBIWordReader reader = null;
    switch (file.getFormat()) {
      case TEXTGRID:
        reader = new TextGridReader(filename);
        break;
      case CPROM:
        reader = new CPromTextGridReader(filename, "words", "delivery", "UTF16",
            autobi.getBooleanParameter("cprom_include_secondary", true));
        break;
      case BURNC:
        reader = new BURNCReader(filename.replace(".ala", ""));
        break;
      case SIMPLE_WORD:
        reader = new SimpleWordReader(filename);
        break;
      default:
        reader = new TextGridReader(filename);
    }

    WavReader wav_reader = new WavReader();
    WavData wav = null;
    try {
      wav = wav_reader.read(wav_filename);
    } catch (UnsupportedAudioFileException e1) {
      e1.printStackTrace();
    } catch (AuToBIException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      AuToBIUtils.log("Reading words from: " + filename);
      List<Word> words = reader.readWords();

      autobi.registerAllFeatureExtractors(wav);
      autobi.registerNullFeatureExtractor("speaker_id");

      FeatureSet current_fs = target_fs.newInstance();
      current_fs.setDataPoints(words);

      autobi.extractFeatures(current_fs);

      // Free the autobi object for garbage collection.
      autobi = null;
      return current_fs;
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }

    return null;
  }
}
