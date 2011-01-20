/*  ContourPolynomialFitter.java

    Copyright (c) 2009-2011 Andrew Rosenberg

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

import Jama.Matrix;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Pair;

/**
 * This class fits a polynomial to a contour.
 * <p/>
 * This is used to obtain the polynomial fit coefficients when modeling the shape of a pitch or intensity contour as a
 * polynomial curve.
 */
public class ContourPolynomialFitter {
  private int n;  // The degree of the polynomial to fit.

  /**
   * Constructs a new ContourPolynomialFitter with degree n.
   *
   * @param n the degree of the polynomial to fit.
   */
  public ContourPolynomialFitter(int n) {
    this.n = n;
  }

  /**
   * Calculates the polynomial coefficients of a minimum squared error fit of an n degree polynomial to the contour.
   *
   * @param c the contour
   * @return an array of polynomial coefficients
   */
  public double[] fitContour(Contour c) {

    Matrix t = new Matrix(c.size(), 1);
    Matrix x = new Matrix(c.size(), n + 1);

    int i = 0;
    for (Pair<Double, Double> p : c) {
      t.set(i, 0, p.second);
      for (int j = 0; j <=n; ++j) {
        x.set(i, j, Math.pow(p.first, j));
      }
      ++i;
    }

    Matrix w = x.transpose().times(x).inverse().times(x.transpose()).times(t);

    return w.transpose().getArray()[0];
  }

}
