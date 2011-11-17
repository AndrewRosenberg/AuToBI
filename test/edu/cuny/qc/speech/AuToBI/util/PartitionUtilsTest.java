/*  PartitionUtilsTest.java

    Copyright 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for PartitionUtils methods
 */
public class PartitionUtilsTest {

  @Test
  public void testAssignFoldNumInRange() {
    List<Word> words = new ArrayList<Word>();
    Word r1 = new Word(0, 1, "one");
    Word r2 = new Word(0, 1, "two");
    words.add(r1);
    words.add(r2);

    try {
      PartitionUtils.assignFoldNum(words, "fold_num", 2);
      assertTrue(r1.hasAttribute("fold_num"));
      assertTrue(r2.hasAttribute("fold_num"));

      assertTrue(r1.getAttribute("fold_num") instanceof Integer);
      int r1_fold = (Integer) r1.getAttribute("fold_num");

      assertTrue(r1_fold >= 0 && r1_fold < 2);

      assertTrue(r2.getAttribute("fold_num") instanceof Integer);
      int r2_fold = (Integer) r2.getAttribute("fold_num");

      assertTrue(r2_fold >= 0 && r2_fold < 2);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAssignFoldNumInEmptyRange() {
    List<Word> words = new ArrayList<Word>();
    Word r1 = new Word(0, 1, "one");
    Word r2 = new Word(0, 1, "two");
    words.add(r1);
    words.add(r2);

    try {
      PartitionUtils.assignFoldNum(words, "fold_num", 0);
      fail();
    } catch (AuToBIException e) {
      // Expected
    }
  }

  @Test
  public void testGenerateXFoldAssignmentInRange() {
    List<String> words = new ArrayList<String>();
    words.add("one");
    words.add("two");
    words.add("three");

    HashMap<String, Integer> map = PartitionUtils.generateXValFoldAssignment(words, 2);

    assertEquals(3, map.size());

    assertTrue(map.containsKey("one"));
    assertTrue(map.containsKey("two"));
    assertTrue(map.containsKey("three"));

    assertTrue(map.get("one") >= 0 && map.get("one") < 2);
    assertTrue(map.get("two") >= 0 && map.get("two") < 2);
    assertTrue(map.get("three") >= 0 && map.get("three") < 2);
  }

  @Test
  public void testAssignStratifiedFoldNumNoClassAttribute() {
    List<Word> words = new ArrayList<Word>();
    Word r1 = new Word(0, 1, "one");
    Word r2 = new Word(0, 1, "two");
    words.add(r1);
    words.add(r2);

    try {
      PartitionUtils.assignStratifiedFoldNum(words, "fold_num", 2, "class");
      fail();
    } catch (AuToBIException e) {
      // Expected
    }
  }

  @Test
  public void testAssignStratifiedFoldNumInRange() {
    List<Word> words = new ArrayList<Word>();
    Word r1 = new Word(0, 1, "one");
    r1.setAttribute("class", "a");
    Word r2 = new Word(0, 1, "two");
    r2.setAttribute("class", "a");
    words.add(r1);
    words.add(r2);

    try {
      PartitionUtils.assignStratifiedFoldNum(words, "fold_num", 2, "class");
      assertTrue(r1.hasAttribute("fold_num"));
      assertTrue(r2.hasAttribute("fold_num"));

      assertTrue(r1.getAttribute("fold_num") instanceof Integer);
      int r1_fold = (Integer) r1.getAttribute("fold_num");

      assertTrue(r1_fold >= 0 && r1_fold < 2);

      assertTrue(r2.getAttribute("fold_num") instanceof Integer);
      int r2_fold = (Integer) r2.getAttribute("fold_num");

      assertTrue(r2_fold >= 0 && r2_fold < 2);

    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testSplitData() {
    List<Word> data = new ArrayList<Word>();
    List<Word> train = new ArrayList<Word>();
    List<Word> test = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("fold_num", 1);
    data.add(w1);

    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("fold_num", 2);
    data.add(w2);

    try {
      PartitionUtils.splitData(data, train, test, 1, "fold_num");
      assertEquals(data.size(), train.size() + test.size());

      assertTrue(train.contains(w2));
      assertTrue(test.contains(w1));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testSplitDataWithEmptyFoldAttributes() {
    List<Word> data = new ArrayList<Word>();
    List<Word> train = new ArrayList<Word>();
    List<Word> test = new ArrayList<Word>();

    Word w1 = new Word(0, 1, "one");
    data.add(w1);

    Word w2 = new Word(0, 1, "two");
    data.add(w2);

    try {
      PartitionUtils.splitData(data, train, test, 1, "fold_num");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testSplitDataWithStringsSize() {
    List<String> strings = new ArrayList<String>();
    strings.add("one");
    strings.add("two");
    strings.add("three");

    HashMap<String, Integer> map = new HashMap<String, Integer>();
    map.put("one", 1);
    map.put("two", 1);
    map.put("three", 2);

    List<String> train = new ArrayList<String>();
    List<String> test = new ArrayList<String>();

    PartitionUtils.splitData(strings, train, test, map, 1);

    assertEquals(strings.size(), train.size(), test.size());

  }

  @Test
  public void testSplitDataWithStringsAssignment() {
    List<String> strings = new ArrayList<String>();
    strings.add("one");
    strings.add("two");
    strings.add("three");

    HashMap<String, Integer> map = new HashMap<String, Integer>();
    map.put("one", 1);
    map.put("two", 1);
    map.put("three", 2);

    List<String> train = new ArrayList<String>();
    List<String> test = new ArrayList<String>();

    PartitionUtils.splitData(strings, train, test, map, 1);

    assertTrue(train.contains("three"));
    assertTrue(test.contains("one"));
    assertTrue(test.contains("two"));
  }

  @Test
  public void testGetAttributeMatchingWords() {
    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("attribute", "yes");
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("attribute", "no");

    words.add(w1);
    words.add(w2);

    List<Word> subset = PartitionUtils.getAttributeMatchingWords(words, "attribute", "yes");

    assertEquals(1, subset.size());
    assertTrue(subset.contains(w1));
  }

  @Test
  public void testGetAttributeMatchingWordsOKWithEmptyAttribute() {
    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("attribute", "yes");
    Word w2 = new Word(0, 1, "two");

    words.add(w1);
    words.add(w2);

    List<Word> subset = PartitionUtils.getAttributeMatchingWords(words, "attribute", "yes");

    assertEquals(1, subset.size());
    assertTrue(subset.contains(w1));
  }

  @Test
  public void testGenerateClassDistribution() {
    List<Word> words = new ArrayList<Word>();
    Word w1 = new Word(0, 1, "one");
    w1.setAttribute("attribute", "yes");
    Word w2 = new Word(0, 1, "two");
    w2.setAttribute("attribute", "no");

    Word w3 = new Word(0, 1, "three");
    w3.setAttribute("attribute", "no");

    words.add(w1);
    words.add(w2);
    words.add(w3);

    Distribution d = PartitionUtils.generateAttributeDistribution(words, "attribute");

    assertEquals(2, d.size());
    assertEquals(1.0, d.get("yes"));
    assertEquals(2.0, d.get("no"));
  }
}
