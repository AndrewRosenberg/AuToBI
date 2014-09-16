/*  Feature.java

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

import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.*;
import java.io.Serializable;

/**
 * The Feature class stores information about extracted features.
 * <p/>
 * For numeric features this is a trivial wrapper to a string.
 * <p/>
 * For nominal features, the Feature class manages the possible nominal values.
 */
public class Feature implements Comparable, Serializable {
  private static final long serialVersionUID = 6410344724558496458L;
  private String name;                          // the feature name
  private LinkedHashSet<String> nominalValues;  // the nominal values
  public static final int STRING_LIST = 0;
  public static final int CSV_FORMAT = 1;
  private boolean isString;                     // is the feature a string

  /**
   * Constructs a new feature.
   *
   * @param name the name of the feature
   */
  public Feature(String name) {
    this.name = name;
    nominalValues = null;
  }

  /**
   * Sets whether or not the feature contains strings.
   *
   * @param b a boolean value
   */
  public void setString(boolean b) {
    isString = b;
  }

  /**
   * Retrives the name of the feature.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the feature.
   *
   * @param name the new name of the feature
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Compares two Features against each other by their names
   *
   * @param o the compared object
   * @return 0 if they match, -1 if this feature has an earlier name than the compared object, 1 if a later name
   */
  public int compareTo(Object o) {
    if (o instanceof Feature) {
      return this.getName().compareTo(((Feature) o).getName());
    }
    if (o instanceof String) {
      return this.getName().compareTo((String) o);
    }
    throw new ClassCastException("Cannot compare Feature with " + o.getClass());
  }

  /**
   * Determines if the feature holds strings or not.
   *
   * @return true if it is a string feature, false otherwise.
   */
  public boolean isString() {
    return isString;
  }

  /**
   * Sets the feature to contain nominal values.
   */
  public void setNominal() {
    nominalValues = new LinkedHashSet<String>();
  }

  /**
   * Returns true if the feature is nominal, false otherwise.
   *
   * @return whether or not the feature is nominal
   */
  public boolean isNominal() {
    return (nominalValues != null);
  }


  /**
   * Adds a new nominal value.
   *
   * @param s the new value
   */
  public void addNominalValue(String s) {
    if (!isNominal()) {
      setNominal();
    }
    nominalValues.add(s);
  }

  /**
   * Retrieves the nominal values as a comma separated list.
   *
   * @return the nominal values
   */
  public String getNominalValuesCSV() {
    StringBuffer csvStr = new StringBuffer();

    boolean first = true;

    for (String s : nominalValues) {
      if (!first) {
        csvStr.append(",");
      } else {
        first = false;
      }
      csvStr.append(s);
    }
    return csvStr.toString();
  }

  /**
   * Retrieves the set of nominal values.
   *
   * @return the nominal values
   */
  public Set<String> getNominalValues() {
    return nominalValues;
  }

  /**
   * Sets the nominal values.
   *
   * @param values a set of nominal values
   */
  public void setNominalValues(Collection<String> values) {
    this.nominalValues = new LinkedHashSet<String>();
    this.nominalValues.addAll(values);
  }

  /**
   * Sets nominal values to to the current list.
   *
   * @param values the values
   */
  public void setNominalValues(String[] values) {
    nominalValues = new LinkedHashSet<String>();
    this.nominalValues.addAll(Arrays.asList(values));
  }

  /**
   * Adds nominal values to to the current list.
   *
   * @param values the values
   */
  public void addNominalValues(Collection<String> values) {
    if (nominalValues == null) {
      nominalValues = new LinkedHashSet<String>();
    }
    for (String s : values) {
      this.nominalValues.add(s);
    }
  }

  /**
   * Adds nominal values to to the current list.
   *
   * @param values the values
   */
  public void addNominalValues(String[] values) {
    if (nominalValues == null) {
      nominalValues = new LinkedHashSet<String>();
    }
    this.nominalValues.addAll(Arrays.asList(values));
  }

  /**
   * Returns a unique index for a nominal value.
   * Returns -1 if the value does not exist.
   *
   * @param value the value to index
   */
  public int getNominalIndex(String value) {
    int i = 0;
    for (String nominalValue : nominalValues) {
      if (nominalValue.equals(value)) return i;
      i++;
    }
    return -1;
  }

  /**
   * Generate the used values for the feature from a set of data points.
   *
   * @param data_points the data points.
   */
  public void generateNominalValues(List<Word> data_points) {
    if (!isNominal()) {
      setNominal();
    }
    for (Region r : data_points) {
      if (r.getAttribute(name) == null) {
        AuToBIUtils.debug("Region " + r + " has no attribute: " + name);
      } else if (r.getAttribute(name) instanceof String) {
        addNominalValue((String) r.getAttribute(name));
      } else if (r.getAttribute(name) instanceof Number) {
        addNominalValue(r.getAttribute(name).toString());
      }
    }
  }
}
