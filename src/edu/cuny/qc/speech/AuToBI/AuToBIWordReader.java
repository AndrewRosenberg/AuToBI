package edu.cuny.qc.speech.AuToBI;

import java.io.IOException;
import java.util.List;

/**
 * An abstract class to govern the reading of a variety of input file types.
 */
public abstract class AuToBIWordReader {
  public String silence_regex = null;  // A regular expression to match against silence

  /**
   * Generate a list of words from an appropriate set of input files.
   *
   * @return A list of words
   * @throws IOException     If there is something wrong with the file reading
   * @throws AuToBIException If there is a formatting problem
   */
  abstract public List<Word> readWords() throws IOException, AuToBIException;

  /**
   * Sets the silence regular expression
   *
   * @param regex the silence regular expression.
   */
  public void setSilenceRegex(String regex){
    this.silence_regex = regex;
  }

  /**
   * Retrieves the silence regular expression
   *
   * @return the regular expression
   */
  public String getSilenceRegex() {
    return this.silence_regex;
  }
}
