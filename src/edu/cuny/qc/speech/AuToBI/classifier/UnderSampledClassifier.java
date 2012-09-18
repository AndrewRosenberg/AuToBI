/*  UnderSampledClassifier.java

    Copyright (c) 2012 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.util.PartitionUtils;

/**
 * UnderSampledClassifier downsamples majority class tokens before training.
 */
public class UnderSampledClassifier extends AuToBIClassifier {
  private AuToBIClassifier classifier;
  private String class_attribute;

  public UnderSampledClassifier(AuToBIClassifier classifier, String class_attribute) {
    this.classifier = classifier;
    this.class_attribute = class_attribute;
  }

  public Distribution distributionForInstance(Word testing_point) throws Exception {
    return classifier.distributionForInstance(testing_point);
  }

  public void train(FeatureSet feature_set) throws Exception {

    FeatureSet undersampled = feature_set.newInstance();
    undersampled.setDataPoints(PartitionUtils.performUnderSampling(feature_set.getDataPoints(), class_attribute));
    classifier.train(undersampled);
  }

  public AuToBIClassifier newInstance() {
    return new UnderSampledClassifier(classifier.newInstance(), class_attribute);
  }
}
