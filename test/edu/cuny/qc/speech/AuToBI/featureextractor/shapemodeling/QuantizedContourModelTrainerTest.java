/*  QuantizedContourModelTrainerTest.java

    Copyright 2009-2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.ConditionalDistribution;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * A Test class for QuantizedContourModelTrainer.
 *
 * @see QuantizedContourModelTrainer
 */
public class QuantizedContourModelTrainerTest {
  @Test
  public void testConstructor() {
    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.0);

    assertEquals(5, trainer.time_bins);
    assertEquals(2, trainer.value_bins);
    assertEquals(0.0, trainer.omit_rate, 0.0001);
  }

  @Test
  public void testIdentifyLimitsNoOmit() {
    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.0);
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5});
    Contour c1 = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5});
    Contour c2 = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5});
    List<Contour> contours = new ArrayList<Contour>();
    contours.add(c);
    contours.add(c1);
    contours.add(c2);

    Pair<Double, Double> limits = trainer.identifyLimits(contours);
    assertEquals(0.0, limits.first, 0.00001);
    assertEquals(0.5, limits.second, 0.00001);
  }

  @Test
  public void testIdentifyLimits() {
    // Omitting the top and bottom 20% is pretty extreme, but it works for a test
    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.2);
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.2, 0.3, 0.4});
    Contour c1 = new Contour(2.0, 0.001, new double[]{-10.0, 0.1, 0.2, 0.3, 0.7});
    Contour c2 = new Contour(2.0, 0.001, new double[]{-50.0, 0.1, 0.2, 0.3, 1.4});
    List<Contour> contours = new ArrayList<Contour>();
    contours.add(c);
    contours.add(c1);
    contours.add(c2);

    Pair<Double, Double> limits = trainer.identifyLimits(contours);
    assertEquals(0.1, limits.first, 0.00001);
    assertEquals(0.3, limits.second, 0.00001);
  }

  @Test
  public void testTrainProducedCorrectModelSize() {
    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.2);
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.1, 0.1, 0.4});
    Contour c1 = new Contour(2.0, 0.001, new double[]{-10.0, 0.2, 0.2, 0.2, 0.7});
    Contour c2 = new Contour(2.0, 0.001, new double[]{-50.0, 0.3, 0.3, 0.3, 1.4});
    List<Contour> contours = new ArrayList<Contour>();
    contours.add(c);
    contours.add(c1);
    contours.add(c2);

    QuantizedContourModel qcm = trainer.train(contours);

    assertEquals(5, qcm.time_models.length);
  }

  @Test
  public void testTrainProducedCorrectModels() {
    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.2);
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.1, 0.1, 0.4});
    Contour c1 = new Contour(2.0, 0.001, new double[]{-10.0, 0.2, 0.2, 0.2, 0.7});
    Contour c2 = new Contour(2.0, 0.001, new double[]{-50.0, 0.3, 0.3, 0.3, 1.4});
    List<Contour> contours = new ArrayList<Contour>();
    contours.add(c);
    contours.add(c1);
    contours.add(c2);

    QuantizedContourModel qcm = trainer.train(contours);

    ConditionalDistribution cd = qcm.time_models[0];
    assertTrue(cd.containsKey(""));
    assertEquals(1.0, cd.get("").get("0"), 0.00001);

    cd = qcm.time_models[1];
    assertTrue(cd.containsKey("0"));
    assertEquals(0.3333333, cd.get("0").get("0"), 0.00001);
    assertEquals(0.6666667, cd.get("0").get("1"), 0.00001);

    cd = qcm.time_models[2];
    assertTrue(cd.containsKey("0"));
    assertEquals(1.0, cd.get("0").get("0"), 0.00001);
    assertTrue(cd.containsKey("1"));
    assertEquals(1.0, cd.get("1").get("1"), 0.00001);

    cd = qcm.time_models[3];
    assertTrue(cd.containsKey("0"));
    assertEquals(1.0, cd.get("0").get("0"), 0.00001);
    assertTrue(cd.containsKey("1"));
    assertEquals(1.0, cd.get("1").get("1"), 0.00001);

    cd = qcm.time_models[4];
    assertTrue(cd.containsKey("0"));
    assertEquals(1.0, cd.get("0").get("1"), 0.00001);
    assertTrue(cd.containsKey("1"));
    assertEquals(1.0, cd.get("1").get("1"), 0.00001);


  }
}


