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
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * SpectrumBandFeatureExtractor extracts the energy from a specific frequency range.
 * <p/>
 * The frequency range is defined in bark scale.
 */
@SuppressWarnings("unchecked")
public class SpectrumBandFeatureExtractor extends ContourFeatureExtractor {
  public static final String moniker = "spectrumBand";
  private String spectrum_feature;  // the spectrum feature

  private int low;                          // the low boundary of the frequency bandwidth
  private int high;                         // the high boundary of the frequency bandwidth

  /**
   * Constructs a new SpectrumBandFeatureExtractor with associated spectrum, feature prefix and frequency region.
   *
   * @param low_bark  the bottom of the frequency region
   * @param high_bark the top of the frequency region
   */
  public SpectrumBandFeatureExtractor(int low_bark, int high_bark) {
    this.spectrum_feature = "spectrum";

    this.low = low_bark;
    this.high = high_bark;

    // register extracted features
    extracted_features = new ArrayList<String>();
    extracted_features.add("spectrumBand[" + low + "," + high + "]");

    required_features.add(spectrum_feature);
  }

  public SpectrumBandFeatureExtractor(String low_bark, String high_bark) {
    this(Integer.parseInt(low_bark), Integer.parseInt(high_bark));
  }

  /**
   * Extracts spectrum based features for each region.
   *
   * @param regions the regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<Spectrum, Contour> cache = new HashMap<Spectrum, Contour>();
    try {
      for (Region r : (List<Region>) regions) {
        if (r.hasAttribute(spectrum_feature)) {
          Spectrum spectrum = (Spectrum) r.getAttribute(spectrum_feature);
          if (cache.containsKey(spectrum)) {
            r.setAttribute("spectrumBand[" + low + "," + high + "]", cache.get(spectrum));
          } else {
            Contour spectrum_band = spectrum
                .getPowerContour(SpectralTiltFeatureExtractor.barkToHertz(low),
                    SpectralTiltFeatureExtractor.barkToHertz(high),
                    false);

            r.setAttribute("spectrumBand[" + low + "," + high + "]", spectrum_band);
            cache.put(spectrum, spectrum_band);
          }
        }
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
