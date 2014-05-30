/*  SpeakerNormalizationParameterTest.java

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for PitchCandidate
 */
public class SpeakerNormalizationParameterTest {

  @Test
  public void testEmptyConstructorInitialization() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    assertEquals("", snp.getSpeakerId());
    assertEquals("f0: mean 0.0 - stdev 0.0\n" +
        "I: mean 0.0 - stdev 0.0", snp.toString());
  }

  @Test
  public void testNamedConstructorInitialization() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter("speaker_test");

    assertEquals("speaker_test", snp.getSpeakerId());
    assertEquals("f0: mean 0.0 - stdev 0.0\n" +
        "I: mean 0.0 - stdev 0.0", snp.toString());
  }

  @Test
  public void testInsertPitch() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    snp.insertPitch(1.0);
    assertEquals("f0: mean 1.0 - stdev 0.0\n" +
        "I: mean 0.0 - stdev 0.0", snp.toString());
  }

  @Test
  public void testInsertPitchContour() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    Contour p = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});
    snp.insertPitch(p);
    assertEquals("f0: mean 2.0 - stdev 1.0\n" +
        "I: mean 0.0 - stdev 0.0", snp.toString());
  }

  @Test
  public void testInsertPitchAlsoAdjustsLogPitch() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    snp.insertPitch(1.0);
    snp.insertPitch(5.0);
    assertEquals(1.92899409250466, snp.normalize("log[f0]", 3.0), 0.0001);
  }

  @Test
  public void testInsertIntensity() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    snp.insertIntensity(1.0);
    assertEquals("f0: mean 0.0 - stdev 0.0\n" +
        "I: mean 1.0 - stdev 0.0", snp.toString());
  }

  @Test
  public void testInsertIntensityContour() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    Contour i = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});
    snp.insertIntensity(i);
    assertEquals("f0: mean 0.0 - stdev 0.0\n" +
        "I: mean 2.0 - stdev 1.0", snp.toString());
  }

  @Test
  public void testInsertIntensityAdjustsLogIntensity() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    snp.insertIntensity(1.0);
    snp.insertIntensity(5.0);
    assertEquals(1.92899409250466, snp.normalize("log[I]", 3.0), 0.0001);
  }

  @Test
  public void testCanNormalizePitchLogPitchIntensityLogIntensity() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    assertTrue(snp.canNormalize("f0"));
    assertTrue(snp.canNormalize("log[f0]"));
    assertTrue(snp.canNormalize("I"));
    assertTrue(snp.canNormalize("log[I]"));
  }

  @Test
  public void testNormalizeWorksCorrectly() {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();

    snp.insertIntensity(1.0);
    snp.insertIntensity(5.0);
    assertEquals(0.0, snp.normalize("I", 3.0), 0.0001);
  }
}

