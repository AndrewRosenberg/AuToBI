/*  FeatureSetTest.java

    Copyright (c) 2011 Andrew Rosenberg

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

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for FeatureSet.
 *
 * @see FeatureSet
 */
public class FeatureSetTest {

  @Test
  public void testFeatureConstructionWorksWithNullClassAttribute() {
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(new Word(0, 1, "one"));
    fs.getDataPoints().add(new Word(1, 2, "two"));
    fs.setClassAttribute(null);

    fs.constructFeatures();
  }

  @Test
  public void testNewInstance() {
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(new Word(0, 1, "one"));
    fs.getDataPoints().add(new Word(1, 2, "two"));
    fs.setClassAttribute(null);

    fs.constructFeatures();

    FeatureSet newfs = fs.newInstance();

    assertEquals(2, newfs.getDataPoints().size());
    assertNull(newfs.getClassAttribute());
  }

  @Test
  public void testSetDataPoints() {
    FeatureSet fs = new FeatureSet();
    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    words.add(w1);
    words.add(w2);

    fs.setDataPoints(words);

    assertEquals(2, fs.getDataPoints().size());
  }

  @Test
  public void testSetFeatures() {
    FeatureSet fs = new FeatureSet();

    Set<Feature> features = new HashSet<Feature>();
    features.add(new Feature("one"));
    features.add(new Feature("two"));
    fs.setFeatures(features);

    assertEquals(2, fs.getFeatures().size());
  }

  @Test
  public void testGetFeature() {
    FeatureSet fs = new FeatureSet();

    Set<Feature> features = new HashSet<Feature>();
    features.add(new Feature("one"));
    features.add(new Feature("two"));
    fs.setFeatures(features);

    assertEquals("one", fs.getFeature("one").getName());
  }

  @Test
  public void testGetFeatureNames() {
    FeatureSet fs = new FeatureSet();

    Set<Feature> features = new HashSet<Feature>();
    features.add(new Feature("one"));
    features.add(new Feature("two"));
    fs.setFeatures(features);

    assertEquals(2, fs.getFeatureNames().size());
    assertTrue(fs.getFeatureNames().contains("one"));
    assertTrue(fs.getFeatureNames().contains("two"));
  }

  @Test
  public void testGetNullFeature() {
    FeatureSet fs = new FeatureSet();

    Set<Feature> features = new HashSet<Feature>();
    features.add(new Feature("one"));
    features.add(new Feature("two"));
    fs.setFeatures(features);

    assertNull(fs.getFeature("no_feature"));
  }

  @Test
  public void testGetAndSetFeatures() {
    FeatureSet fs = new FeatureSet();

    Set<Feature> features = new HashSet<Feature>();
    features.add(new Feature("one"));
    features.add(new Feature("two"));
    fs.setFeatures(features);

    assertEquals(features, fs.getFeatures());
  }

  @Test
  public void testInsertDataPoint() {
    FeatureSet fs = new FeatureSet();
    fs.setDataPoints(new ArrayList<Word>());
    fs.insertDataPoint(new Word(0, 1, "test"));

    assertEquals(1, fs.getDataPoints().size());
  }

  @Test
  public void testInsertDataPointWhenNotAllocated() {

    FeatureSet fs = new FeatureSet();
    fs.insertDataPoint(new Word(0, 1, "test"));

    assertEquals(1, fs.getDataPoints().size());
  }

  @Test
  public void testSetAndGetClassAttribute() {
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("test_class_attribute");

    assertEquals("test_class_attribute", fs.getClassAttribute());
  }

  @Test
  public void testRemoveFeatureFromWords() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("test_feature", "value");

    assertEquals("value", w.getAttribute("test_feature"));

    FeatureSet fs = new FeatureSet();
    fs.insertDataPoint(w);
    fs.removeFeatureFromDataPoints("test_feature");
    assertFalse(w.hasAttribute("test_feature"));
  }

  @Test
  public void testConstructFeatures() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("one");

    fs.constructFeatures();

    boolean found = false;
    for (Feature f : fs.getFeatures()) {
      if (f.getName().equals("one")) {
        found = true;
      }
    }
    assertTrue(found);
  }


  @Test
  public void testConstructFeaturesNominal() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");

    fs.constructFeatures();

    boolean found = false;
    for (Feature f : fs.getFeatures()) {
      if (f.getName().equals("nominal_one")) {
        found = true;
        assertTrue(f.isNominal());
      }
    }
    assertTrue(found);
  }

  @Test
  public void testGenerateArffAttributes() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", 3);
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    w2.setAttribute("two", 5);
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    String arffattributes = fs.generateArffAttributes();

    assertEquals("@attribute two numeric\n" +
        "@attribute nominal_one {value_one,value_two}\n", arffattributes);

  }

  @Test
  public void testGenerateArffAttributesWithStringAttribute() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", "test_string_value");
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    w2.setAttribute("two", "test_string_value");
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    fs.getFeature("two").setString(true);

    String arffattributes = fs.generateArffAttributes();

    assertEquals("@attribute two string\n" +
        "@attribute nominal_one {value_one,value_two}\n", arffattributes);

  }

  @Test
  public void testGenerateArffAttributesWithEmptyNomnialValues() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    fs.constructFeatures();

    String arffattributes = fs.generateArffAttributes();

    assertEquals("@attribute two numeric\n" +
        "@attribute nominal_one {}\n", arffattributes);

  }

  @Test
  public void testGenerateArffData() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", 3);
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    w2.setAttribute("two", 5);
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    String data = fs.generateArffData();

    assertEquals("@data\n" +
        "3,value_one\n" +
        "5,value_two\n", data);
  }

  @Test
  public void testGenerateArffDataWithMissingAttributes() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", 3);
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    String data = fs.generateArffData();

    assertEquals("@data\n" +
        "3,value_one\n" +
        "?,value_two\n", data);
  }

  @Test
  public void testGenerateArffDataWithEmptyValue() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", 3);
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    w2.setAttribute("two", "");
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    String data = fs.generateArffData();

    assertEquals("@data\n" +
        "3,value_one\n" +
        ",value_two\n", data);
  }

  @Test
  public void testGenerateCSVHeader() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("nominal_one", "value_one");
    w1.setAttribute("two", 3);
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("nominal_one", "value_two");
    w2.setAttribute("two", 5);
    fs.getDataPoints().add(w1);
    fs.getDataPoints().add(w2);
    fs.constructFeatures();
    String header = fs.generateCSVHeader();

    assertEquals("two,nominal_one\n", header);
  }


  @Test
  public void testGetFeatureIndexWorksOnClassAttribute() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");
    fs.setClassAttribute("class_attr");

    assertEquals(0, fs.getFeatureIndex("class_attr"));
  }

  @Test
  public void testGetFeatureIndexWorksOnRequiredFeature() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");
    fs.setClassAttribute("class_attr");

    assertEquals(2, fs.getFeatureIndex("two"));
  }

  @Test
  public void testGetFeatureIndexReturnsNegOneOnUnknownFeature() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("nominal_one");
    fs.insertRequiredFeature("two");
    fs.setClassAttribute("class_attr");

    assertEquals(-1, fs.getFeatureIndex("nosuchfeature"));
  }
}
