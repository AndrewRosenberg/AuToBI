package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/7/12 Time: 2:31 PM To change this template use File | Settings | File
 * Templates.
 */
public class KOHReader extends AuToBIWordReader {

  private String filename;

  public KOHReader(String filename) {
    this.filename = filename;
  }

  /**
   * Reads word boundaries and tobi labels from a koh formatted file.
   * <p/>
   * (1)occ_start_time (2)leaf_name (3)occ_index (4)context (5)vec_num_of_start (6)vec_num_of_end (7)occ_steps
   * (8)start_pitch (9)end_pitch (10)dur_leaf_name (11)en_leaf_name (12)lexeme (13)syllable (14)pitch accent
   * (15)HiF0_val (16)phrase accent and boundary tone (17)break index
   *
   * @return a list of words
   * @throws IOException     if there is a problem with the file reading
   * @throws AuToBIException if there is a problem with the tone alignment
   */
  @Override
  public List<Word> readWords() throws IOException, AuToBIException {

    AuToBIFileReader reader = new AuToBIFileReader(filename);

    List<Word> words = new ArrayList<Word>();
    String line;
    String prev_break_idx = null;
    while ((line = reader.readLine()) != null) {
      String data[] = line.trim().split(";");

      if (data.length == 17) {
        if (!data[11].trim().startsWith(".") && !data[11].trim().startsWith("~")) {
          String accent = data[13].trim();
          if (!accent.startsWith(".")) {
            accent = null;
          }
          String phrase_accent = data[15].trim();
          String break_idx = data[16].trim();
          //  strip the (\d+) out of the ortho label.
          String word = data[11].trim();
          word = word.substring(0, word.indexOf("("));
          Word w = new Word(0.0, 0.0, word, accent, filename);
          if (!phrase_accent.equals(".") && phrase_accent.contains("-")) {
            String phrase_accent_label = phrase_accent.substring(0, phrase_accent.indexOf("-") + 1);
            w.setPhraseAccent(phrase_accent_label.trim());

            if (phrase_accent.contains("%")) {
              String boundary_tone = phrase_accent.substring(phrase_accent.indexOf("-") + 1, phrase_accent.length());
              if (boundary_tone.trim().length() > 0) {
                w.setBoundaryTone(boundary_tone.trim());
              }
            }
          }
          w.setBreakAfter(break_idx);
          w.setBreakBefore(prev_break_idx);
          words.add(w);
          prev_break_idx = break_idx;
        }
      }
    }

    return words;
  }
}
