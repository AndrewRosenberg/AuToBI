/*  AggregationTest.java

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
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Test Class for Aggregation.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.Aggregation
 */
public class AggregationTest {

  @Test
  public void testCreateEmptyAggregation() {
    Aggregation agg = new Aggregation();

    assertEquals(0.0, agg.getMean(), 0.0001);
    assertEquals(0.0, agg.getStdev(), 0.0001);
    assertEquals(0.0, agg.getSize(), 0.0001);
    assertEquals(-Double.MAX_VALUE, agg.getMax(), 0.0001);
    assertEquals(Double.MAX_VALUE, agg.getMin(), 0.0001);
  }

  @Test
  public void testCreateLabeledAggregation() {
    Aggregation agg = new Aggregation("test");

    assertEquals("test", agg.getLabel());
  }

  @Test
  public void testSetLabel() {
    Aggregation agg = new Aggregation();

    agg.setLabel("test");
    assertEquals("test", agg.getLabel());
  }

  @Test
  public void testInsertIntoAggregation() {
    Aggregation agg = new Aggregation();

    agg.insert(2.0);

    assertEquals(2.0, agg.getMean(), 0.0001);
    assertEquals(0.0, agg.getStdev(), 0.0001);
    assertEquals(0.0, agg.getVariance(), 0.0001);
    assertEquals(1.0, agg.getSize(), 0.0001);
    assertEquals(2.0, agg.getMax(), 0.0001);
    assertEquals(2.0, agg.getMin(), 0.0001);
  }

  @Test
  public void testRemoveLowersCount() {
    Aggregation agg = new Aggregation();

    agg.insert(2.0);

    agg.remove(2.0);
    assertEquals(0, agg.getSize(), 0.0001);
  }

  @Test
  public void testRemoveMaximum() {
    Aggregation agg = new Aggregation();

    agg.insert(2.0);

    agg.remove(2.0);
    assertTrue(Double.isNaN(agg.getMax()));
  }

  @Test
  public void testRemoveMinimum() {
    Aggregation agg = new Aggregation();

    agg.insert(2.0);

    agg.remove(2.0);
    assertTrue(Double.isNaN(agg.getMin()));
  }

  @Test
  public void testInsertCollection() {
    Aggregation agg = new Aggregation();
    List<Double> list = new ArrayList<Double>();
    list.add(1.0);
    list.add(2.0);
    agg.insert(list);
    assertEquals(2, agg.getSize());
    assertEquals(1.5, agg.getMean(), 0.0001);
  }

  @Test
  public void testRemoveCollection() {
    Aggregation agg = new Aggregation();
    List<Double> list = new ArrayList<Double>();
    agg.insert(1.0);
    agg.insert(2.0);

    list.add(1.0);
    list.add(2.0);
    agg.remove(list);
    assertEquals(0, agg.getSize(), 0.0001);
    assertEquals(0.0, agg.getMean(), 0.0001);
  }

  @Test
  public void testGetMean() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(3.0, agg.getMean(), 0.0001);
  }

  @Test
  public void testGetStdDev() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(2.82842, agg.getStdev(), 0.0001);
  }

  @Test
  public void testGetVariance() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(8.0, agg.getVariance(), 0.0001);
  }

  @Test
  public void testEvaluateGaussianPDF() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(0.14104775, agg.evaluateGaussianPDF(3.0), 0.0001);
  }

  @Test
  public void testEvaluateGaussianCDF() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(0.57015828, agg.evaluateGaussianCDF(3.5), 0.0001);
  }

  @Test
  public void testGetRMS() {
    Aggregation agg = new Aggregation();
    agg.insert(5.0);
    agg.insert(1.0);

    assertEquals(3.6055512, agg.getRMS(), 0.0001);
  }
}


