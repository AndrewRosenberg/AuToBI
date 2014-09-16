/*  SNPAssignmentFeatureExtractor.java

    Copyright (c) 2010-2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.SpeakerNormalizationParameterGenerator;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.HashMap;
import java.util.List;

/**
 * A feature extractor to assign speaker normalization parameters (SNPs) to each word for acoustic speaker
 * normalization.
 */
public class SNPAssignmentFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spkrNormParamsFromFile";

  private String destination_feature;  // The name of the feature to store the SNPs on
  private String speaker_id_feature; // The name of the speaker identifier feature
  private HashMap<String, SpeakerNormalizationParameter> snp_map;  // map from speaker ids to features.
  private SpeakerNormalizationParameter default_snp = null;

  /**
   * Constructs a new SNPAssignmentFeatureExtractor.
   * <p/>
   * This has two operating modes.  In one mode, the speaker normalization parameters are assigned to regions without
   * regard for the speaker id feature of the regions.
   * <p/>
   * This happens when a new corpus is read and the Reader does not know how to correctly set the speaker_id parameter.
   * In this case, a single snp_file should be delivered to this constructor, and the contained parameters will be
   * assigned to all regions during feature extraction.
   * <p/>
   * The second operating mode loads a set of speaker normalization paramaters and assigns them to regions by matching
   * the stored speaker_id parameter on the parameterization files with those set by the Reader when the regions are
   * constructed.
   *
   * @param speaker_id_feature The attribute containing the speaker identifier
   * @param snp_files          the serialized SNP files to load
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException If speaker id feature is null and there are more snp
   *                                                        files set
   */
  public SNPAssignmentFeatureExtractor(String speaker_id_feature, List<String> snp_files)
      throws AuToBIException {

    if (snp_files.size() > 1 && speaker_id_feature == null) {
      throw new AuToBIException(
          "Without known speaker_id values, only one speaker normalization parameter file can be loaded.");
    }

    this.speaker_id_feature = speaker_id_feature;
    this.destination_feature = moniker;
    this.extracted_features.add(destination_feature);

    if (speaker_id_feature == null) {
      this.default_snp = SpeakerNormalizationParameterGenerator.readSerializedParameters(snp_files.get(0));
      this.snp_map = null;
    } else {
      this.default_snp = null;
      this.snp_map = new HashMap<String, SpeakerNormalizationParameter>();
      loadSNPList(snp_files);
      this.required_features.add(speaker_id_feature);
    }
  }

  /**
   * Loads a list of serialized SpeakerNormalizationParameter files.
   *
   * @param files the SNP files to load.
   */
  private void loadSNPList(List<String> files) {
    for (String filename : files) {
      SpeakerNormalizationParameter snp = SpeakerNormalizationParameterGenerator.readSerializedParameters(filename);

      snp_map.put(snp.getSpeakerId(), snp);
    }
  }

  /**
   * Assigns speaker normalization parameters to regions.
   * <p/>
   * If there is a speaker_id_feature set, then parameters are assigned based on the speaker_id feature and the
   * speaker_id feature associated with the speaker normalization parameters at generation.
   * <p/>
   * If there is no speaker_id feature set, all regions will be assigned the same parameter, loaded during
   * construction.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (speaker_id_feature != null) {
        if (snp_map.containsKey(r.getAttribute(speaker_id_feature).toString())) {
          r.setAttribute(destination_feature, snp_map.get(r.getAttribute(speaker_id_feature).toString()));
        }
      } else {
        r.setAttribute(destination_feature, default_snp);
      }
    }
  }
}
