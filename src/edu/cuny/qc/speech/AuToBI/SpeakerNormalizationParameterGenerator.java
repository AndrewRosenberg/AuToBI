/*  SpeakerNormalizationParameterGenerator.java

    Copyright (c) 2009-2014 Andrew Rosenberg

     This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * SpeakerNormalizationParameterGenerator is a tool to construct speaker normalization parameters given a set of wav
 * files.
 */
public class SpeakerNormalizationParameterGenerator {

  public SpeakerNormalizationParameter generateNormalizationParameters(List<WavData> wavs) {
    return generateNormalizationParameter(wavs, "");
  }

  /**
   * Generates new speaker normalization parameters based on a set of wav files from a given speaker.
   *
   * @param wavs       the wave data to generate normalization data from
   * @param speaker_id an identifier for the speaker
   * @return the SpeakernNormalizationParameter
   */
  public SpeakerNormalizationParameter generateNormalizationParameter(List<WavData> wavs, String speaker_id) {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter(speaker_id);
    extendSpeakerNormalizationParameter(wavs, snp);
    return snp;
  }

  /**
   * Inserts additional data into an existing SpeakerNormalizationParameter
   *
   * @param wavs the wav data to add to the normalization data
   * @param snp  the existing parameters to add to
   */
  private void extendSpeakerNormalizationParameter(List<WavData> wavs, SpeakerNormalizationParameter snp) {
    for (WavData wav : wavs) {
      RAPTPitchExtractor pe = new RAPTPitchExtractor();

      try {
        Contour pitches = pe.getPitch(wav);
        for (Pair<Double, Double> pitch : pitches) {
          snp.insertPitch(pitch.second);
        }
      } catch (AuToBIException e) {
        e.printStackTrace();
      }

      IntensityExtractor ie = new IntensityExtractor(wav);

      Contour intensities = ie.soundToIntensity();
      for (Pair<Double, Double> intensity : intensities) {
        snp.insertIntensity(intensity.second);
      }
    }
  }

  /**
   * Reads a serialized SpeakerNormalizationParameter object.
   *
   * @param filename the file storing the SpeakerNormalizationParameter.
   * @return the SpeakerNormalizationParameter object
   */
  public static SpeakerNormalizationParameter readSerializedParameters(String filename) {
    FileInputStream fis;
    ObjectInputStream in;
    try {
      fis = new FileInputStream(filename);
      in = new ObjectInputStream(fis);
      Object o = in.readObject();
      if (o instanceof SpeakerNormalizationParameter) {
        return (SpeakerNormalizationParameter) o;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Generates a SpeakerNormalizationParameter object from a single WavData object, and a corresponding speaker_id.
   *
   * @param wav        the wave data to analyze
   * @param speaker_id the speaker id.
   * @return a SpeakerNormalizationParameter object containing parameters based on a single wave file
   */
  public SpeakerNormalizationParameter generateNormalizationParameter(WavData wav, String speaker_id) {
    List<WavData> list = new ArrayList<WavData>();
    list.add(wav);
    return generateNormalizationParameter(list, speaker_id);
  }

  public static void main(String[] args) {
    AuToBI autobi = new AuToBI();
    autobi.init(args);

    try {
      String speaker_id = autobi.getParameter("speaker_id");
      String output_file = autobi.getParameter("output_file");
      SpeakerNormalizationParameter norm_param = new SpeakerNormalizationParameter(speaker_id);

      WavReader reader = new WavReader();
      for (String filename : AuToBIUtils.glob(autobi.getParameter("wav_files"))) {
        AuToBIUtils.info("processing file: " + filename);
        WavData wav = reader.read(filename);
        RAPTPitchExtractor pitch_extractor = new RAPTPitchExtractor();
        IntensityExtractor intensity_extractor = new IntensityExtractor(wav);

        Contour pitch_values = pitch_extractor.getPitch(wav);
        Contour intensity_values = intensity_extractor.soundToIntensity();

        norm_param.insertPitch(pitch_values);
        norm_param.insertIntensity(intensity_values);
      }

      AuToBIUtils.info("Successfully generated normalization parameters.");

      AuToBIUtils.info("Generated Parameters: " + norm_param);

      // Serialize parameters
      FileOutputStream fos;
      ObjectOutputStream out;
      fos = new FileOutputStream(output_file);
      out = new ObjectOutputStream(fos);
      out.writeObject(norm_param);
      out.close();

    } catch (AuToBIException e) {
      e.printStackTrace();
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
