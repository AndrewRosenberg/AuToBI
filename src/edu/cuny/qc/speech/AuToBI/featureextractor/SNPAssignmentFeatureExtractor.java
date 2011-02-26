/*  SNPAssignmentFeatureExtractor.java

    Copyright (c) 2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.SpeakerNormalizationParameterGenerator;
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

  private String destination_feature;  // The name of the feature to store the SNPs on
  private String speaker_id_feature; // The name of the speaker identifier feature
  private HashMap<String, SpeakerNormalizationParameter> snp_map;  // map from speaker ids to features.

  /**
   * Constructs a new SNPAssignmentFeatureExtractor
   *
   * @param destination_feature The attribute name to hold the SNP
   * @param speaker_id_feature  The attribute containing the speaker identifier
   * @param snp_files           the serialized SNP files to load
   */
  public SNPAssignmentFeatureExtractor(String destination_feature, String speaker_id_feature, List<String> snp_files) {

    this.snp_map = new HashMap<String, SpeakerNormalizationParameter>();
    loadSNPList(snp_files);

    this.speaker_id_feature = speaker_id_feature;
    this.destination_feature = destination_feature;
    this.required_features.add(speaker_id_feature);
    this.extracted_features.add(destination_feature);
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

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (snp_map.containsKey(r.getAttribute(speaker_id_feature))) {
        r.setAttribute(destination_feature, snp_map.get(r.getAttribute(speaker_id_feature)));
      }
    }
  }
}
