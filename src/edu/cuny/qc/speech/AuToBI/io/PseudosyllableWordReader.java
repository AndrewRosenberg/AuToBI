/* PseudosyllableWordReader.java

  Copyright 2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.core.syllabifier.Syllabifier;
import edu.cuny.qc.speech.AuToBI.core.syllabifier.VillingSyllabifier;
import edu.cuny.qc.speech.AuToBI.featureextractor.ContourFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.IntensityFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.AlignmentUtils;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PseudosyllableWordReader uses the AuToBIWordReader mechanisms to generate "word" boundaries using acoustic based
 * pseudosyllabification from a wav file.
 * <p/>
 * The syllabified is based on the approach described in Villing et al. (2004) Automatic Blind Syllable Segmentation for
 * Continuous Speech In: Irish Signals and Systems Conference 2004, 30 June - 2 July 2004, Queens University, Belfast.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.syllabifier.Syllabifier
 * @see AuToBIWordReader
 */
public class PseudosyllableWordReader extends AuToBIWordReader {
  private WavData wav_data;   // the audio material to base the segmentation on
  private double threshold;   // the silence threshold in mean dB in the region
  private FormattedFile annotation_file;  // A file containing ToBI annotations.

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a silence threshold, threshold.
   *
   * @param wav_data  source audio material.
   * @param threshold the silence threshold in mean dB
   */
  public PseudosyllableWordReader(WavData wav_data, double threshold) {
    this.wav_data = wav_data;
    this.threshold = threshold;
    this.annotation_file = null;
  }

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a default silence threshold of 10dB.
   *
   * @param wav_data source audio material.
   */
  public PseudosyllableWordReader(WavData wav_data) {
    this.wav_data = wav_data;
    this.threshold = 25.0;
    this.annotation_file = null;
  }

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a default silence threshold of 10dB.
   *
   * @param wav_data source audio material.
   */
  public PseudosyllableWordReader(WavData wav_data, FormattedFile annotation_file) {
    this.wav_data = wav_data;
    this.threshold = 25.0;
    this.annotation_file = annotation_file;
  }

  /**
   * Gets the silence threshold value (mean db over the region)
   *
   * @return the silence threshold
   */
  public double getThreshold() {
    return threshold;
  }

  /**
   * Sets the silence threshold value  (mean db over the region)
   *
   * @param threshold the silence threshold
   */
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }


  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    Syllabifier syllabifier = new VillingSyllabifier();
    List<Region> regions = syllabifier.generatePseudosyllableRegions(wav_data);
    for (Region r : regions) {
      r.setAttribute("wav", wav_data);
    }
    List<Word> words = new ArrayList<Word>();

    // Identify silent regions by mean intensity less than the threshold.
    IntensityFeatureExtractor ife = new IntensityFeatureExtractor();
    ContourFeatureExtractor cfe = new ContourFeatureExtractor("I");
    try {
      ife.extractFeatures(regions);
      cfe.extractFeatures(regions);
    } catch (FeatureExtractorException e) {
      throw new AuToBIException("Error extracting intensity: " + e.getMessage());
    }

    double max_I = -Double.MAX_VALUE;
    for (Region r : regions) {
      if (r.hasAttribute("max[I]")) {
        max_I = Math.max(max_I, (Double) r.getAttribute("max[I]"));
      }
    }

    for (Region r : regions) {
      if ((Double) r.getAttribute("max[I]") >= max_I - threshold) {
        words.add(new Word(r.getStart(), r.getEnd(), "", "", wav_data.getFilename()));
      }
    }

    if (annotation_file != null) {
      // TODO: make this less hacky.  Refactor the Annotation reading from the word segment reading for all readers.
      if (annotation_file.format == FormattedFile.Format.BURNC) {
        BURNCReader reader = new BURNCReader(annotation_file.getFilename().replace(".ala", ""));
        List<Region> tones = reader.readTones();
        List<Region> breaks = reader.readBreaks();

        AlignmentUtils.copyToBIBreaksByTime(words, breaks);
        AlignmentUtils.copyToBITonesByIndex(words, tones);
      } else if (annotation_file.format == FormattedFile.Format.TEXTGRID) {
        TextGridReader reader = new TextGridReader(annotation_file.getFilename());
        reader.readWords();
        Tier tones_tier = reader.tones_tier;
        Tier breaks_tier = reader.breaks_tier;

        AlignmentUtils.copyToBITonesByTime(words, tones_tier.getRegions());
        AlignmentUtils.copyToBIBreaksByTime(words, breaks_tier.getRegions());
      } else {
        AuToBIUtils.error("Cannot read annotations from formats other than TextGrid or BURNC .ala files.");
      }
    }
    return words;
  }
}
