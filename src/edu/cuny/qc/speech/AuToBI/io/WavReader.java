/*  WavReader.java

    Copyright 2009-2010 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.WavData;

import javax.sound.sampled.*;

import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * WavReader is used to read Wave files from into memory.
 */
public class WavReader {
  /**
   * Constructs a WavData object from the wav file pointed to by filename.
   * <p/>
   * Note AuToBI currently only supports 16bit wave files.
   *
   * @param filename the filename to read
   * @return The wav data stored in the file.
   * @throws IOException                   if there is a file reading problem
   * @throws UnsupportedAudioFileException if there is a problem with the audio file format
   * @throws AuToBIException               if the file is not 16 bit
   */
  public WavData read(String filename)
      throws UnsupportedAudioFileException, IOException, AuToBIException {
    File file = new File(filename);
    if (!file.exists()) {
      throw new AuToBIException("Wav file does not exist: " + filename);
    }
    AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));

    return read(soundIn);
  }

  /**
   * Constructs a WavData object from the wav file pointed to by filename with specified start and end times.  The
   * original time information is *not* preserved.
   * <p/>
   * Note AuToBI currently only supports 16bit wave files.
   *
   * @param filename the filename to read
   * @return The wav data stored in the file.
   * @throws IOException                   if there is a file reading problem
   * @throws UnsupportedAudioFileException if there is a problem with the audio file format
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *                                       if the file is not 16 bit
   */
  public WavData read(String filename, Double start, Double end)
      throws UnsupportedAudioFileException, IOException, AuToBIException {
    File file = new File(filename);
    AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));

    return read(soundIn, start, end);
  }


  /**
   * Reads an entire wave file from an AudioInputStream
   *
   * @param stream the AudioInputStream containing the audio.
   * @return the wave data
   * @throws AuToBIException if the file does not use 16bit samples.
   */
  public WavData read(AudioInputStream stream) throws AuToBIException {
    return read(stream, 0.0, null);
  }


  /**
   * Reads the data from an AudioInputStream.
   *
   * @param stream the AudioInputStream containing the audio.
   * @param start  The start time to read in seconds
   * @param end    The end time to read in seconds
   * @return the wave data
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          if there is an IO problem.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public WavData read(AudioInputStream stream, Double start, Double end) throws AuToBIException {
    WavData data = new WavData();

    data.numberOfChannels = stream.getFormat().getChannels();
    data.sampleSize = stream.getFormat().getSampleSizeInBits();
    data.sampleRate = stream.getFormat().getSampleRate();

    // Read raw data from stream
    byte[] bytes;
    if (start == null || end == null) {
      bytes = new byte[(int) stream.getFrameLength() * stream.getFormat().getFrameSize()];
      try {
        stream.read(bytes);
      } catch (IOException e) {
        throw new AuToBIException(e.getMessage());
      }
    } else {
      int start_sample = ((int) Math.floor(start * stream.getFormat().getFrameRate()));
      int end_sample = ((int) Math.ceil(end * stream.getFormat().getFrameRate()));
      int length = end_sample - start_sample;


      bytes = new byte[length * stream.getFormat().getFrameSize()];

      try {
        stream.skip(start_sample);
        stream.read(bytes, 0, length * stream.getFormat().getFrameSize());
      } catch (IOException e) {
        throw new AuToBIException(e.getMessage());
      }
    }


    // Wave files are by default little endian.  Currently does not support big endian formatted wave files.
    // Convert endian-ness of raw data to sample data
    // Currently only supports 16-bit raw_samples
    int[][] raw_samples = new int[data.numberOfChannels][bytes.length / (2 * data.numberOfChannels)];
    data.samples = new double[data.numberOfChannels][bytes.length / (2 * data.numberOfChannels)];

    int i = 0;
    int index = 0;
    while (i < bytes.length) {
      for (int channel = 0; channel < data.numberOfChannels; ++channel) {
        int low = (int) bytes[i];
        ++i;
        int high = (int) bytes[i];
        ++i;

        int sample = (high << 8) + (low & 0x00ff);
        raw_samples[channel][index] = sample;
      }
      ++index;
    }

    for (int channel = 0; channel < raw_samples.length; ++channel) {
      for (int j = 0; j < raw_samples[channel].length; ++j) {
        data.samples[channel][j] = raw_samples[channel][j] * 1.0 / (1 << (data.sampleSize - 1));
      }
    }
    return data;
  }
}
