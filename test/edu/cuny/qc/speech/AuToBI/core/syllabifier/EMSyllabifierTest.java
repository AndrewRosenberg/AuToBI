/**
 EMSyllabifierTest.java

 Copyright 2013 Andrew Rosenberg
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
package edu.cuny.qc.speech.AuToBI.core.syllabifier;


import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test class for EMSyllabifier class.
 *
 * @see EMSyllabifier
 */
public class EMSyllabifierTest {
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private EMSyllabifier ems;

  @Before
  public void setup() {
    ems = new EMSyllabifier();
  }

  @Test
  public void testIntersectionWorksWithEqualVariance() {
    GMMComponent m1 = new GMMComponent(0., 1.);
    GMMComponent m2 = new GMMComponent(4., 1.);

    double x = ems.intersection(m1, m2);

    assertEquals(2.0, x, 0.0001);
  }

  @Test
  public void testIntersectionWorksWithUnequalVariance() {
    GMMComponent m1 = new GMMComponent(0., 1.);
    GMMComponent m2 = new GMMComponent(4., 0.5);

    double x = ems.intersection(m1, m2);

    assertEquals(2.28220784038, x, 0.0001);
  }

  @Test
  public void testIntersectionWorksWithEqualVarianceAndUnequalWeights() {
    GMMComponent m1 = new GMMComponent(0., 1.);
    GMMComponent m2 = new GMMComponent(4., 1.);
    m2.setWeight(2.);

    double x = ems.intersection(m1, m2);

    assertEquals(1.82671320486, x, 0.0001);
  }

  @Test
  public void testIntersectionWorksWithUnequalVarianceAndUnequalWeights() {
    GMMComponent m1 = new GMMComponent(0., 1.);
    GMMComponent m2 = new GMMComponent(4., 2.);
    m2.setWeight(2.);

    double x = ems.intersection(m1, m2);

    assertEquals(1.53296535674, x, 0.0001);
  }


  @Test
  public void testIntersectionReturnNaNWhenNoInternalIntersectionExists() {
    GMMComponent m1 = new GMMComponent(0., 1.);
    GMMComponent m2 = new GMMComponent(0.1, 2.);

    double x = ems.intersection(m1, m2);

    assertTrue(Double.isNaN(x));
  }

  @Test
  public void testSyllabifierIDsSyllables() {
    WavReader r = new WavReader();
    WavData wav = null;
    try {
      wav = r.read(TEST_DIR + "/bdc-test.wav");
    } catch (UnsupportedAudioFileException e) {
      fail();
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
    List<Region> regions = ems.generatePseudosyllableRegions(wav);

    // Note: there are 12 true syllables in the file.  If the approach is tuned to become more accurate,
    // the expected value may change.
    assertEquals(15, regions.size());
  }

  @Test
  public void testSyllabifierIDsSyllablesThatStartAndEndAtReasonablePoints() {
    WavReader r = new WavReader();
    WavData wav = null;
    try {
      wav = r.read(TEST_DIR + "/bdc-test.wav");
    } catch (UnsupportedAudioFileException e) {
      fail();
    } catch (IOException e) {
      fail();
    } catch (AuToBIException e) {
      fail();
    }
    List<Region> regions = ems.generatePseudosyllableRegions(wav);

    // Make sure that no syllable is marked before 4.3 seconds -- this is all silence
    assertTrue(4.3 < regions.get(0).getStart());

    // Make sure that no syllable is marked after 6.8 seconds -- this is all silence
    assertTrue(6.8 > regions.get(regions.size() - 1).getEnd());
  }

  @Test
  public void testCalculateLikelihoodWithOneComponentAndOnePointContour() {
    GMMComponent m1 = new GMMComponent(1, 1.);
    List<GMMComponent> gmm = new ArrayList<GMMComponent>();
    gmm.add(m1);

    Contour c = new Contour(1, 0.5, new double[]{10});

    double ll = ems.calcLikelihood(gmm, c);
    double expected_ll = Math.log(m1.calcLikelihood(1.));
    assertEquals(expected_ll, ll, 0.0001);
  }

  @Test
  public void testCalculateLikelihoodWithOneComponentAndOneWeightedPointContour() {
    GMMComponent m1 = new GMMComponent(1, 1.);
    List<GMMComponent> gmm = new ArrayList<GMMComponent>();
    gmm.add(m1);

    Contour c = new Contour(1, 0.5, new double[]{20});

    double ll = ems.calcLikelihood(gmm, c);
    double expected_ll = Math.log(m1.calcLikelihood(1.));
    assertEquals(expected_ll, ll, 0.0001);
  }
}
