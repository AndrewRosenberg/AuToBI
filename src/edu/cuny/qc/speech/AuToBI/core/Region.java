/*  Region.java

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

import java.util.*;
import java.io.Serializable;

/**
 * Region is a class to describe a region in time.  In the context of AuToBI, it is the main object that is used to
 * represent words, phrases, syllables, etc.
 * <p/>
 * Through the use of a map of strings to objects, a user is able to attach any attribute to a region, including other
 * regions.  AuToBI makes heavy use of this functionality in its feature extraction, serialization and model training.
 */
public class Region implements Serializable {
  private static final long serialVersionUID = 6410344724558496468L;
  private double start;  // the start time
  private double end;    // the end time
  private String label;  // an optional label for the region.  For words, this is typically the orthography of the word
  private String file;   // an optional field to store the path to the source file for the region.
  private Map<String, Object> attributes;  // a collection of attributes associated with the region.

  /**
   * Constrcuts a new region with a label and file.
   *
   * @param start the region start time
   * @param end   the region end time
   * @param label the label
   * @param file  the file name
   */
  public Region(double start, double end, String label, String file) {
    this.start = start;
    this.end = end;
    this.label = label;
    this.file = file;
    this.attributes = null;
  }

  /**
   * Constrcuts a new region with a label.
   *
   * @param start the region start time
   * @param end   the region end time
   * @param label the label
   */
  public Region(double start, double end, String label) {
    this(start, end, label, null);
  }

  /**
   * Constrcuts a new region without a label.
   *
   * @param start the region start time
   * @param end   the region end time
   */
  public Region(double start, double end) {
    this(start, end, null);
  }

  /**
   * Constructs a new point region.
   * <p/>
   * Point regions have the same start and end time.
   *
   * @param point the point.
   */
  public Region(double point) {
    this(point, point);
  }

  /**
   * Constructs a copy of an existing region.
   *
   * @param r the existing region
   */
  public Region(Region r) {
    start = r.start;
    end = r.end;
    label = r.label;
    file = r.file;
    if (r.attributes != null) {
      attributes = new HashMap<String, Object>(r.attributes);
    }
  }

  /**
   * Retrieves the start time of the region.
   *
   * @return the start time
   */
  public double getStart() {
    return start;
  }

  /**
   * Sets the start time of the region.
   *
   * @param start the start time.
   */
  public void setStart(double start) {
    this.start = start;
  }

  /**
   * Retrieves the end time of the region.
   *
   * @return the end time
   */
  public double getEnd() {
    return end;
  }

  /**
   * Sets the end time of the region.
   *
   * @param end the end time
   */
  public void setEnd(double end) {
    this.end = end;
  }

  /**
   * Calculates the duration of the region.
   *
   * @return the duration.
   */
  public double getDuration() {
    return getEnd() - getStart();
  }

  /**
   * Retrieves the file.
   *
   * @return the file name
   */
  public String getFile() {
    return file;
  }

  /**
   * Sets the file name.
   *
   * @param file the filename
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * Retrieves the label of the region.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label of the region.
   *
   * @param label the label
   */
  public void setLabel(String label) {
    this.label = label.trim();
  }

  /**
   * Retrieves the attributes hash.
   *
   * @return the hash of attributes and their names
   */
  public Map<String, Object> getAttributes() {
    checkMapUsage();
    return attributes;
  }

  /**
   * Sets the value for a new or existing attribute.
   *
   * @param name  the attribute name
   * @param value the new value
   */
  public void setAttribute(String name, Object value) {
    checkMapUsage();
    this.attributes.put(name, value);
  }

  /**
   * Determines if the region has a non-null attribute for a given attribute name
   *
   * @param name the attribute name
   * @return true if the attribute exists and is non-null
   */
  public Boolean hasAttribute(String name) {
    checkMapUsage();
    return this.attributes.containsKey(name) && this.attributes.get(name) != null;
  }

  /**
   * Retrieves an attribute.
   *
   * @param name the name of the requested attribute
   * @return the attribute value or null if it does not exist
   */
  public Object getAttribute(String name) {
    checkMapUsage();
    return this.attributes.get(name);
  }

  /**
   * Retrieves a set of all of the set attribute names.
   * <p/>
   * Note: an attribute name can exist in this set even if it has been assigned a null value
   *
   * @return the set of attribute names
   */
  public Set<String> getAttributeNames() {
    checkMapUsage();
    Set<String> names = new HashSet<String>();
    for (String name : this.attributes.keySet()) {
      if (getAttribute(name) != null) {
        names.add(name);
      }
    }
    return names;
  }

  /**
   * Removes an attribute from the region.
   *
   * @param name the attribute name
   */
  public void removeAttribute(String name) {
    this.attributes.remove(name);
  }

  /**
   * Removes all attributes from the region.
   */
  public void clearAttributes() {
    this.attributes.clear();
  }

  /**
   * Ensures that the attributes map is not null.
   */
  private void checkMapUsage() {
    if (attributes == null) {
      attributes = new HashMap<String, Object>();
    }
  }

  /**
   * Constructs a string representation of a Region.
   *
   * @return the string representation of the region
   */
  public String toString() {
    return label + " [" + start + ", " + end + "]" + " (" + file + ")";
  }
}
