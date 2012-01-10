/*  ContourCenterOfGravityFeatureExtractorTest.java

    Copyright 2012 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for ContourCenterOfGravityFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.ContourCenterOfGravityFeatureExtractor
 */
public class ContourCenterOfGravityFeatureExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {

  }

  @Test
  public void testExtractFeaturesWorksWithZeroLengthContour() {

  }

  @Test
  public void testRequiredAndExtractedAttributeNames() {
    ContourCenterOfGravityFeatureExtractor ccogfe = new ContourCenterOfGravityFeatureExtractor("attr");

    assertTrue(ccogfe.getRequiredFeatures().contains("attr"));
    assertTrue(ccogfe.getExtractedFeatures().contains("attr__cog"));
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeatures() {

  }
}
