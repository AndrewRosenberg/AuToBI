/*  SpectralTiltFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * SpectralTiltFeatureExtractor extracts spectral tilt from a each region.
 * <p/>
 * Spectral tilt, in this case, is defined as the ratio of the energy in a specified spectral region and the total
 * energy in the frame.
 */
@SuppressWarnings("unchecked")
public class SpectralTiltFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spectralTilt";
  private String spectrum_feature;  // the spectrum of the signal

  // An associated ContourFeatureExtractor responsible for the extraction
  private int low;                          // The low boundary of the frequency bandwidth
  private int high;                         // The high boundary of the frequency bandwidth

  /**
   * Constructs a new SpectralTiltFeatureExtractor.
   *
   * @param low_bark  the low boundary of the spectral region
   * @param high_bark the high boundary of the spectral region
   */
  public SpectralTiltFeatureExtractor(int low_bark, int high_bark) {
    super();
    this.spectrum_feature = "spectrum";
    this.low = low_bark;
    this.high = high_bark;

    extracted_features = new ArrayList<String>();
    extracted_features.add("spectralTilt[" + low + "," + high + "]");

    required_features.add(spectrum_feature);
  }

  public SpectralTiltFeatureExtractor(String low_bark, String high_bark) {
    this(Integer.parseInt(low_bark), Integer.parseInt(high_bark));
  }


  /**
   * Converts a bark value to a hertz value using the following formula:
   * <p/>
   * hz = 600 * sinh(bark/6)
   *
   * @param bark the bark value
   * @return the pitch value
   */
  public static double barkToHertz(double bark) {
    return 600 * Math.sinh(bark / 6);
  }

  /**
   * Extracts spectral tilt features from a set of regions.
   *
   * @param regions the regions to extract features from
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if something goes wrong with the
   *                                                                              feature extraction
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    try {
      HashMap<Spectrum, Contour> cache = new HashMap<Spectrum, Contour>();
      for (Region r : (List<Region>) regions) {
        if (r.hasAttribute(spectrum_feature)) {
          Spectrum spectrum = (Spectrum) r.getAttribute(spectrum_feature);
          if (cache.containsKey(spectrum)) {
            r.setAttribute("spectralTilt[" + low + "," + high + "]", cache.get(spectrum));
          } else {
            Contour spectral_tilt = spectrum.getPowerTiltContour(barkToHertz(low), barkToHertz(high), false);
            r.setAttribute("spectralTilt[" + low + "," + high + "]", spectral_tilt);
            cache.put(spectrum, spectral_tilt);
          }
        }
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}