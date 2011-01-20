package edu.cuny.qc.speech.AuToBI.core;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Test class for Spectrum.
 *
 * @see Spectrum
 */
public class SpectrumTest {

  @Test
  public void testGetEmptySliceBelowRange() {
    double[][] data = new double[10][10];

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = 0;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(0.1, 0.4);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetEmptySliceAboveRange() {
    double[][] data = new double[10][10];

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = 0;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(10.1, 10.4);
    } catch (AuToBIException e) {
      fail();
    }
  }
}
