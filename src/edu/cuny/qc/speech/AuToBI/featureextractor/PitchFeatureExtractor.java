/*  PitchFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.PitchExtractor;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PitchFeatureExtractor extracts pitch information from a given WavData object and aligns the appropriate contour
 * sections to the supplied regions.
 */
@SuppressWarnings("ALL")
public class PitchFeatureExtractor extends FeatureExtractor {

  private String feature_name;  // the name of the feature to hold pitch information

  public PitchFeatureExtractor(String feature_name) {

    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    try {
      // Identify all regions which are associated with each wav data.
      HashMap<WavData, List<Region>> wave_region_map = new HashMap<WavData, List<Region>>();
      for (Region r : (List<Region>) regions) {
        WavData wav = (WavData) r.getAttribute("wav");
        if (wav != null) {
          if (!wave_region_map.containsKey(wav)) {
            wave_region_map.put(wav, new ArrayList<Region>());
          }
          wave_region_map.get(wav).add(r);
        }
      }

      for (WavData wav : wave_region_map.keySet()) {
        PitchExtractor extractor = new PitchExtractor(wav);
        Contour pitch_contour = extractor.soundToPitch();
        ContourUtils.assignValuesToRegions(wave_region_map.get(wav), pitch_contour, feature_name);
      }
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
  }
}
