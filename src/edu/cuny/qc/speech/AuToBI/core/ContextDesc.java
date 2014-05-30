/*  ContextDesc.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to describe discrete contexts.
 * <p/>
 * This is used to define windows of analysis of the form, x units forward, y units back.
 */
public class ContextDesc {
  private String label;     // a label to describe the context
  private int back;     // the number of backwards units to include
  private int forward;  // The number of forward units to include

  /**
   * Constructs a new ContextDesc.
   *
   * @param label   the label
   * @param forward the number of forward units
   * @param back    the number of backward units
   */
  public ContextDesc(String label, int forward, int back) {
    this.label = label;
    this.back = back;
    this.forward = forward;
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label.
   *
   * @param label the label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets the back context.
   *
   * @return back
   */
  public int getBack() {
    return back;
  }

  /**
   * Sets the back context.
   *
   * @param back the back context
   */
  public void setBack(int back) {
    this.back = back;
  }

  /**
   * Gets the forward context.
   *
   * @return forward
   */
  public int getForward() {
    return forward;
  }

  /**
   * Sets the forward context.
   *
   * @param forward the forward context
   */
  public void setForward(int forward) {
    this.forward = forward;
  }

  /**
   * Parses a context descriptor string into a ContextDesc object.
   * <p/>
   * The format is f##b## where the two ## fields are the number of forward and backward words regions to include.
   *
   * @param context_desc the context description string
   * @return a ContextDesc object
   */
  public static ContextDesc parseContextDescriptor(String context_desc) throws AuToBIException {
    Pattern p = Pattern.compile("f(\\d+)b(\\d+)");
    Matcher m = p.matcher(context_desc);
    if (!m.matches()) {
      throw new AuToBIException("Pattern '" + context_desc + "' is not a valid context pattern.");
    }
    return new ContextDesc(context_desc, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
  }
}
