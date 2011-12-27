/*  WavDataTest.java

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

import static junit.framework.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Test class for WavData.
 *
 * @see WavData
 */
public class WavDataTest {
  @Test
  public void testEmptyConstructor() {
    WavData wav = new WavData();

    assertEquals(0.0, wav.t0);
  }

  @Test
  public void testGetDuration() {
    WavData wav = new WavData();
    wav.sampleRate = (float) 100;
    wav.samples = new double[][]{{0.0, 1.0, 2.0}};

    assertEquals(0.03, wav.getDuration(), 0.0001);
  }

  @Test
  public void testGetFrameSize() {
    WavData wav = new WavData();
    wav.sampleRate = (float) 100;
    wav.samples = new double[][]{{0.0, 1.0, 2.0}};

    assertEquals(0.01, wav.getFrameSize(), 0.0001);
  }

  @Test
  public void testGetNumSamples() {
    WavData wav = new WavData();
    wav.sampleRate = (float) 100;
    wav.samples = new double[][]{{0.0, 1.0, 2.0}};

    assertEquals(3, wav.getNumSamples());
  }

  @Test
  public void testGetSample() {
    WavData wav = new WavData();
    wav.sampleRate = (float) 100;
    wav.samples = new double[][]{{0.0, 1.0, 2.0}};

    assertEquals(2.0, wav.getSample(0, 2));
  }

  @Test
  public void testGetSamples() {
    WavData wav = new WavData();
    wav.sampleRate = (float) 100;
    wav.samples = new double[][]{{0.0, 1.0, 2.0}};

    assertArrayEquals(new double[]{0.0, 1.0, 2.0}, wav.getSamples(0), 0.0001);
  }

  @Test
  public void testSetAndGetFilename() {
    WavData wav = new WavData();

    wav.setFilename("/test/file.txt");
    assertEquals("/test/file.txt", wav.getFilename());
  }
}
