/*  ContourPolynomialFitter.java

    Copyright (c) 2009-2014 Andrew Rosenberg

  This file is part of the AuToBI prosodic analysis package.

  AuToBI is free software: you can redistribute it and/or modify
  it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
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

    Pair<Matrix, Matrix> tx = constructTargetAndDataMatrix(c);
    Matrix t = tx.first;
    Matrix x = tx.second;

    Matrix w = x.transpose().times(x).inverse().times(x.transpose()).times(t);

    return w.transpose().getArray()[0];
  }

  /**
   * Calculates the mean squared error (MSE) between a contour c, and the polynomial weights given by weights.
   *
   * @param c       the contour
   * @param weights the weights
   * @return the mean squared error
   */
  public double getMSE(Contour c, double[] weights) {

    Pair<Matrix, Matrix> tx = constructTargetAndDataMatrix(c);
    Matrix t = tx.first;
    Matrix x = tx.second;

    Matrix w = new Matrix(weights.length, 1);
    for (int i = 0; i < weights.length; ++i) {
      w.set(i, 0, weights[i]);
    }
    Matrix y = x.times(w);
    double error = 0.0;
    for (int i = 0; i < y.getRowDimension(); ++i) {
      error += (y.get(i, 0) - t.get(i, 0)) * (y.get(i, 0) - t.get(i, 0));
    }

    return error;
  }

  /**
   * Constructs two matrices from a contour.
   * <p/>
   * The first is the target values, the second is the exponentiated x values.
   *
   * @param c the contour
   * @return a pair of matrices.
   */
  private Pair<Matrix, Matrix> constructTargetAndDataMatrix(Contour c) {
    Pair<Matrix, Matrix> retval = new Pair<Matrix, Matrix>();

    Matrix t = new Matrix(c.size(), 1);
    Matrix x = new Matrix(c.size(), n + 1);

    int i = 0;
    for (Pair<Double, Double> p : c) {
      t.set(i, 0, p.second);
      for (int j = 0; j <= n; ++j) {
        x.set(i, j, Math.pow(p.first, j));
      }
      ++i;
    }
    retval.first = t;
    retval.second = x;
    return retval;
  }

  /**
   * Returns the order of the polynomial that will be fit.
   *
   * @return the order
   */
  public int getOrder() {
    return n;
  }
}
