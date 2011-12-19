/*  FeatureTest.java

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

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class FeatureTest {

  @Test
  public void testConstructor() {

    Feature f = new Feature("test");
    assertEquals("test", f.getName());
  }

  @Test
  public void testSetAndIsString() {
    Feature f = new Feature("test");
    f.setString(true);
    assertTrue(f.isString());
  }

  @Test
  public void testGetAndSetName() {
    Feature f = new Feature("test");
    f.setName("one");
    assertEquals("one", f.getName());
  }

  @Test
  public void testCompareToFeature() {

    Feature f1 = new Feature("test");

    Feature f2 = new Feature("second");

    assertEquals(1, f1.compareTo(f2));
  }

  @Test
  public void testCompareToString() {

    Feature f1 = new Feature("test");

    assertEquals(1, f1.compareTo("second"));
  }

  @Test
  public void testCompareToOtherObjectFails() {

    Feature f1 = new Feature("test");

    try {
      assertEquals(1, f1.compareTo(1.0));
      fail();
    } catch (ClassCastException expected) {

    }
  }

  @Test
  public void testSetAndIsNominal() {
    Feature f1 = new Feature("test");

    assertFalse(f1.isNominal());
    f1.setNominal();
    assertTrue(f1.isNominal());
  }

  @Test
  public void testAddNominalValues() {
    Feature f1 = new Feature("test");

    List<String> nominal_values = new ArrayList<String>();
    nominal_values.add("one");
    nominal_values.add("two");

    f1.addNominalValues(nominal_values);

    assertEquals(2, f1.getNominalValues().size());
  }

  @Test
  public void testAddNominalValuesArray() {
    Feature f1 = new Feature("test");

    f1.addNominalValues(new String[]{"one", "two"});

    assertEquals(2, f1.getNominalValues().size());
  }

  @Test
  public void testSetNominalValues() {
    Feature f1 = new Feature("test");

    List<String> nominal_values = new ArrayList<String>();
    nominal_values.add("one");
    nominal_values.add("two");

    f1.setNominalValues(nominal_values);

    assertEquals(2, f1.getNominalValues().size());
  }

  @Test
  public void testSetNominalValuesArray() {
    Feature f1 = new Feature("test");

    f1.setNominalValues(new String[]{"one", "two"});

    assertEquals(2, f1.getNominalValues().size());

  }

  @Test
  public void testGenerateNominalValues() {
    Feature f1 = new Feature("test");

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1.0, 2.0, "one");
    Word w2 = new Word(2.0, 3.0, "two");
    words.add(w1);
    words.add(w2);
    w1.setAttribute("test", "hello");
    w2.setAttribute("test", "world");

    f1.generateNominalValues(words);

    assertEquals(2, f1.getNominalValues().size());
  }

  @Test
  public void testGenerateNominalValuesWithNullAttribute() {
    Feature f1 = new Feature("test");

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1.0, 2.0, "one");
    Word w2 = new Word(2.0, 3.0, "two");
    words.add(w1);
    words.add(w2);
    w1.setAttribute("test", "hello");

    f1.generateNominalValues(words);

    assertEquals(1, f1.getNominalValues().size());
  }

  @Test
  public void testGenerateNominalValuesOverNumbers() {
    Feature f1 = new Feature("test");

    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(1.0, 2.0, "one");
    Word w2 = new Word(2.0, 3.0, "two");
    words.add(w1);
    words.add(w2);
    w1.setAttribute("test", 123);
    w2.setAttribute("test", 1.23);

    f1.generateNominalValues(words);

    assertEquals(2, f1.getNominalValues().size());
  }

  @Test
  public void testGetNominalValuesCSV() {
    Feature f1 = new Feature("test");

    f1.setNominalValues(new String[]{"one", "two"});
    assertEquals("one,two", f1.getNominalValuesCSV());
  }

}

