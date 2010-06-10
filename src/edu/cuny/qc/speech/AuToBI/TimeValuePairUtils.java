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
  public static void assignValuesToRegions(List regions, List<TimeValuePair> values, String feature_name)
      throws AuToBIException {
    for (Region r : (List<Region>) regions) {
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
   * if greater_than is true, return the index of the first value greater than time if false, return the index of the last value
   * lesser than time
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
}
