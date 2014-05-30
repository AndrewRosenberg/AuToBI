/*  FeatureExtractor.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * An abstract base class for extracting features from region objects.
 * <p/>
 * FeatureExtractors are responsible for describing the features the extract and the other features they require for
 * execution.
 * <p/>
 * * The extracted features are maintained in a list that can be accessed through getExtractedFeatures().
 * <p/>
 * The required features are maintained in a Set that can be accessed through getRequiredFeatures().
 * <p/>
 * (Below is support for the version 1.4 feature extraction routines.)
 * Each FeatureExtractor (member of the featureextractor package) must include a "static final String moniker" field
 * containing
 * the short of the feature which it extracts.  This will be a name like 'delta' or 'log' for extractors that calculate
 * a delta or log contour.  This is in contrast with extracted_features and required_features which include the complete
 * name of the features extracted by a particular _instance_ of the feature extractor, like 'delta[log[f0]]'
 * <p/>
 * Each FeatureExtractor is expected to have a constructor which takes 0-N String arguments.  For those
 * FeatureExtractors that take primitives like ints or bools, the FeatureExtractor is expected to provide that
 * conversion functionality.  This constructor is required to effectively parse "moniker1[moniker2,primitive1,
 * moniker3]"..
 * This allows for constructors to be correctly called based a feature descriptor string.  Relatedly,
 * the extracted features
 * names should follow the convention of "moniker" for primitive features, and "moniker[param1,...,
 * paramN]" for derived features.
 * <p/>
 */
public abstract class FeatureExtractor {
  protected List<String> extracted_features;  // The extracted features.
  protected Set<String> required_features;    // The required features.

  /**
   * Extracts the registered features for each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException If something goes wrong.
   */
  public abstract void extractFeatures(List regions) throws FeatureExtractorException;

  /**
   * Retrieves a list of extracted features.
   *
   * @return extracted features
   */
  public List<String> getExtractedFeatures() {
    return extracted_features;
  }

  /**
   * Retrieves a list of required features.
   *
   * @return required features
   */
  public Set<String> getRequiredFeatures() {
    return required_features;
  }

  /**
   * Constructs a new FeatureExtractor and initializes extracted and required feature storage objects.
   */
  public FeatureExtractor() {
    extracted_features = new ArrayList<String>();
    required_features = new HashSet<String>();
  }
}
