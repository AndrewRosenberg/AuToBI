/*  SpectrumFeatureExtractor.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.SpectrumExtractor;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.HashMap;
import java.util.List;

/**
 * SpectrumFeatureExtractor extracts a spectrum from a given WavData object.
 * <p/>
 * v1.4 SpectrumFeatureExtractor has changed to attach spectra to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
@SuppressWarnings("unchecked")
public class SpectrumFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spectrum";
  private String feature_name;  // the name of the feature to hold pitch information
  private double frame_size; // The spectrum frame duration
  private double hamming_window; // The size of the hamming window used in the spectrum analysis


  /**
   * Constructs a new SpectrumFeatureExtractor to process wav_data and store the resulting Spectrum objects on
   * feature_name
   * <p/>
   * This uses a default frame size of 0.01s, and a hamming window of 0.02s.
   * <p/>
   * Deprecated as of 1.4.  The feature name is always the 'moniker', "spectrum".
   *
   * @param feature_name the feature name
   */
  @Deprecated
  public SpectrumFeatureExtractor(String feature_name) {
    this(feature_name, 0.01, 0.02);
  }

  public SpectrumFeatureExtractor() {
    this.feature_name = moniker;
    this.frame_size = 0.01;
    this.hamming_window = 0.02;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  @Deprecated
  public SpectrumFeatureExtractor(String feature_name, double frame_size, double hamming_window) {
    this.feature_name = feature_name;
    this.frame_size = frame_size;
    this.hamming_window = hamming_window;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  public SpectrumFeatureExtractor(double frame_size, double hamming_window) {
    this.feature_name = moniker;
    this.frame_size = frame_size;
    this.hamming_window = hamming_window;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  public SpectrumFeatureExtractor(String feature_name, String frame_size, String hamming_window) {
    this.feature_name = moniker + "[" + frame_size + "," + hamming_window + "]";
    this.frame_size = Double.parseDouble(frame_size);
    this.hamming_window = Double.parseDouble(hamming_window);

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  /**
   * Extracts the spectrum and aligns information to regions.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<WavData, Spectrum> cache = new HashMap<WavData, Spectrum>();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("wav")) {
        WavData wav = (WavData) r.getAttribute("wav");
        if (cache.containsKey(wav)) {
          r.setAttribute(feature_name, cache.get(wav));
        } else {
          if (wav != null) {
            SpectrumExtractor extractor = new SpectrumExtractor(wav);
            Spectrum spectrum = extractor.getSpectrum(frame_size, hamming_window);

            if (spectrum == null) {
              // AR: When writing tests, I couldn't get this case to fire. It seems unwise to remove this fail-safe
              // though. If it happens during runtime, write a test for it.
              throw new FeatureExtractorException(
                  "Tried to extract the spectrum from segment with too few frames: " + r.getDuration() +
                      " seconds. (" +
                      wav.getNumSamples() + " frames)");
            }
            r.setAttribute(feature_name, spectrum);
            cache.put(wav, spectrum);
          }
        }
      }
    }
  }
}
