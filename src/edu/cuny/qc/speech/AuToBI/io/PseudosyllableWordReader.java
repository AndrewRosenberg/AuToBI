package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.Syllabifier;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.ContourFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.IntensityFeatureExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PseudosyllableWordReader uses the AuToBIWordReader mechanisms to generate "word" boundaries using acoustic based
 * pseudosyllabification from a wav file.
 * <p/>
 * The syllabified is based on the approach described in Villing et al. (2004) Automatic Blind Syllable Segmentation for
 * Continuous Speech In: Irish Signals and Systems Conference 2004, 30 June - 2 July 2004, Queens University, Belfast.
 *
 * @see edu.cuny.qc.speech.AuToBI.Syllabifier
 * @see AuToBIWordReader
 */
public class PseudosyllableWordReader extends AuToBIWordReader {
  private WavData wav_data;   // the audio material to base the segmentation on
  private double threshold;   // the silence threshold in mean dB in the region
  private String annotation_file;  // A file containing ToBI annotations.

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a silence threshold, threshold.
   *
   * @param wav_data  source audio material.
   * @param threshold the silence threshold in mean dB
   */
  public PseudosyllableWordReader(WavData wav_data, double threshold) {
    this.wav_data = wav_data;
    this.threshold = threshold;
    this.annotation_file = null;
  }

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a default silence threshold of 10dB.
   *
   * @param wav_data source audio material.
   */
  public PseudosyllableWordReader(WavData wav_data) {
    this.wav_data = wav_data;
    this.threshold = 10.0;
    this.annotation_file = null;
  }

  /**
   * Gets the silence threshold value (mean db over the region)
   *
   * @return the silence threshold
   */
  public double getThreshold() {
    return threshold;
  }

  /**
   * Sets the silence threshold value  (mean db over the region)
   *
   * @param threshold the silence threshold
   */
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }


  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    Syllabifier syllabifier = new Syllabifier();
    List<Region> regions = syllabifier.generatePseudosyllableRegions(wav_data);
    for (Region r : regions) {
      r.setAttribute("wav", wav_data);
    }
    List<Word> words = new ArrayList<Word>();

    // Identify silent regions by mean intensity less than the threshold.
    IntensityFeatureExtractor ife = new IntensityFeatureExtractor("I");
    ContourFeatureExtractor cfe = new ContourFeatureExtractor("I");
    try {
      ife.extractFeatures(regions);
      cfe.extractFeatures(regions);
    } catch (FeatureExtractorException e) {
      throw new AuToBIException("Error extracting intensity: " + e.getMessage());
    }

    for (Region r : regions) {
      if ((Double) r.getAttribute("I__mean") >= threshold) {
        words.add(new Word(r.getStart(), r.getEnd(), "", "", wav_data.getFilename()));
      }
    }

    if (annotation_file != null) {
      // TODO: include a way to read ToBI annotations from an annotation file and align to the pseudosyllable words.
      // Sample code:
//      TextGridReader reader = new TextGridReader(annotation_file);
//      reader.readWords();
//      Tier tones_tier = reader.getTonesTier();
      // do the alignment.
    }
    return words;
  }
}
