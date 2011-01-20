/*  QuantizedContourModelTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


/**
 * A Test class for Quantized contour models.
 *
 * @see QuantizedContourModel
 */
public class QuantizedContourModelTest {

  @Test
  public void testEvaluateContour() {

    QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(5, 2, 0.2);
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.1, 0.1, 0.4});
    Contour c1 = new Contour(2.0, 0.001, new double[]{-10.0, 0.2, 0.2, 0.2, 0.7});
    Contour c2 = new Contour(2.0, 0.001, new double[]{-50.0, 0.3, 0.3, 0.3, 1.4});
    List<Contour> contours = new ArrayList<Contour>();
    contours.add(c);
    contours.add(c1);
    contours.add(c2);

    QuantizedContourModel qcm = trainer.train(contours);

    try {
      double log_p = qcm.evaluateContour(c2);

      double expected = Math.log(1.0) + Math.log(0.66666) + Math.log(1.0) + Math.log(1.0) + Math.log(1.0);
      assertEquals(expected, log_p, 0.0001);
    } catch (ContourQuantizerException e) {
      e.printStackTrace();
    }

  }
}
