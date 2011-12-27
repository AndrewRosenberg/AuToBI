/*  TiltParameters.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Paul Taylor's Tilt Intonation Model parameters.
 * <p/>
 * The goal is to model "intonational events".  Intonational Events are identified independently from the TILT
 * parameterization. Following identification the shape of a pitch excursion is modeled via the tilt parameters.
 * <p/>
 * There is an assumption that each intonational event contains zero or one rises followed by zero or one falls in its
 * pitch contour.  The tilt parameters model the size of this rise as well as its position within the intonational
 * event.  After describing these separately, the two values tilt_amp and tilt_dur are combined into a single tilt
 * value.
 * <p/>
 * See citation:
 * <p/>
 * \@misc{ taylor-tilt, author = "P. Taylor", title = "The Tilt Intonation Model", text = "P. Taylor. The Tilt
 * Intonation Model. ICSLP98, this volume.", url = "citeseer.ist.psu.edu/taylor98tilt.html" }
 */
public class TiltParameters {
  private double amplitude_rise = 0.0;
  private double amplitude_fall = 0.0;
  private double duration_rise = 0.0;
  private double duration_fall = 0.0;

  /**
   * Constructs a new empty TileParameters object.
   */
  public TiltParameters() {
  }

  /**
   * Constructs a new TiltParameters object that parameterizes the contour represented by data
   *
   * @param data a list of TimeValuePairs that represents the contour
   */
  public TiltParameters(Contour data) {
    calculateTilt(data);
  }

  /**
   * Retrieves the tilt value that has been calculated.
   *
   * @return the tilt value
   */
  public Double getTilt() {
    return 0.5 * getAmplitudeTilt() + 0.5 * getDurationTilt();
  }

  /**
   * Retrieves the amplitude component of the tilt parameterization
   *
   * @return the amplitude component
   */
  public Double getAmplitudeTilt() {
    if (Math.abs(amplitude_rise) + Math.abs(amplitude_fall) == 0) return 0.0;
    return (Math.abs(amplitude_rise) - Math.abs(amplitude_fall)) /
        (Math.abs(amplitude_rise) + Math.abs(amplitude_fall));
  }

  /**
   * Retrieves the duration component of the tilt parameterization
   *
   * @return the duration component
   */
  public Double getDurationTilt() {
    if (Math.abs(duration_rise) + Math.abs(duration_fall) == 0) return 0.0;
    return (Math.abs(duration_rise) - Math.abs(duration_fall)) / (Math.abs(duration_rise) + Math.abs(duration_fall));
  }

  /**
   * Calculate tilt parameters based on the supplied time value pairs
   * <p/>
   * Tilt makes the assumption that the data that it is modeling consists of a single rise and a single fall.  While
   * either the rise or fall may be null, the modeling behavior is undefined under data with multiple local maxima.
   *
   * @param data the TimeValuePair objects to calculate tilt over
   */
  public void calculateTilt(Contour data) {
    if (data == null || data.size() == 0) {
      amplitude_rise = 0;
      amplitude_fall = 0;
      duration_rise = 0;
      duration_fall = 0;
      return;
    }

    int max_index = ContourUtils.getIndexOfMaximum(data);
    int prev_min_index = ContourUtils.getIndexOfPrecedingMinimum(data, max_index, 0.5);
    int fol_min_index = ContourUtils.getIndexOfFollowingMinimum(data, max_index, 0.5);

    amplitude_rise = data.get(max_index) - data.get(prev_min_index);
    amplitude_fall = data.get(max_index) - data.get(fol_min_index);
    duration_rise = data.timeFromIndex(max_index) - data.timeFromIndex(prev_min_index);
    duration_fall = data.timeFromIndex(fol_min_index) - data.timeFromIndex(max_index);
  }
}
