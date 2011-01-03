/*  SpectrumFeatureExtractor.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;
import edu.cuny.qc.speech.AuToBI.core.TimeValuePair;
import edu.cuny.qc.speech.AuToBI.util.TimeValuePairUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * SpectrumFeatureExtractor extracts the energy from a specific frequency range.
 * <p/>
 * The frequency range is defined in bark scale.
 */
public class SpectrumFeatureExtractor extends TimeValuePairFeatureExtractor {
  private Spectrum spectrum;  // the spectrum

  private TimeValuePairFeatureExtractor tvpfe;
  // asn associated feature extractor responsible for the feature calculation
  private Integer low;                          // the low boundary of the frequency bandwidth
  private Integer high;                         // the high boundary of the frequency bandwidth

  /**
   * Constructs a new SpectrumFeatureExtractor with an associated Spectrum object.
   *
   * @param spectrum the spectrum
   */
  public SpectrumFeatureExtractor(Spectrum spectrum) {
    super();
    this.spectrum = spectrum;
  }

  /**
   * Constructs a new SpectrumFeatureExtractor with associated spectrum, feature prefix and frequency region.
   *
   * @param feature_prefix a feature prefix for extracted features
   * @param spectrum       the spectrum to be analyzed
   * @param low_bark       the bottom of the frequency region
   * @param high_bark      the top of the frequency region
   */
  public SpectrumFeatureExtractor(String feature_prefix, Spectrum spectrum, Integer low_bark, Integer high_bark) {
    this.spectrum = spectrum;

    this.attribute_name = feature_prefix;
    this.low = low_bark;
    this.high = high_bark;

    tvpfe = new TimeValuePairFeatureExtractor(feature_prefix + "_" + low + "_" + high);

    // register extracted features
    extracted_features = new ArrayList<String>();
    extracted_features.addAll(tvpfe.extracted_features);
  }

  /**
   * Extracts spectrum based features for each region.
   *
   * @param regions the regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    try {
      // construct time value pair lists with energy regions
      List<TimeValuePair> spectrum_band = spectrum
          .getPowerList(SpectralTiltFeatureExtractor.barkToHertz(low), SpectralTiltFeatureExtractor.barkToHertz(high),
              false);
      TimeValuePairUtils.assignValuesToRegions(regions, spectrum_band, attribute_name + "_" + low + "_" + high);
      
      tvpfe.extractFeatures(regions);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
