/*  ContourUtils.java

    Copyright 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.List;
import java.util.ArrayList;

/**
 * ContourUtils is a utility class to store static methods relating to Contours.
 *
 * @see Contour
 */
public class ContourUtils {

  // Utility classes cannot be constructed.
  private ContourUtils() {
    throw new AssertionError();
  }

  /**
   * Aligns a list of contour to regions.
   * <p/>
   * This runs in O(n) but requires that the regions are non-overlapping and strictly increasing.
   *
   * @param regions      The regions to accept contour
   * @param contour      The contour to align
   * @param feature_name The feature name to store the contour on the regions
   */
  public static void assignValuesToOrderedRegions(List regions, Contour contour, String feature_name) {
    if (regions.size() == 0)
      return;
    int i = 0;
    Region current = (Region) regions.get(i);
    Contour attr = new Contour(current.getStart(), contour.getStep(),
        contour.indexFromTime(current.getDuration()) + 1);
    current.setAttribute(feature_name, attr);

    for (Pair<Double, Double> tvp : contour) {
      while (tvp.first > current.getEnd()) {
        i++;
        if (i >= regions.size()) break;
        current = (Region) regions.get(i);
        attr = new Contour(current.getStart(), contour.getStep(),
            contour.indexFromTime(current.getDuration()) + 1);

        current.setAttribute(feature_name, attr);
      }
      if ((tvp.first > current.getStart()) && (tvp.first <= current.getEnd())) {
        attr.set(tvp.first, tvp.second);
      }
    }
    while (i < regions.size()) {
      current = (Region) regions.get(i);
      current.setAttribute(feature_name, new Contour(current.getStart(), contour.getStep(), 0));
      ++i;
    }
  }

  /**
   * Aligns a list of contour to regions.
   * <p/>
   * This runs in O(n log n) and allows regions to be out of order or overlapping.
   * <p/>
   * The contour list must be ordered.
   *
   * @param regions      The regions to accept contour
   * @param contour      The contour to align
   * @param feature_name The feature name to store the contour on the regions
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          If the start and end times of a region are inconsistent. (I.e., start > end)
   */
  public static void assignValuesToRegions(List regions, Contour contour, String feature_name)
      throws AuToBIException {
    for (Region r : (List<Region>) regions) {
      Contour values_subset = ContourUtils.getSubContour(contour, r.getStart(), r.getEnd());
      r.setAttribute(feature_name, values_subset);
    }
  }

  /**
   * Aligns a list of values from regions to subregions.
   * <p/>
   * This runs in O(n log n) and allows regions to be out of order or overlapping.
   * <p/>
   * The values list must be ordered.
   *
   * @param subregions   The regions to accept values
   * @param regions      The regions to draw values from
   * @param feature_name The feature name of the values on the regions and subregions
   * @throws AuToBIException If the start and end times of a region are inconsistent. (I.e., start > end)
   */
  public static void assignValuesToSubregions(List<Region> subregions, List regions, String feature_name)
      throws AuToBIException {
    // Constructs a single contour that spans the whole list of regions.
    // To hold every value, use the smallest time step and lowest start time.
    double x0 = Double.MAX_VALUE;
    double time_step = Double.MAX_VALUE;
    double max_time = 0.0;
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature_name)) {
        Contour c = (Contour) r.getAttribute(feature_name);
        x0 = Math.min(x0, c.getStart());
        time_step = Math.min(time_step, c.getStep());
        max_time = Math.max(max_time, c.getStart() + c.size() * c.getStep());
      }
    }

    // Only bother with the assignment if there is a contour to process
    if (max_time - x0 > 0) {
      Contour contour = new Contour(x0, time_step, (int) Math.ceil((max_time - x0) / time_step));
      for (Region r : (List<Region>) regions) {
        Contour c = (Contour) r.getAttribute(feature_name);
        for (Pair<Double, Double> tvp : c) {
          contour.set(tvp.first, tvp.second);
        }
      }

      for (Region r : subregions) {
        Contour values_subset = ContourUtils.getSubContour(contour, r.getStart(), r.getEnd());
        r.setAttribute(feature_name, values_subset);
      }
    }
  }

  /**
   * Return a subcontour of a contourlist given by desired start and end times
   *
   * @param contour the contour
   * @param start   the start time
   * @param end     the end time
   * @return the subcontour of contour containing all values with time greater than start and less than end
   * @throws AuToBIException if start is greater than end
   */
  public static Contour getSubContour(Contour contour, double start, double end)
      throws AuToBIException {
    if (start > end) {
      throw new AuToBIException("start (" + start + ") greater than end (" + end + ")");
    }
    if (contour == null)
      throw new AuToBIException("Null Contour.");
    if (contour.size() == 0) return null;
    int start_idx = contour.indexFromTimeCeil(start);
    int end_idx = contour.indexFromTimeFloor(end);

    Contour subcontour = new Contour(contour.timeFromIndex(start_idx), contour.getStep(), end_idx - start_idx + 1);
    for (int i = start_idx; i <= end_idx; ++i) {
      if (contour.isEmpty(i)) {
        subcontour.setEmpty(i - start_idx);
      } else {
        subcontour.set(i - start_idx, contour.get(i));
      }
    }
    return subcontour;
  }

  /**
   * Return the index of the local minimum preceding (or including) idx. The minimum must be more than threshold below
   * the starting point.
   *
   * @param contour   The contour to analyze
   * @param idx       The starting index
   * @param threshold The minimum difference to the minimum
   * @return the index of the local minimum preceding idx
   */
  public static int getIndexOfPrecedingMinimum(Contour contour, int idx, Double threshold) {
    double previous_value = contour.get(idx);
    double maximum_value = contour.get(idx);
    boolean rising = false;
    for (int i = idx - 1; i >= 0; --i) {
      maximum_value = Math.max(previous_value, maximum_value);

      if (contour.get(i) > previous_value) {

        if (!rising && maximum_value - previous_value > threshold) {
          // Just passed a local minima.
          // If the difference between the value of this minima is sufficiently lower that the maximum, return the index
          // of the local minima
          return i + 1;
        }
        rising = true;
      }

      if (contour.get(i) < previous_value) {
        rising = false;
      }

      previous_value = contour.get(i);
    }

    if (!rising && maximum_value - contour.get(0) > threshold) {
      // The previous minimum is the start of the contour
      return 0;
    } else {
      // The previous minimum is undefined, return the starting index.
      return idx;
    }
  }

  /**
   * Return the index of the next local minimum following idx.  The minimum must be more than threshold below the
   * starting point
   *
   * @param contour   The contour to analyze
   * @param idx       The starting index
   * @param threshold A minimum difference to the minimum.
   * @return the index of the local minimum following idx
   */
  public static int getIndexOfFollowingMinimum(Contour contour, int idx, Double threshold) {
    double previous_value = contour.get(idx);
    double maximum_value = contour.get(idx);
    boolean rising = false;
    for (int i = idx + 1; i < contour.size(); ++i) {
      maximum_value = Math.max(previous_value, maximum_value);
      if (contour.get(i) > previous_value) {

        // In a valley.  If the valley was sufficiently deep from previous peak, store the value.
        if (!rising && maximum_value - previous_value > threshold) {
          return i - 1;
        }
        rising = true;
      }

      if (contour.get(i) < previous_value) {
        rising = false;
      }

      previous_value = contour.get(i);
    }

    if (!rising && maximum_value - contour.get(contour.size() - 1) > threshold) {
      // The following minimum is the end of the contour
      return contour.size() - 1;
    } else {
      // The following local mimina is undefined, return the ending point.
      return idx;
    }
  }

  /**
   * Returns the index of the maximum value in a contour.
   *
   * @param data the contour
   * @return the index of the maximum value
   */
  public static int getIndexOfMaximum(Contour data) {
    double max = -Double.MAX_VALUE;
    int max_idx = 0;
    for (int i = 0; i < data.size(); ++i) {
      if (data.get(i) > max) {
        max = data.get(i);
        max_idx = i;
      }
    }
    return max_idx;
  }

  /**
   * Performs z-score, (x-mean) / stdev, normalization of the supplied values, using the parameters in norm_params.
   *
   * @param values       The values to normalize
   * @param norm_params  the normalization parameters
   * @param feature_name The normalization feature
   * @return a normalized list of time value pairs
   */
  public static Contour zScoreNormalizeContour(Contour values, SpeakerNormalizationParameter norm_params,
                                               String feature_name) {
    Contour normalized_values = new Contour(values.getStart(), values.getStep(), values.size());

    for (int i = 0; i < values.size(); ++i) {
      if (values.isEmpty(i)) {
        normalized_values.setEmpty(i);
      } else {
        normalized_values.set(i, norm_params.normalize(feature_name, values.get(i)));
      }
    }

    return normalized_values;
  }

  /**
   * Generates a delta contour from raw data.
   * <p/>
   * The resulting points are the first order difference of subsequent values and they are placed in time between the
   * surrounding points.
   *
   * @param values The initial values
   * @return The delta values
   */
  public static Contour generateDeltaContour(Contour values) {
    Contour d_contour;
    if (values.size() > 0) {
      d_contour = new Contour(values.getStart() + values.getStep() / 2, values.getStep(), values.size() - 1);
      for (int i = 0; i < values.size() - 1; ++i) {
        if (values.isEmpty(i) || values.isEmpty(i + 1)) {
          d_contour.setEmpty(i);
        } else {
          double value = (values.get(i + 1) - values.get(i)) / values.getStep();
          d_contour.set(i, value);
        }
      }
    } else {
      d_contour = new Contour(values.getStart() + values.getStep() / 2, values.getStep(), 0);
    }

    return d_contour;
  }
}
