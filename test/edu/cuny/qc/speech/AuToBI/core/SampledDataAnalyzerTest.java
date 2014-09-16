/*  SampledDataAnalyzerTest.java

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

import edu.cuny.qc.speech.AuToBI.util.SignalProcessingUtils;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests for PitchCandidate
 */
public class SampledDataAnalyzerTest {

  @Test
  public void testIndexToX() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };

    assertEquals(7.0, sda.indexToX(1.0, 2.0, 3));
  }

  @Test
  public void testXToLowIndex() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };

    assertEquals(3, sda.xToLowIndex(1.0, 2.0, 7.1));
  }

  @Test
  public void testXToHighIndex() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };

    assertEquals(4, sda.xToHighIndex(1.0, 2.0, 7.1));
  }

  @Test
  public void testXToNearestIndexDown() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };

    assertEquals(3, sda.xToNearestIndex(1.0, 2.0, 7.1));
  }

  @Test
  public void testXToNearestIndexUp() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };

    assertEquals(4, sda.xToNearestIndex(1.0, 2.0, 8.01));
  }

  @Test
  public void testShortTermAnalysis() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };
    sda.wav = new WavData();
    sda.wav.t0 = 0.0;
    sda.wav.sampleRate = (float) 1.0;
    sda.wav.samples = new double[][]{{0.0, 1.0, 2.0, 3.0, 4.0, 5.0}};

    assertEquals((Integer) 3, sda.getNFramesAndStartTime(2.0, 2.0).first);
    assertEquals(0.5, sda.getNFramesAndStartTime(2.0, 2.0).second, 0.001);
  }

  @Test
  public void testGetWindowedFrame() {
    int starting_sample = 4;
    int frame_index = 1;
    int frame_samples = 2;
    int window_samples = 3;

    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };
    sda.wav = new WavData();
    sda.wav.t0 = 0.0;
    sda.wav.sampleRate = (float) 1.0;
    sda.wav.samples = new double[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}};

    double[] frame = sda.getWindowedFrame(starting_sample, frame_index, frame_samples, window_samples);

    assertEquals(window_samples, frame.length);
    assertArrayEquals(new double[]{5, 6, 7}, frame, 0.00001);
  }

  @Test
  public void testGetHanningWindow() {
    SampledDataAnalyzer sda = new SampledDataAnalyzer() {
    };
    double[] window = SignalProcessingUtils.constructHanningWindow(7);
    assertArrayEquals(new double[]{0.0, 0.25, 0.75, 1.0, 0.75, 0.25, 0.0}, window, 0.0001);
  }

}

