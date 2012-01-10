/*  SpectrumExtractor.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.SampledDataAnalyzer;
import edu.cuny.qc.speech.AuToBI.core.Spectrum;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import jnt.FFT.RealDoubleFFT_Radix2;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * SpectrumExtractor is used to generate the spectrum from a wav file.
 */
public class SpectrumExtractor extends SampledDataAnalyzer {

  /**
   * Constructs a SpectrumExtractor object and attach it to wave data.
   *
   * @param wav the wave data to analyze.
   */
  public SpectrumExtractor(WavData wav) {
    this.wav = wav;
  }

  /**
   * Constructs a spectrogram for the the associated WavData.
   * <p/>
   * The frame_size parameter determines the sampling rate and frame size of the spectrogram. The hanning_window_size
   * describes the overlap across frames.
   * <p/>
   * Both of these parameters are described in seconds.
   * <p/>
   * A typical spectrogram has as 10ms framesize and a 50ms hanning window.
   *
   * @param frame_size          The frame size of the spectrogram
   * @param hanning_window_size The size of the hanning window
   * @return A two dimensional array of doubles containing the spectrogram.
   */
  public Spectrum getSpectrum(double frame_size, double hanning_window_size) {
    if (wav == null) {
      return null;
    }
    int frame_samples = (int) (wav.sampleRate * frame_size);
    int hanning_window_samples = (int) (wav.sampleRate * hanning_window_size);

    int n_frames = (wav.getNumSamples() - hanning_window_samples) / frame_samples;

    int starting_sample = hanning_window_samples / 2;

    double[] window = constructHanningWindow(hanning_window_samples);

    int nfft = 256;
    while (nfft < window.length)
      nfft *= 2;

    if (n_frames < 1) {
      return null;
    }

    double[][] spectrogram = new double[n_frames][nfft];

    for (int frame = 0; frame < n_frames; ++frame) {
      // hanning windowing
      double[] wave_data = getWindowedFrame(starting_sample, frame, frame_samples, hanning_window_samples);

      double[] windowed_sample = convolveWaveData(wave_data, window);
      windowed_sample = resizeArray(windowed_sample, nfft);

      // FFT windowed sample.
      RealDoubleFFT_Radix2 window_fft = new RealDoubleFFT_Radix2(nfft);
      window_fft.transform(windowed_sample);

      double[] spectrum = absoluteValueSquared(windowed_sample);
      spectrogram[frame] = spectrum;
    }

    double starting_time = starting_sample * wav.getFrameSize() + wav.t0;
    Spectrum return_value = new Spectrum(spectrogram, starting_time, frame_size, wav.sampleRate / (2 * nfft));
    return return_value;
  }

  /**
   * Convolves the wave data with a window function.
   *
   * @param wave_data The wave data
   * @param window    The window function
   * @return The convolved data
   */
  private double[] convolveWaveData(double[] wave_data, double[] window) {
    double[] convolved = new double[wave_data.length];
    for (int i = 0; i < window.length; ++i) {
      convolved[i] = wave_data[i] * window[i];
    }
    for (int i = window.length; i < wave_data.length; ++i) {
      convolved[i] = 0.0;
    }
    return convolved;
  }

  /**
   * Resizes an array of Complex objects.
   * <p/>
   * This is used to deliver an array with size = 2^d to the FFT operation
   *
   * @param data         The data
   * @param desired_size The desired size of the array
   * @return a new array of complex values
   */
  private double[] resizeArray(double[] data, int desired_size) {

    double[] array = new double[desired_size];
    for (int i = 0; i < Math.min(desired_size, data.length); ++i) {
      array[i] = data[i];
    }
    for (int i = data.length; i < desired_size; ++i) {
      array[i] = 0.0;
    }
    return array;
  }

  /**
   * Calculates the absolute value squared for each element of a array produced by RealDoubleFFT_Radix2
   *
   * @param data The complex array
   * @return an array of the same size containing the absolute value squared
   */
  private double[] absoluteValueSquared(double[] data) {
    double[] result = new double[data.length / 2];
    for (int i = 1; i < data.length / 2; ++i) {
      result[i] = data[i] * data[i] + data[data.length - i] * data[data.length - i];
    }
    return result;
  }


  public static void main(String[] args) {

    String filename = args[0];
    double frame_size = Double.parseDouble(args[1]);
    double hanning_window_size = Double.parseDouble(args[2]);

    WavReader reader = new WavReader();
    try {
      WavData wav = reader.read(filename);

      SpectrumExtractor extractor = new SpectrumExtractor(wav);

      Spectrum spectrum = extractor.getSpectrum(frame_size, hanning_window_size);

      for (int i = 0; i < spectrum.numFrames(); ++i) {
        for (int j = 0; j < spectrum.numFreqs(); ++j) {
          System.out.print(spectrum.get(i, j) + " ");
        }
        System.out.println("");
      }
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
