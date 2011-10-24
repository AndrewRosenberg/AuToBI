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
  private double threshold;  // the silence threshold in mean dB in the region

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a silence threshold, threshold.
   *
   * @param wav_data   source audio material.
   * @param threshold the silence threshold in mean dB
   */
  public PseudosyllableWordReader(WavData wav_data, double threshold) {
    this.wav_data = wav_data;
    this.threshold = threshold;
  }

  /**
   * Constructs a new PseudosyllableWordReader based on audio data, wav_data, and a default silence threshold of 10dB.
   *
   * @param wav_data   source audio material.
   */
  public PseudosyllableWordReader(WavData wav_data) {
    this.wav_data = wav_data;
    this.threshold = 10.0;
  }

  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    Syllabifier syllabifier = new Syllabifier();
    List<Region> regions = syllabifier.generatePseudosyllableRegions(wav_data);
    List<Word> words = new ArrayList<Word>();

    // Identify silent regions by mean intensity less than the threshold.
    IntensityFeatureExtractor ife = new IntensityFeatureExtractor(wav_data, "I");
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

    return words;
  }
}
