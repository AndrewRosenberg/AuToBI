/*  FeatureSet.java

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

import edu.cuny.qc.speech.AuToBI.io.AuToBIFileWriter;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.*;
import java.io.Serializable;
import java.io.IOException;

/**
 * FeatureSet objects are responsible for maintaining information about the features required for a classification
 * task.
 * <p/>
 * For a given task, the required features are described.  An AuToBI object uses these required features to drive the
 * appropriate feature extraction routines.
 * <p/>
 * FeatureSets also maintain a list of data points.  This allows everything about a data set to be serialized for later
 * processing.
 *
 * @see edu.cuny.qc.speech.AuToBI.AuToBI
 */
public class FeatureSet implements Serializable {
  private static final long serialVersionUID = 20100324L;

  protected Set<String> required_features;  // The required feature names
  protected Set<Feature> features;          // The extracted feature objects
  protected List<Word> data_points;         // Associated data points
  protected String class_attribute;         // The name of the class attribute (if any)

  // TODO update the feature extraction routine to make this unnecessary -- see AuToBI for notes about reference counting.
  // Intermediate features are kept from being removed from data points when cleaning unnecessary features
  // during feature extraction.
  protected Set<String> intermediate_features;  // Any intermediate features that may be needed for processing

  /**
   * Constructs an empty FeatureSet.
   */
  public FeatureSet() {
    this.features = new LinkedHashSet<Feature>();
    this.data_points = new ArrayList<Word>();
    this.required_features = new HashSet<String>();
    this.intermediate_features = new HashSet<String>();
  }

  /**
   * FeatureSet copy constructor.
   *
   * @return a new instance of the FeatureSet with copied internal variables.
   */
  public FeatureSet newInstance() {
    FeatureSet newfs = new FeatureSet();
    newfs.features.addAll(this.getFeatures());
    newfs.data_points.addAll(this.getDataPoints());
    newfs.required_features.addAll(this.getRequiredFeatures());
    newfs.intermediate_features.addAll(this.getIntermediateFeatures());
    newfs.class_attribute = this.getClassAttribute();
    return newfs;
  }


  /**
   * Retrieves the associated data points.
   *
   * @return the data points
   */
  public List<Word> getDataPoints() {
    return data_points;
  }

  /**
   * Sets the associated data points.
   *
   * @param words the data points
   */
  public void setDataPoints(List<Word> words) {
    data_points = words;
  }

  /**
   * Retrieves the feature names.
   *
   * @return the feature names
   */
  public List<String> getFeatureNames() {
    ArrayList<String> names = new ArrayList<String>();
    for (Feature f : features)
      names.add(f.getName());

    return names;
  }

  /**
   * Retrieve a Feature object.
   * <p/>
   * Returns null if the requested feature does not exist.
   *
   * @param feature_name the feature name
   * @return the associated feature object
   */
  public Feature getFeature(String feature_name) {
    for (Feature f : features) {
      if (f.getName().equals(feature_name)) {
        return f;
      }
    }
    return null;
  }

  /**
   * Sets the set of extracted features.
   *
   * @param features the features
   */
  public void setFeatures(Set<Feature> features) {
    this.features = features;
  }

  /**
   * Gets the set of features.
   *
   * @return the features.
   */
  public Set<Feature> getFeatures() {
    return features;
  }

  /**
   * Inserts a data points into the feature set.
   *
   * @param pt the data point
   */
  public void insertDataPoint(Word pt) {
    checkDataPoints();
    data_points.add(pt);
  }

  /**
   * Confirms that data points is not null.
   */
  private void checkDataPoints() {
    if (data_points == null) data_points = new ArrayList<Word>();
  }

  /**
   * Retrieves the class attribute.
   *
   * @return the class attribute
   */
  public String getClassAttribute() {
    return class_attribute;
  }

  /**
   * Remove all attributes from the data points in a feature set that are not in the required_features list.
   * <p/>
   * When extracting features, additional attributes can be attached to features as intermedate features. To conserve
   * memory, these can be deleted when the feature set is finalized.
   */
  public void garbageCollection() {
    for (Word w : data_points) {
      List<String> to_delete = new ArrayList<String>();
      for (String attribute_name : w.getAttributes().keySet()) {
        if (!required_features.contains(attribute_name) && !attribute_name.equals(class_attribute) &&
            !intermediate_features.contains(attribute_name)) {
          to_delete.add(attribute_name);
        }
      }
      for (String attribute_name : to_delete) {
        w.getAttributes().remove(attribute_name);
      }
    }
  }

  /**
   * Constructs the set of features from the list of required features.
   * <p/>
   * Note: feature names that start with "nominal_" are treated as nominal features.  Valid nominal values are
   * constructed from all values represented in the associated data points.
   * <p/>
   * The class attribute is always treated as a nominal feature.
   */
  public void constructFeatures() {
    features.clear();
    for (String feature : required_features) {
      Feature f = new Feature(feature);
      if (feature.startsWith("nominal_")) {
        f.generateNominalValues(data_points);
      }
      features.add(f);
    }

    Feature class_feature = new Feature(class_attribute);
    class_feature.generateNominalValues(data_points);
    features.add(class_feature);
  }

  /**
   * Retrieves the set of required features.
   *
   * @return the required features
   */
  public Set<String> getRequiredFeatures() {
    return required_features;
  }

  /**
   * Writes the contents of the feature set to an arff formatted text file.
   * <p/>
   * The arff format is read by the weka machine learning toolkit.
   * <p/>
   * A description of the format can be found at the following URL: http://www.cs.waikato.ac.nz/~ml/weka/arff.html
   *
   * @param arff_file     the name of the destination arff file
   * @param relation_name a description of the relation, a required arff field
   * @throws IOException if there is a problem writing to the file.
   */
  public void writeArff(String arff_file, String relation_name) throws
      IOException {
    AuToBIFileWriter writer = new AuToBIFileWriter(arff_file);

    writer.write("@relation ");
    writer.write(relation_name);
    writer.write("\n\n");
    writer.write(generateArffAttributes());
    writer.write("\n@data\n");
    writeCSVData(writer);
    writer.close();
  }

  /**
   * Writes the contents of the feature set to a comma separated value (CSV) text file.
   *
   * @param csv_file the name of the destination csv file
   * @throws IOException if there is a problem writing to the file.
   */
  public void writeCSVFile(String csv_file) throws IOException {
    AuToBIFileWriter writer = new AuToBIFileWriter(csv_file);
    writeCSVHeader(writer);
    writeCSVData(writer);
    writer.close();
  }


  /**
   * Build the arff attribute section based on the list of features contained in this feature set
   * <p/>
   * See http://www.cs.waikato.ac.nz/~ml/weka/arff.html for the arff standard
   *
   * @return a string contining an arff description of the included features
   */
  protected String generateArffAttributes() {
    StringBuffer attrString = new StringBuffer();

    for (Feature f : this.features) {

      attrString.append("@attribute ");
      attrString.append(f.getName());
      if (f.isNominal()) {
        if (f.getNominalValues().isEmpty())
          AuToBIUtils
              .warn(" Warning: empty nominal values for feature:" + f.getName() + ". Generate Arff _Data_ first.");

        attrString.append(" {");
        attrString.append(f.getNominalValuesCSV());
        attrString.append("}\n");
      } else if (f.isString()) {
        attrString.append(" string\n");
      } else {
        attrString.append(" numeric\n");
      }
    }

    return attrString.toString();
  }

  /**
   * Writes comma separated data via the provided writer.
   *
   * @param writer A writer object to write to the file
   * @throws java.io.IOException on Write errors
   */
  protected void writeCSVData(AuToBIFileWriter writer) throws IOException {
    for (Region r : data_points) {
      boolean first = true;
      for (Feature f : this.features) {
        if (!first) {
          writer.write(",");
        } else {
          first = false;
        }
        if (r.getAttribute(f.getName()) == null) {
          AuToBIUtils.debug("missing attribute:" + f.getName() + " on word:" +
              r.toString());

          // Weka's arff standard uses the question mark (?) to indicate missing values
          writer.write("?");
        } else {
          String value = r.getAttribute(f.getName()).toString();
          if (value.length() == 0) {
            AuToBIUtils.warn("Empty value for attribute:" + f.getName() + " on " + r);
          }
          writer.write(value);
        }
        if (f.isNominal() || (f.getNominalValues() != null && f.getNominalValues().size() > 0)) {
          f.addNominalValue(r.getAttribute(f.getName()).toString());
        }
      }

      writer.write("\n");
    }
  }

  /**
   * Writes a csv header line via the provided writer.
   *
   * @param writer A writer object to write to the file
   * @throws IOException on Write errors
   */
  protected void writeCSVHeader(AuToBIFileWriter writer) throws IOException {
    if (data_points.size() == 0) return;

    boolean first = true;
    for (Feature f : this.features) {
      if (!first) writer.write(",");
      else first = false;
      writer.write(f.getName());
    }
    writer.write("\n");
  }


  /**
   * Sets the class attribute.
   *
   * @param class_attribute the desired class attribute
   */
  public void setClassAttribute(String class_attribute) {
    this.class_attribute = class_attribute;
  }

  /**
   * Retrieves the set of intermediate features.
   *
   * @return the intermediate features
   */
  public Collection<? extends String> getIntermediateFeatures() {
    return intermediate_features;
  }

  /**
   * Adds a collection of strings to the list of intermediate features.
   *
   * @param features the features to add to the set
   */
  public void addIntermediateFeatures(Collection<String> features) {
    intermediate_features.addAll(features);
  }
}
