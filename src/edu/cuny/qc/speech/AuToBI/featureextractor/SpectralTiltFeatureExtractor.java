/*  SpectralTiltFeatureExtractor.java

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
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;

import java.util.List;
import java.util.ArrayList;

/**
 * SpectralTiltFeatureExtractor extracts spectral tilt from a each region.
 * <p/>
 * Spectral tilt, in this case, is defined as the ratio of the energy in a specified spectral region and the total
 * energy in the frame.
 */
@SuppressWarnings("unchecked")
public class SpectralTiltFeatureExtractor extends ContourFeatureExtractor {
  private String spectrum_feature;  // the spectrum of the signal

  private ContourFeatureExtractor tvpfe;
  // An associated ContourFeatureExtractor responsible for the extraction
  private Integer low;                          // The low boundary of the frequency bandwidth
  private Integer high;                         // The high boundary of the frequency bandwidth

  /**
   * Constructs a new SpectralTiltFeatureExtractor.
   *
   * @param feature_prefix   an identifier for the extracted feature -- typically "bark"
   * @param spectrum_feature the spectrum_feature of the signal
   * @param low_bark         the low boundary of the spectral region
   * @param high_bark        the high boundary of the spectral region
   */
  public SpectralTiltFeatureExtractor(String feature_prefix, String spectrum_feature, Integer low_bark,
                                      Integer high_bark) {
    super();
    this.spectrum_feature = spectrum_feature;
    this.low = low_bark;
    this.high = high_bark;
    this.attribute_name = feature_prefix;

    tvpfe = new ContourFeatureExtractor(feature_prefix + "_" + low + "_" + high);

    extracted_features = new ArrayList<String>();
    extracted_features.add(feature_prefix + "_" + low + "_" + high);
    extracted_features.addAll(tvpfe.getExtractedFeatures());

    required_features.add(spectrum_feature);
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
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   *          if something goes wrong with the feature extraction
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    try {
      for (Region r : (List<Region>) regions) {
        if (r.hasAttribute(spectrum_feature)) {
          Spectrum spectrum = (Spectrum) r.getAttribute(spectrum_feature);
          Contour spectral_tilt = spectrum.getPowerTiltContour(barkToHertz(low), barkToHertz(high), false);
          r.setAttribute(attribute_name + "_" + low + "_" + high, spectral_tilt);
        }
      }

      tvpfe.extractFeatures(regions);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}