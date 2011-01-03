/*  ContextDesc.java

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

/**
 * A class to describe discrete contexts.
 * <p/>
 * This is used to define windows of analysis of the form, x units forward, y units back.
 */
public class ContextDesc {
  private String label;     // a label to describe the context
  private Integer back;     // the number of backwards units to include
  private Integer forward;  // The number of forward units to include

  /**
   * Constructs a new ContextDesc.
   *
   * @param label   the label
   * @param forward the number of forward units
   * @param back    the number of backward units
   */
  public ContextDesc(String label, Integer forward, Integer back) {
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
  public Integer getBack() {
    return back;
  }

  /**
   * Sets the back context.
   *
   * @param back the back context
   */
  public void setBack(Integer back) {
    this.back = back;
  }

  /**
   * Gets the forward context.
   *
   * @return forward
   */
  public Integer getForward() {
    return forward;
  }

  /**
   * Sets the forward context.
   *
   * @param forward the forward context
   */
  public void setForward(Integer forward) {
    this.forward = forward;
  }
}
