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
    this.file = file;
    this.target_fs = fs;
  }

  public FeatureSet call() {
    String filename = file.getFilename();
    String file_stem = filename.substring(0, filename.lastIndexOf('.'));
    String wav_filename = file_stem + ".wav";

    AuToBIWordReader reader = WordReaderUtils.getAppropriateReader(file, autobi.getParameters());

    WavReader wav_reader = new WavReader();
    WavData wav = null;
    try {
      try {
        if (autobi.getBooleanParameter("read_wav", true)) {
          wav = wav_reader.read(wav_filename);
        }
      } catch (AuToBIException e) {
        AuToBIUtils.warn("Problem reading wave file -- " + e.getMessage());
        wav = null;
      }
      AuToBIUtils.log("Reading words from: " + filename);
      List<Word> words = reader.readWords();

      for (Word w : words) {
        w.setAttribute("wav", wav);
      }

      FeatureSet current_fs = target_fs.newInstance();
      current_fs.setDataPoints(words);

      autobi.extractFeatures(current_fs);

      if (autobi.getBooleanParameter("aggressive_feature_elimination", false)) {
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
