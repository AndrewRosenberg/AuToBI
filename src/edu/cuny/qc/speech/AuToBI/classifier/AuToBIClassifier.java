/*  AuToBIClassifier.java

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.Serializable;

/**
 * An abstract serializable class to store, call and train classifiers.
 */
public abstract class AuToBIClassifier implements Serializable {

  private static final long serialVersionUID = 20090507L;

  /**
   * Return the normalized posterior distribution from the classifier.
   *
   * @param testing_point The point to evaluate
   * @return a normalized posterior distribution
   * @throws Exception If something fails.
   */
  public abstract Distribution distributionForInstance(Word testing_point) throws Exception;

  /**
   * Train the classifier on the given FeatureSet
   *
   * @param feature_set The training data
   * @throws Exception If something fails
   */
  public abstract void train(FeatureSet feature_set) throws Exception;

  /**
   * Construct and return an untrained copy of the classifier.
   *
   * @return the new classifier
   */
  public abstract AuToBIClassifier newInstance();

  /**
   * Classifies a given test point.
   *
   * @param testing_point the test point
   * @return the hypothesized value
   * @throws Exception if something goes wrong.
   */
  public String classify(Word testing_point) throws Exception {
    Distribution dist = distributionForInstance(testing_point);
    if (dist == null)
      return null;
    return dist.getKeyWithMaximumValue();
  }
}
