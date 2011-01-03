/*  DurationFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * DurationFeatureExtractor extracts features related to the length of the regions.
 * <p/>
 * The extracted features include the duration in seconds, the duration normalized by the duration of surrounding words,
 * and the length and presence of any following silences.
 */
public class DurationFeatureExtractor extends FeatureExtractor {
  // Gaps between regions greater than this length are considered silent.
  private static final Double SILENCE_THRESHOLD = 0.01;

  // The context regions to use for normalization.
  private List<ContextDesc> contexts;

  /**
   * Constructs a DurationFeatureExtractor.
   */
  public DurationFeatureExtractor() {
    super();

    contexts = new ArrayList<ContextDesc>();
    contexts.add(new ContextDesc("f2b2", 2, 2));
    contexts.add(new ContextDesc("f2b1", 2, 1));
    contexts.add(new ContextDesc("f2b0", 2, 0));
    contexts.add(new ContextDesc("f1b2", 1, 2));
    contexts.add(new ContextDesc("f0b2", 0, 2));
    contexts.add(new ContextDesc("f0b1", 0, 1));
    contexts.add(new ContextDesc("f1b0", 1, 0));
    contexts.add(new ContextDesc("f1b1", 1, 1));

    extracted_features.add("duration__duration");
    extracted_features.add("duration__prevPause");
    extracted_features.add("duration__follPause");
    extracted_features.add("nominal_followsSilence");
    extracted_features.add("nominal_precedesSilence");

    for (ContextDesc context : contexts) {
      extracted_features.add("duration__duration_" + context.getLabel() + "__zNorm");
      extracted_features.add("duration__duration_" + context.getLabel() + "__rNorm");
    }
  }

  /**
   * Extracts duration features from the data points.
   *
   * @param data_points the data points
   * @throws FeatureExtractorException if the regions overlap
   */
  public void extractFeatures(List data_points) throws FeatureExtractorException {
    if (data_points.size() == 0)
      return;
    if (!(data_points.get(0) instanceof Region)) {
      return;
    }

    String feature_prefix = "duration__";

    Double previous_pause;
    Double following_pause;
    Double max_pauselen = 120.0;
    Double epsilon = 0.001;
    for (int i = 0; i < data_points.size(); ++i) {
      if (i < data_points.size() - 1) {
        following_pause = Math.min(max_pauselen, ((Region) data_points.get(i + 1)).getStart() -
                                                 ((Region) data_points.get(i)).getEnd());
      } else {
        following_pause = max_pauselen;
      }
      if (i > 0) {
        previous_pause = Math.min(max_pauselen, ((Region) data_points.get(i)).getStart() -
                                                ((Region) data_points.get(i - 1)).getEnd());
      } else {
        previous_pause = max_pauselen;
      }
      if (previous_pause < 0.0) {
        if (previous_pause > -epsilon) {
          previous_pause = 0.0;
        } else {
          AuToBIUtils.warn(
              "Invalid previous pause value on word: " + data_points.get(i).toString());
        }
      }

      if (following_pause < 0.0) {
        if (following_pause > -epsilon) {
          following_pause = 0.0;
        } else {
          AuToBIUtils.warn(
              "Invalid following pause value on word: " + data_points.get(i).toString());
        }
      }

      Region region = (Region) data_points.get(i);

      region.setAttribute(feature_prefix + "duration", (
          region.getEnd() - region.getStart()));
      region.setAttribute(feature_prefix + "prevPause", previous_pause);
      region.setAttribute(feature_prefix + "follPause", following_pause);
      if (following_pause > SILENCE_THRESHOLD) {
        region.setAttribute("nominal_precedesSilence", "TRUE");
      } else {
        region.setAttribute("nominal_precedesSilence", "FALSE");
      }
      if (previous_pause > SILENCE_THRESHOLD) {
        region.setAttribute("nominal_followsSilence", "TRUE");
      } else {
        region.setAttribute("nominal_followsSilence", "FALSE");
      }
    }

    for (ContextDesc c : contexts) {
      ContextNormalizedFeatureExtractor cnfe = new ContextNormalizedFeatureExtractor(feature_prefix + "duration", c);
      cnfe.extractFeatures(data_points);
    }
  }
}
