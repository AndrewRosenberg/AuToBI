/*  FeatureSetPropagator.java

    Copyright (c) 2009-2014 Andrew Rosenberg

  This file is part of the AuToBI prosodic analysis package.

  AuToBI is free software: you can redistribute it and/or modify
  it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.AuToBI;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.io.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.WordReaderUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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
    this.autobi.setFeatureRegistry(autobi.getFeatureRegistry());
    this.autobi.setMonikerMap(autobi.getMonikerMap());
    this.file = file;
    this.target_fs = fs;
  }

  public FeatureSet call() {
    String filename = file.getFilename();
    String file_stem = filename.substring(0, filename.lastIndexOf('.'));
    String wav_filename = file_stem + ".wav";

    WavReader wav_reader = new WavReader();
    WavData wav = null;
    try {
      try {
        if (autobi.getBooleanParameter("read_wav", true)) {
          wav = wav_reader.read(wav_filename);
        }
      } catch (AuToBIException e) {
        // A misisng wav file is a problem if you're looking for it.
        throw new AuToBIException("Problem reading wave file, " + wav_filename + " -- " + e.getMessage());
      }
      AuToBIUtils.log("Reading words from: " + filename);
      AuToBIWordReader reader;
      if (autobi.getBooleanParameter("syllable_based", false)) {
        reader = new PseudosyllableWordReader(wav, file);
      } else {
        reader = WordReaderUtils.getAppropriateReader(file, autobi.getParameters());
      }
      List<Word> words = reader.readWords();

      for (Word w : words) {
        w.setAttribute("wav", wav);
      }

      FeatureSet current_fs = target_fs.newInstance();
      current_fs.setDataPoints(words);

      autobi.extractFeatures(current_fs);

      if (!autobi.getBooleanParameter("feature_preservation", false)) {
        for (Word w : current_fs.getDataPoints()) {
          Set<String> attrs = w.getAttributeNames();
          for (String attr : attrs) {
            if (!current_fs.getRequiredFeatures().contains(attr) && !attr.equals(current_fs.getClassAttribute())) {
              w.removeAttribute(attr);
            }
          }
        }
      }

      // Free the autobi object for garbage collection.
      autobi = null;
      return current_fs;
    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    }

    return null;
  }

}
