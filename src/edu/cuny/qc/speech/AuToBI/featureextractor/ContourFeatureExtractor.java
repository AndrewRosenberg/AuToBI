/*  ContourFeatureExtractor.java

    Copyright 2009-2012 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * ContourExtractor extracts aggregations from a list of TimeValuePairs.
 */
public class ContourFeatureExtractor extends FeatureExtractor {

  protected String attribute_name;  // the name of the feature name to analyze

  private List<ContextNormalizedFeatureExtractor> cnfes;  // associated context normalized feature extractors

  /**
   * Constructs a new ContourFeatureExtractor with associated values and attribute name.
   * <p/>
   * The attribute name often matches the the stored SpeakerNormalizationParameter identifiers, e.g. "f0"
   *
   * @param attribute_name the attribute_name
   */
  public ContourFeatureExtractor(String attribute_name) {
    setAttributeName(attribute_name);
  }

  /**
   * Constructs a new empty ContourFeatureExtractor.
   */
  public ContourFeatureExtractor() {
  }

  /**
   * Sets the attribute name and registers extracted features to match the new name.
   *
   * @param attribute_name the new attribute name
   */
  public void setAttributeName(String attribute_name) {
    this.attribute_name = attribute_name;
    List<ContextDesc> contexts = new ArrayList<ContextDesc>();
    contexts.add(new ContextDesc("f2b2", 2, 2));
    contexts.add(new ContextDesc("f2b1", 2, 1));
    contexts.add(new ContextDesc("f2b0", 2, 0));
    contexts.add(new ContextDesc("f1b2", 1, 2));
    contexts.add(new ContextDesc("f0b2", 0, 2));
    contexts.add(new ContextDesc("f0b1", 0, 1));
    contexts.add(new ContextDesc("f1b0", 1, 0));
    contexts.add(new ContextDesc("f1b1", 1, 1));

    cnfes = new ArrayList<ContextNormalizedFeatureExtractor>();
    for (ContextDesc context : contexts) {
      cnfes.add(new ContextNormalizedFeatureExtractor(this.attribute_name, context));
    }

    extracted_features = new ArrayList<String>();
    extracted_features.add(this.attribute_name + "__max");
    extracted_features.add(this.attribute_name + "__min");
    extracted_features.add(this.attribute_name + "__mean");
    extracted_features.add(this.attribute_name + "__stdev");
    extracted_features.add(this.attribute_name + "__zMax");
    extracted_features.add(this.attribute_name + "__maxLocation");
    extracted_features.add(this.attribute_name + "__maxRelLocation");

    for (ContextNormalizedFeatureExtractor cnfe : cnfes) {
      extracted_features.addAll(cnfe.getExtractedFeatures());
    }

    required_features.add(this.attribute_name);
  }

  /**
   * Retrieves the attribute name.
   *
   * @return the attribute name
   */
  public String getAttributeName() {
    return attribute_name;
  }

  /**
   * Extracts acoustic aggregations of the associated contour for each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    extractFeatures(regions, regions);
  }

  /**
   * Extracts acoustic aggregations of the associated contour for each region where the context normalization is
   * calculated over a distinct set of regions from the initial aggregations.
   * <p/>
   * This distinction is useful when normalizing subword regions, like syllables, from all of the surrounding acoustic
   * material.
   *
   * @param regions         The regions to extract features from.
   * @param context_regions the regions that define context -- can be identical as regions.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions, List context_regions) throws FeatureExtractorException {
    for (Region region : (List<Region>) regions) {
      extractFeatures(region);
    }

    for (ContextNormalizedFeatureExtractor cnfe : cnfes) {
      cnfe.extractFeatures(regions, context_regions);
    }
  }

  /**
   * Extract features from a single region.
   *
   * @param region the region
   */
  private void extractFeatures(Region region) {
    if (!region.hasAttribute(attribute_name)) {
      AuToBIUtils.warn("region doesn't have attribute: " + attribute_name);
      return;
    }
    Contour contour = (Contour) region.getAttribute(attribute_name);
    double max_location = region.getStart();
    Aggregation agg = new Aggregation();
    for (Pair<Double, Double> tvp : contour) {
      if (tvp.second > agg.getMax()) {
        max_location = tvp.first;
      }
      agg.insert(tvp.second);
    }

    double mean = agg.getMean();
    double stdev = agg.getStdev();
    double duration = region.getDuration();

    if (agg.getSize() > 0) {
      region.setAttribute(attribute_name + "__max", agg.getMax());
      region.setAttribute(attribute_name + "__min", agg.getMin());
      region.setAttribute(attribute_name + "__mean", mean);
      if (Double.isNaN(stdev)) {
        region.setAttribute(attribute_name + "__stdev", 0.0);
      } else {
        region.setAttribute(attribute_name + "__stdev", stdev);
      }
      if (stdev == 0.0) {
        region.setAttribute(attribute_name + "__zMax", 0.0);
      } else {
        region.setAttribute(attribute_name + "__zMax", (agg.getMax() - mean) / stdev);
      }


      max_location -= region.getStart();
      max_location = Math.min(Math.max(max_location, 0.0), duration);
      region.setAttribute(attribute_name + "__maxLocation", max_location);
      region.setAttribute(attribute_name + "__maxRelLocation", max_location / duration);
    } else {
      region.setAttribute(attribute_name + "__max", 0.0);
      region.setAttribute(attribute_name + "__min", 0.0);
      region.setAttribute(attribute_name + "__mean", 0.0);
      region.setAttribute(attribute_name + "__stdev", 0.0);
      region.setAttribute(attribute_name + "__zMax", 0.0);

      region.setAttribute(attribute_name + "__maxLocation", 0.0);
      region.setAttribute(attribute_name + "__maxRelLocation", 0.0);
    }
  }
}
