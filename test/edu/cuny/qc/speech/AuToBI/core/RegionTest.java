/*  RegionTest.java

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

import java.util.Set;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertNotSame;

/**
 * Tests for PitchCandidate
 */
public class RegionTest {

  @Test
  public void testConstructor() {
    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");

    assertEquals(0.0, r.getStart());
    assertEquals(1.0, r.getEnd());
    assertEquals("test_label", r.getLabel());
    assertEquals("/test/file/name.txt", r.getFile());
  }

  @Test
  public void testConstructorNoFile() {
    Region r = new Region(0.0, 1.0, "test_label");

    assertEquals(0.0, r.getStart());
    assertEquals(1.0, r.getEnd());
    assertEquals("test_label", r.getLabel());
    assertEquals(null, r.getFile());
  }

  @Test
  public void testConstructorNoLabel() {
    Region r = new Region(0.0, 1.0);

    assertEquals(0.0, r.getStart());
    assertEquals(1.0, r.getEnd());
    assertNull(r.getLabel());
    assertNull(r.getFile());
  }

  @Test
  public void testConstructorPointDuration() {
    Region r = new Region(1.0);

    assertEquals(1.0, r.getStart());
    assertEquals(1.0, r.getEnd());
    assertNull(r.getLabel());
    assertNull(r.getFile());
  }

  @Test
  public void testConstructorFromOtherRegionWithNoAttributes() {

    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");
    Region new_r = new Region(r);

    assertNotSame(new_r, r);
    assertEquals(0.0, new_r.getStart());
    assertEquals(1.0, new_r.getEnd());
    assertEquals("test_label", new_r.getLabel());
    assertEquals("/test/file/name.txt", new_r.getFile());
  }

  @Test
  public void testConstructorFromOtherRegionWithAttributes() {

    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");
    r.setAttribute("test", "value");
    Region new_r = new Region(r);

    assertNotSame(new_r, r);
    assertEquals(0.0, new_r.getStart());
    assertEquals(1.0, new_r.getEnd());
    assertEquals("test_label", new_r.getLabel());
    assertEquals("/test/file/name.txt", new_r.getFile());
    assertTrue(new_r.hasAttribute("test"));
  }


  @Test
  public void testSetAndGetStart() {
    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");

    r.setStart(4.5);
    assertEquals(4.5, r.getStart(), 0.0001);
  }

  @Test
  public void testSetAndGetEnd() {
    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");

    r.setEnd(4.5);
    assertEquals(4.5, r.getEnd(), 0.0001);
  }

  @Test
  public void testSetAndGetLabel() {
    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");

    r.setLabel("new_label");
    assertEquals("new_label", r.getLabel());
  }

  @Test
  public void testSetAndGetFile() {
    Region r = new Region(0.0, 1.0, "test_label", "/test/file/name.txt");

    r.setFile("/new/file.txt");
    assertEquals("/new/file.txt", r.getFile());
  }

  @Test
  public void testGetDuration() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    assertEquals(10.0, r.getDuration(), 0.0001);
  }

  @Test
  public void testGetAttributes() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    assertNotNull(r.getAttributeNames());
    assertEquals(0, r.getAttributeNames().size());
  }

  @Test
  public void testSetAttribute() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", "value");
    assertTrue(r.hasAttribute("test_attribute"));
    assertEquals("value", r.getAttribute("test_attribute"));
  }

  @Test
  public void testHasAttributeIsFalseOnNullValue() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", null);
    assertFalse(r.hasAttribute("test_attribute"));
  }

  @Test
  public void testGetAttributeNames() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", "value");
    r.setAttribute("test_attribute_two", "value_two");

    Set<String> names = r.getAttributeNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("test_attribute"));
    assertTrue(names.contains("test_attribute_two"));
  }

  @Test
  public void testGetAttributeNamesDoesNotIncludeNullValuedAttributes() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", "value");
    r.setAttribute("test_attribute_two", null);

    Set<String> names = r.getAttributeNames();
    assertEquals(1, names.size());
    assertTrue(names.contains("test_attribute"));
    assertFalse(names.contains("test_attribute_two"));
  }

  @Test
  public void testRemoveAttribute() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", "value");

    r.removeAttribute("test_attribute");
    assertFalse(r.hasAttribute("test_attribute"));
  }

  @Test
  public void testClearAttributes() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");

    r.setAttribute("test_attribute", "value");
    r.setAttribute("test_attribute_two", "value");

    r.clearAttributes();
    assertEquals(0, r.getAttributeNames().size());
  }


  @Test
  public void testSetFeatureSetAssignsRequiredFeatureNames() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");


    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("test_feature");
    r.setFeatureSet(fs);
    r.setAttribute("test_feature", "hello");

    assertTrue(r.getAttributeNames().contains("test_feature"));
  }

  @Test
  public void testSetFeatureSetAssignsClassAttribute() {
    Region r = new Region(5.0, 15.0, "test_label", "/test/file/name.txt");


    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("test_feature");

    r.setFeatureSet(fs);
    r.setAttribute("test_feature", "hi");

    assertTrue(r.getAttributeNames().contains("test_feature"));
  }

  @Test
  public void testSetAndGetFeatureSetRequiredAttribute() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");

    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("test_feature");
    fs.insertDataPoint(w);

    w.setAttribute("test_feature", "TESTING");
    assertEquals("TESTING", w.getAttribute("test_feature"));
  }

  @Test
  public void testSetAndGetFeatureSetClassAttribute() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");

    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("class_attr");
    fs.insertDataPoint(w);

    w.setAttribute("class_attr", "TESTING");
    assertEquals("TESTING", w.getAttribute("class_attr"));
  }

  @Test
  public void testSetAndGetAttributesWithMismatchedFeatureSetAssignment() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");
    w.setAttribute("test_attribute", "TESTING");

    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("test_attribute");
    fs.insertDataPoint(w);

    assertEquals("TESTING", w.getAttribute("test_attribute"));
  }

  @Test
  public void testHasAttributesWithMismatchedFeatureSetAssignment() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");
    w.setAttribute("test_attribute", "TESTING");

    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("test_attribute");
    fs.insertDataPoint(w);

    assertTrue(w.hasAttribute("test_attribute"));
  }

  @Test
  public void testGetAttributeAfterFeatureSetReassignmentRemovingRequiredFeature() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");
    w.setAttribute("test_attribute", "TESTING");

    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("test_attribute");
    fs.insertDataPoint(w);

    FeatureSet new_fs = new FeatureSet();
    new_fs.insertDataPoint(w);

    assertEquals("TESTING", w.getAttribute("test_attribute"));
  }

  @Test
  public void testGetAttributeAfterFeatureSetReassignmentAddingRequiredFeature() {
    Word w = new Word(5.0, 15.0, "test_label", "/test/file/name.txt");
    w.setAttribute("test_attribute", "TESTING");

    FeatureSet fs = new FeatureSet();
    fs.insertDataPoint(w);

    FeatureSet new_fs = new FeatureSet();
    new_fs.insertRequiredFeature("test_attribute");
    new_fs.insertDataPoint(w);

    assertEquals("TESTING", w.getAttribute("test_attribute"));
  }
}

