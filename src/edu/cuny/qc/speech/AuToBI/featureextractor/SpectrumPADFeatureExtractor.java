/*  SpectrumPADFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.List;

/**
 * SpectrumPADFeatureExtractor is a FeatureExtractor which generates hypothesized pitch accent detection (PAD)
 * prediction hypotheses given a particular spectral region.
 * <p/>
 * This feature extractor requires the association of an externally trained pitch accent detection classifier and a
 * descriptor of the spectral region -- here typically in terms of a low and high bark boundary.
 */
@SuppressWarnings("unchecked")
public class SpectrumPADFeatureExtractor extends FeatureExtractor {

  private final String ACCENTED_VALUE = "ACCENTED";  // a label for ACCENTED words
  private int low;                                   // the bottom of the spectral region (in bark)
  private int high;                                  // the top of the spectral region (in bark)
  private AuToBIClassifier classifier;               // the classifier responsible for generating predictions
  private FeatureSet fs;                             // a featureSet to describe the features requied by the classifier

  /**
   * Constructs a new SpectrumPADFeatureExtractor given a spectral region, externally trained classifier, and AuToBI
   * object
   *
   * @param low        the low boundary of the spectral region -- in bark units
   * @param high       the high boundary of the spectral region
   * @param classifier the classifier responsible for generating hypotheses
   * @param fs         the AuToBI object to guide feature extraction
   */
  public SpectrumPADFeatureExtractor(int low, int high, AuToBIClassifier classifier, FeatureSet fs) {
    this.low = low;
    this.high = high;
    this.classifier = classifier;
    this.fs = fs;

    extracted_features.add("nominal_bark_" + low + "_" + high + "__prediction");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence_accented");

    required_features.addAll(fs.getRequiredFeatures());
    required_features.add(fs.getClassAttribute());
  }

  /**
   * Generates hypothesed pitch accent detection predictions for each region and stores these with the regions.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   *          if something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Construct a feature set.
    FeatureSet feature_set = fs.newInstance();

    // Extract spectrum features.
    feature_set.setDataPoints((List<Word>) regions);
    feature_set.constructFeatures();

    for (Word w : (List<Word>) regions) {
      try {
        Distribution result = classifier.distributionForInstance(w);

        w.setAttribute("nominal_bark_" + low + "_" + high + "__prediction", result.getKeyWithMaximumValue());
        w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence",
            result.get(result.getKeyWithMaximumValue()));
        w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence_accented", result.get(ACCENTED_VALUE));
      } catch (Exception e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}
