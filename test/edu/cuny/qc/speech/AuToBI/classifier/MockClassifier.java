/*  MockClassifier.java

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

/**
 * This is an empty classifier for use in testing.
 */
public class MockClassifier extends AuToBIClassifier {
  @Override
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    return new Distribution();
  }

  @Override
  public void train(FeatureSet feature_set) throws Exception {
  }

  @Override
  public AuToBIClassifier newInstance() {
    return null;
  }
}