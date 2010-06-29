/*  TimeValuePairUtils.java

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
package edu.cuny.qc.speech.AuToBI;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * TimeValuePairsUtils is a utility class to store static methods relating to TimeValuePairs.
 */
public class TimeValuePairUtils {

  /**
   * Aligns a list of values to regions.
   * <p/>
   * This runs in O(n) but requires that the regions are non-overlapping and strictly increasing.
   *
   * @param regions      The regions to accept values
   * @param values       The values to align
   * @param feature_name The feature name to store the values on the regions
   */
  public static void assignValuesToOrderedRegions(List regions, List<TimeValuePair> values, String feature_name) {
    if (regions.size() == 0)
      return;
    int i = 0;
    List<TimeValuePair> attr = new ArrayList<TimeValuePair>();
    Region current = (Region) regions.get(i);
    current.setAttribute(feature_name, attr);

    for (TimeValuePair tvp : values) {
      while (tvp.getTime() > current.getEnd()) {
        i++;
        if (i >= regions.size()) break;
        current = (Region) regions.get(i);
        attr = new ArrayList<TimeValuePair>();
        current.setAttribute(feature_name, attr);
      }
      if ((tvp.getTime() > current.getStart()) && (tvp.getTime() <= current.getEnd())) {
        attr.add(tvp);
      }
    }
    while (i < regions.size()) {
      current = (Region) regions.get(i);
      current.setAttribute(feature_name, new ArrayList<TimeValuePair>());
      ++i;
    }
  }

  /**
   * Aligns a list of values to regions.
   * <p/>
   * This runs in O(n log n) and allows regions to be out of order or overlapping.
   * <p/>
   * The values list must be ordered.
   *
   * @param regions      The regions to accept values
   * @param values       The values to align
   * @param feature_name The feature name to store the values on the regions
   * @throws AuToBIException If the start and end times of a region are inconsistent. (I.e., start > end)
   */
  public static void assignValuesToRegions(List<Region> regions, List<TimeValuePair> values, String feature_name)
      throws AuToBIException {
    for (Region r : regions) {
      List<TimeValuePair> values_subset = TimeValuePairUtils.getTimeValuePairSublist(values, r.getStart(), r.getEnd());
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
  public static void assignValuesToSubregions(List<Region> subregions, List<Region> regions, String feature_name)
      throws AuToBIException {
    List<TimeValuePair> values = new ArrayList<TimeValuePair>();
    
    for (Region r : regions) {
      if (r.hasAttribute(feature_name)) {
        values.addAll((Collection<? extends TimeValuePair>) r.getAttribute(feature_name));
      }
    }
    for (Region r : subregions) {
      List<TimeValuePair> values_subset = TimeValuePairUtils.getTimeValuePairSublist(values, r.getStart(), r.getEnd());
      r.setAttribute(feature_name, values_subset);
    }
  }

  /**
   * Return a sublist of a time value pair list given by desired start and end times
   *
   * @param tvp   the list of TimeValuePairs
   * @param start the start time
   * @param end   the end time
   * @return the sublist of tvp containing all TimeValuePair objects with time greater than start and less than end
   * @throws AuToBIException if start is greater than end
   */
  static List<TimeValuePair> getTimeValuePairSublist(List<TimeValuePair> tvp, double start, double end)
      throws AuToBIException {
    if (start > end) {
      throw new AuToBIException("start (" + start + ") greater than end (" + end + ")");
    }
    if (tvp == null)
      throw new AuToBIException("Null TimeValuePair list.");
    if (tvp.size() == 0) return null;
    int start_idx = getIndex(tvp, start, true);
    int end_idx = getIndex(tvp, end, false);
    return new ArrayList<TimeValuePair>(tvp.subList(start_idx, end_idx + 1));
  }

  /**
   * Search for time in a list of TimeValuePairs.
   * <p/>
   * Uses a binary search O(log(N)), requiring that the list, tvp, is sorted by time.
   * <p/>
   * if greater_than is true, return the index of the first value greater than time if false, return the index of the
   * last value lesser than time
   *
   * @param tvp          a list of TimeValuePair objects to search
   * @param time         the time to search for
   * @param greater_than Whether to return the closest index greater than or less than time
   * @return the index into tvp obtained by searching for time
   */
  static int getIndex(List<TimeValuePair> tvp, Double time, boolean greater_than) {

    int bottom = 0;
    int top = tvp.size() - 1;

    if (tvp.get(bottom).getTime() > time) return bottom;
    if (tvp.get(top).getTime() < time) return top;

    while (!tvp.get(bottom).getTime().equals(time) && !tvp.get(top).getTime().equals(time) && top - bottom > 1) {
      int middle = Math.round((bottom + top) / 2);
      if (time < tvp.get(middle).getTime()) {
        top = middle;
      } else {
        bottom = middle;
      }
    }

    if (tvp.get(bottom).getTime().equals(time)) {
      return bottom;
    }

    if (tvp.get(top).getTime().equals(time)) {
      return top;
    }

    if (greater_than) {
      return top;
    } else {
      return bottom;
    }
  }

  /**
   * Returns the index of the maximum value in the contour.
   *
   * @param contour the contour of analyze
   * @return the index of the maximum value
   */
  public static int getIndexOfMaximum(List<TimeValuePair> contour) {
    int max_idx = 0;
    double max_value = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < contour.size(); ++i) {
      if (contour.get(i).getValue() > max_value) {
        max_value = contour.get(i).getValue();
        max_idx = i;
      }
    }
    return max_idx;
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
  public static int getIndexOfPrecedingMinimum(List<TimeValuePair> contour, int idx, Double threshold) {
    int previous_minimum = idx;
    double previous_value = contour.get(idx).getValue();
    double previous_maximum_value = contour.get(idx).getValue();
    boolean falling = false;
    boolean rising = false;
    for (int i = idx - 1; i > 0; --i) {
      if (contour.get(i).getValue() > previous_value) {

        // In a valley.  If the valley was sufficiently deep from previous peak, store the value.
        if (falling && previous_maximum_value - previous_value > threshold) {
          previous_minimum = i + 1;
        }
        rising = true;
        falling = false;
      }

      if (contour.get(i).getValue() < previous_value) {

        // At a peak
        if (rising) {
          // If the peak is sufficiently high from the previous stored valley, return the valley.
          if (previous_value - contour.get(previous_minimum).getValue() > threshold) {
            return previous_minimum;
          }

          // Save the height of the peak to compare against the next potential valley.
          previous_maximum_value = previous_value;
        }
        falling = true;
        rising = false;
      }

      previous_value = contour.get(i).getValue();
    }

    if (previous_maximum_value > 0) {
      // There was previous maximum value, take the corresponding minimum prior to that
      return previous_minimum;
    } else {
      // There was no local maxima in the contour (apart from the starting point) return the initial point.
      return 0;
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
  public static int getIndexOfFollowingMinimum(List<TimeValuePair> contour, int idx, Double threshold) {
    int previous_minimum = idx;
    double previous_value = contour.get(idx).getValue();
    double previous_maximum_value = contour.get(idx).getValue();
    boolean falling = false;
    boolean rising = false;
    for (int i = idx + 1; i < contour.size(); ++i) {
      if (contour.get(i).getValue() > previous_value) {

        // In a valley.  If the valley was sufficiently deep from previous peak, store the value.
        if (falling && previous_maximum_value - previous_value > threshold) {
          previous_minimum = i - 1;
        }
        rising = true;
        falling = false;
      }

      if (contour.get(i).getValue() < previous_value) {

        // At a peak
        if (rising) {
          // If the peak is sufficiently high from the previous stored valley, return the valley.
          if (previous_value - contour.get(previous_minimum).getValue() > threshold) {
            return previous_minimum;
          }

          // Save the height of the peak to compare against the next potential valley.
          previous_maximum_value = previous_value;
        }
        falling = true;
        rising = false;
      }

      previous_value = contour.get(i).getValue();
    }

    if (previous_maximum_value > 0) {
      // There was previous maximum value, take the corresponding minimum prior to that
      return previous_minimum;
    } else {
      // There was no local maxima in the contour (apart from the starting point) return the ending point.
      return contour.size() - 1;
    }
  }

}
