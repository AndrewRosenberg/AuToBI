/*  ContourQuantizer.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;

/**
 * ContourQuantizer is used to quantize a Contour object into a specified number of time and value bins.
 * <p/>
 * If a contour has fewer points than time bins, the ContourQuantizer cannot quantize the data.
 */
public class ContourQuantizer {

  public int time_bins;  // the number of time bins
  public int value_bins;  // the number of value bins

  public double max_value;  // the upper limit of the values
  public double min_value;  // the lower limit of the values

  /**
   * Constructs a new ContourQuantizer.
   *
   * @param time_bins  the number of time bins
   * @param value_bins the number of value bins
   * @param min_value  the lower limit of the values
   * @param max_value  the upper limit of the values
   */
  public ContourQuantizer(int time_bins, int value_bins, double min_value, double max_value) {
    this.time_bins = time_bins;
    this.value_bins = value_bins;
    this.min_value = min_value;
    this.max_value = max_value;
  }

  /**
   * Quantizes a contour.
   *
   * @param c the contour
   * @return a quantized contour
   * @throws ContourQuantizerException if there are not enough data points in the contour to quantize it.
   */
  public int[] quantize(Contour c) throws ContourQuantizerException {
    if (c.size() < time_bins) {
      throw new ContourQuantizerException(
          "Cannot construct a quantized contour with fewer than time_bins values. time bins:" + time_bins +
              ", contour size: " + c.size());
    }
    int[] qc = new int[time_bins];

    int index = 0;
    int count = 0;
    double sum = 0;
    for (int i = 0; i < c.size(); ++i) {
      if (Math.floor(((double) i / c.size()) * time_bins) > index) {
        double mean = sum / count;
        int value = (int) Math.round((mean - min_value) / (max_value - min_value));
        value = value < 0 ? 0 : value >= value_bins ? value_bins - 1 : value;

        qc[index] = value;
        index = (int) Math.floor(((double) i / c.size()) * time_bins);
        sum = 0;
        count = 0;
      }
      sum += c.get(i);
      ++count;
    }
    int value = (int) Math.round((sum / count - min_value) / (max_value - min_value));
    value = value < 0 ? 0 : value >= value_bins ? value_bins - 1 : value;
    qc[index] = value;

    return qc;
  }
}
