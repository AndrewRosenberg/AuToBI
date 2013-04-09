package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An AuToBIWordReader that is used to read IBM Formatted Keyword Search Posting Lists.
 * <p/>
 * These are formatted as XML files.
 */
public class PostingListReader extends AuToBIWordReader {
  private final String filename;  // The filename to read posting list results from
  private final String target_stem;
  // The source file containing the desired hits.  This indicates which audio file is being processed.

  public PostingListReader(String filename, String target_stem) {
    this.filename = filename;
    this.target_stem = target_stem;
  }

  @Override
  public List<Word> readWords() throws IOException, AuToBIException {
    AuToBIFileReader reader = new AuToBIFileReader(filename);
    List<Word> words = new ArrayList<Word>();

    String line;
    String kwid = "";
    String stem_pattern = "(.*?file=\")(.+?)(\".*$)";
    String start_pattern = "(.*?tbeg=\")(.+?)(\".*$)";
    String dur_pattern = "(.*?dur=\")(.+?)(\".*$)";

    String kwid_pattern = "(.*?kwid=\")(.+?)(\".*$)";
    int idx = 0;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("<detected_kwlist")) {
        kwid = line.replaceAll(kwid_pattern, "$2");
        idx = 0;
      }
      if (line.startsWith("<kw ")) {
        String stem = line.replaceAll(stem_pattern, "$2");
        // Make sure that the words contain their origin stem as their filename.
        if (stem.equals(target_stem)) {
          double dur = Double.parseDouble(line.replaceAll(dur_pattern, "$2"));
          double start = Double.parseDouble(line.replaceAll(start_pattern, "$2"));
          words.add(new Word(start, start + dur, kwid + "_" + idx, null, stem));
        }
        idx++;
      }
    }

    // Sort identified hits by time.
    Collections.sort(words, new Comparator<Word>() {
      public int compare(Word o1, Word o2) {
        return Double.compare(o1.getStart(), o2.getStart());
      }
    });

    reader.close();
    return words;
  }
}
