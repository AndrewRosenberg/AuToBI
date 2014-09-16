/*  FeatureSet.java

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

import com.google.common.collect.HashBiMap;
import edu.cuny.qc.speech.AuToBI.io.AuToBIFileWriter;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;

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
  private static final long serialVersionUID = 20120110L;

  protected Map<String, Integer> required_features;  // The required feature names
  protected Set<Feature> features;          // The extracted feature objects
  protected List<Word> data_points;         // Associated data points
  protected String class_attribute;         // The name of the class attribute (if any)


  /**
   * Constructs an empty FeatureSet.
   */
  public FeatureSet() {
    this.features = new LinkedHashSet<Feature>();
    this.data_points = new ArrayList<Word>();
    this.required_features = new HashMap<String, Integer>();
  }

  /**
   * FeatureSet copy constructor.
   *
   * @return a new instance of the FeatureSet with copied internal variables and data points.
   */
  public FeatureSet newInstance() {
    FeatureSet newfs = new FeatureSet();
    newfs.features.addAll(this.getFeatures());
    newfs.data_points.addAll(this.getDataPoints());
    for (Map.Entry<String, Integer> entry : this.required_features.entrySet()) {
      newfs.required_features.put(entry.getKey(), entry.getValue());
    }
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
    for (Word w : words) {
      w.setFeatureSet(this);
    }
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
    pt.setFeatureSet(this);
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
   * Removes an feature from every data point assigned to the data set.
   *
   * @param feature_name the attribute to remove.
   */
  public void removeFeatureFromDataPoints(String feature_name) {
    for (Word w : data_points) {
      w.removeAttribute(feature_name);
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
    for (String feature : required_features.keySet()) {
      Feature f = new Feature(feature);
      if (feature.startsWith("nominal_")) {
        f.generateNominalValues(data_points);
      }
      features.add(f);
    }

    if (class_attribute != null) {
      Feature class_feature = new Feature(class_attribute);
      class_feature.generateNominalValues(data_points);
      features.add(class_feature);
    }
  }

  /**
   * Retrieves the set of required features.
   *
   * @return the required features
   */
  public Set<String> getRequiredFeatures() {
    return required_features.keySet();
  }

  /**
   * Inserts a required feature into the set of required features
   *
   * @param feature_name the feature to insert
   */
  public void insertRequiredFeature(String feature_name) {
    if (!required_features.containsKey(feature_name)) {
      // The class attribute is stored at index 0
      required_features.put(feature_name, required_features.size() + 1);
      if (data_points != null && data_points.size() > 0) {
        for (Region r : data_points) {
          r.addRequiredFeatureCapacity();
        }
      }
    }
  }

  /**
   * Removes a required feature from the set of required features
   *
   * @param feature_name the feature to remove
   */
  public void removeRequiredFeature(String feature_name) {
    if (data_points != null && data_points.size() > 0) {
      //TODO: figure out the best way to do this.
      AuToBIUtils.error("Cannot remove a required feature from a FeatureSet that has associated data points..");
    }
    if (required_features.containsKey(feature_name)) {
      // The class attribute is stored at index 0
      Integer value = required_features.get(feature_name);
      for (String s : required_features.keySet()) {
        if (required_features.get(s) >= value) {
          required_features.put(s, required_features.get(s) - 1);
        }
      }
      required_features.remove(feature_name);
    }
  }

  /**
   * Gets a unique index corresponding to feature name.  If there is no associated index, returns -1
   *
   * @param feature_name the feature name
   * @return an appropriate array index.
   */
  public int getFeatureIndex(String feature_name) {
    if (class_attribute != null && class_attribute.equals(feature_name)) {
      return 0;
    }
    if (required_features.containsKey(feature_name)) {
      return required_features.get(feature_name);
    } else {
      return -1;
    }
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
    String data_section = generateArffData();
    writer.write("\n");
    writer.write(data_section);
    writer.close();
  }

  /**
   * Writes the contents of the feature set to a liblinear/libsvm formatted file
   *
   * @param filename filename
   */
  public void writeLibLinear(String filename) throws AuToBIException, IOException {
    String class_attribute = this.getClassAttribute();
    Set<String> class_values = getFeature(class_attribute).getNominalValues();
    double[] labels = ClassifierUtils.convertFeatureSetToLibLinearLabels(this,
        class_values.toArray(new String[class_values.size()]));

    HashMap<String, Aggregation> norm_map = ClassifierUtils.generateNormParams(this);
    HashBiMap<Feature, Integer> feature_map = ClassifierUtils.generateFeatureMap(this);

    de.bwaldvogel.liblinear.Feature[][] data = ClassifierUtils.normalizeLibLinearFeatures(
        ClassifierUtils.convertFeatureSetToLibLinearFeatures(this), feature_map.inverse(), norm_map);

    AuToBIFileWriter writer = new AuToBIFileWriter(filename);
    for (int i = 0; i < labels.length; i++) {
      writer.write(String.valueOf((int) labels[i]));
      writer.write(" ");
      for (int j = 0; j < data[i].length; j++) {
        writer.write(String.valueOf(data[i][j].getIndex()));
        writer.write(":");
        writer.write(String.valueOf(data[i][j].getValue()));
        if (j < data[i].length - 1) {
          writer.write(" ");
        }
      }
      writer.write("\n");
    }
    writer.close();
  }

  /**
   * Generates a String representation of the ARFF @data section.
   * <p/>
   * This includes a line containing @data followed by the data in comma separated format
   *
   * @return a string containing the arff data section
   */
  protected String generateArffData() {
    String data_section = "@data\n";
    data_section += generateCSVData();
    return data_section;
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
    StringBuilder attrString = new StringBuilder();

    for (Feature f : this.features) {

      attrString.append("@attribute ");
      attrString.append(f.getName().replace(",", "_"));  // commas are not allowed in in attribute names
      if (f.isNominal()) {
        if (f.getNominalValues().isEmpty()) {
          AuToBIUtils
              .warn(
                  " Warning: empty nominal values for feature:" + f.getName() + ". Try Generating Arff _Data_ first.");
        }

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
    writer.write(generateCSVData());
  }

  /**
   * Generates a string representation of the contained data in comma separated values form.
   *
   * @return a string containing the values.
   */
  private String generateCSVData() {
    StringBuilder sb = new StringBuilder();
    for (Region r : data_points) {
      boolean first = true;
      for (Feature f : this.features) {
        if (!first) {
          sb.append(",");
        } else {
          first = false;
        }
        if (r.getAttribute(f.getName()) == null) {
          AuToBIUtils.debug("missing attribute:" + f.getName() + " on word:" +
              r.toString());

          // Weka's arff standard uses the question mark (?) to indicate missing values
          sb.append("?");
        } else {
          // TODO: Include some string quoting or removal of commas here.
          String value = r.getAttribute(f.getName()).toString();
          if (value.length() == 0) {
            AuToBIUtils.warn("Empty value for attribute:" + f.getName() + " on " + r);
          }
          sb.append(value);
        }
        if (f.isNominal() || (f.getNominalValues() != null && f.getNominalValues().size() > 0)) {
          f.addNominalValue(r.getAttribute(f.getName()).toString());
        }
      }
      // TODO: Include instance weighting
      sb.append("\n");
    }
    return sb.toString();
  }


  /**
   * Writes a csv header line via the provided writer.
   *
   * @param writer A writer object to write to the file
   * @throws IOException on Write errors
   */
  protected void writeCSVHeader(AuToBIFileWriter writer) throws IOException {
    writer.write(generateCSVHeader());
  }

  /**
   * Generates the CSV header containing each of the feature names.
   *
   * @return the features.
   */
  protected String generateCSVHeader() {
    return AuToBIUtils.join(getFeatureNames(), ",") + "\n";
  }


  /**
   * Sets the class attribute.
   *
   * @param class_attribute the desired class attribute
   */
  public void setClassAttribute(String class_attribute) {
    this.class_attribute = class_attribute;
  }
}
