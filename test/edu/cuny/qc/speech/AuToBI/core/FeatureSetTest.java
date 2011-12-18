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
    fs.getRequiredFeatures().add("one");

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
    fs.getRequiredFeatures().add("nominal_one");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

    fs.constructFeatures();

    String arffattributes = fs.generateArffAttributes();

    assertEquals("@attribute two numeric\n" +
        "@attribute nominal_one {}\n", arffattributes);

  }

  @Test
  public void testGenerateArffData() {
    FeatureSet fs = new FeatureSet();
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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
    fs.getRequiredFeatures().add("nominal_one");
    fs.getRequiredFeatures().add("two");

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

}
