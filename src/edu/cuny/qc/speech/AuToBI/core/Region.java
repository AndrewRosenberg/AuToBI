/*  Region.java

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

  private FeatureSet feature_set;
  // a FeatureSet that describes the features that are required on this region for classification
  private Object[] fs_attributes; // a list of values for each of the required attributes from the FeatureSet

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

    // TODO: copy featureset and fs_attributes.
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
   * <p/>
   * This has been deprecated.  The correct way to use this functionality is to get a list of attribute names, and the
   * get the values for each attribute individually.
   *
   * @return the hash of attributes and their names
   */
  @Deprecated
  public Map<String, Object> getAttributes() {
    checkMapUsage();
    return attributes;
  }

  /**
   * Retrieves the associated FeatureSet
   *
   * @return the associated FeatureSet
   */
  public FeatureSet getFeatureSet() {
    return feature_set;
  }

  /**
   * Sets the corresponding feature set, and structures the attribute storage.
   *
   * @param fs the FeatureSet to be assigned
   */
  public void setFeatureSet(FeatureSet fs) {

    // Move any attributes that were required by the previous feature set.
    Map<String, Object> attr_storage = null;
    if (this.feature_set != null) {
      attr_storage = new HashMap<String, Object>();
      for (String f : feature_set.getRequiredFeatures()) {
        attr_storage.put(f, getAttribute(f));
      }
      attr_storage.put(feature_set.getClassAttribute(), getAttribute(feature_set.getClassAttribute()));
    }
    this.feature_set = fs;

    // Allocate enough space for all of the required features and the class attribute
    fs_attributes = new Object[fs.getRequiredFeatures().size() + 1];

    if (attr_storage != null) {
      // Insert any of the previous required features back into the attribute storage.
      for (String f : attr_storage.keySet()) {
        setAttribute(f, attr_storage.get(f));
      }
    }

    // Move any previously non-required attributes to required storage.
    checkMapUsage();
    Set<String> to_move = new HashSet<String>();
    for (String f : this.attributes.keySet()) {
      if (feature_set.getRequiredFeatures().contains(f)) {
        to_move.add(f);
      }
    }

    for (String f : to_move) {
      Object value = getAttribute(f);
      removeAttribute(f);
      setAttribute(f, value);
    }
  }

  /**
   * Sets the value for a new or existing attribute.
   *
   * @param name  the attribute name
   * @param value the new value
   */
  public void setAttribute(String name, Object value) {
    checkMapUsage();
    if (feature_set != null && feature_set.getRequiredFeatures().contains(name)) {
      int idx = feature_set.getFeatureIndex(name);
      this.fs_attributes[idx] = value;
    } else {
      this.attributes.put(name, value);
    }
  }

  /**
   * Determines if the region has a non-null attribute for a given attribute name
   *
   * @param name the attribute name
   * @return true if the attribute exists and is non-null
   */
  public Boolean hasAttribute(String name) {
    checkMapUsage();
    if (feature_set != null && feature_set.getRequiredFeatures().contains(name)) {
      int idx = feature_set.getFeatureIndex(name);
      return this.fs_attributes[idx] != null ||
          (this.attributes.containsKey(name) && this.attributes.get(name) != null);
    } else {
      return this.attributes.containsKey(name) && this.attributes.get(name) != null;
    }
  }

  /**
   * Retrieves an attribute.
   *
   * @param name the name of the requested attribute
   * @return the attribute value or null if it does not exist
   */
  public Object getAttribute(String name) {
    checkMapUsage();
    if (feature_set != null && feature_set.getRequiredFeatures().contains(name)) {
      int idx = feature_set.getFeatureIndex(name);
      if (this.fs_attributes[idx] != null) {
        return this.fs_attributes[idx];
      } else {
        return this.attributes.get(name);
      }
    } else {
      return this.attributes.get(name);
    }
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
    if (feature_set != null) {
      for (String name : feature_set.getRequiredFeatures()) {
        if (getAttribute(name) != null) {
          names.add(name);
        }
      }
      if (feature_set.class_attribute != null) {
        if (getAttribute(feature_set.class_attribute) != null) {
          names.add(feature_set.class_attribute);
        }
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
    if (feature_set != null && feature_set.getRequiredFeatures().contains(name)) {
      fs_attributes[feature_set.getFeatureIndex(name)] = null;
    }
    // Guarantees that the attribute is not stored in either location.
    if (attributes.containsKey(name)) {
      this.attributes.remove(name);
    }
  }

  /**
   * Removes all attributes from the region.
   */
  public void clearAttributes() {
    this.attributes.clear();
    if (fs_attributes != null) {
      for (int i = 0; i < fs_attributes.length; ++i) {
        fs_attributes[i] = null;
      }
    }
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

  /**
   * Increases the capacity for an additional required feature.
   * <p/>
   * This is used when a region has already been added to a data set, and then later, the required feature list for that
   * attribute is increased.
   */
  public void addRequiredFeatureCapacity() {
    // Reallocate
    Object[] newArray = new Object[fs_attributes.length + 1];

    // copy
    System.arraycopy(fs_attributes, 0, newArray, 0, fs_attributes.length);
    fs_attributes = newArray;
  }
}
