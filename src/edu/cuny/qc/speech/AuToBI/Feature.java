/*  Feature.java

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
package edu.cuny.qc.speech.AuToBI;

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
    if (!isNominal())
      setNominal();
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
      if (!first)
        csvStr.append(",");
      else
        first = false;
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
   * @param nominalValues a set of nominal values
   */
  public void setNominalValues(Collection<String> nominalValues) {
    this.nominalValues.addAll(nominalValues);
  }


  /**
   * Generate the used values for the feature from a set of data points.
   *
   * @param data_points the data points.
   */
  public void generateNominalValues(List<Word> data_points) {
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

  /**
   * Adds nominal values to to the current list.
   *
   * @param values the values
   */
  public void addNominalValues(Collection<String> values) {
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
    this.nominalValues.addAll(Arrays.asList(values));
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
}
