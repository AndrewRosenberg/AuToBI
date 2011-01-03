/*  SubWord.java

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
package edu.cuny.qc.speech.AuToBI.core;

/**
 * Subword is used to describe subword regions, such as syllables.
 * <p/>
 * The object is simply a subclass of Word, with the ability to link the Word of which the given subword region is
 * contained within.
 * <p/>
 * Note, while the intent for this class is that the boundaries of the subword region are completely contained within
 * the linked Word, no enforcement of this policy is performed.  Therefore, the user has both the flexibility of
 * linking regions that are not strictly "subregions" to a given Word object as SubWords.  Along with this, however,
 * comes the responsibility to confirm that feature extraction routines and other processing are not operating on an
 * stricter subword assumption.
 *
 * @see Word
 */
public class SubWord extends Word {
  protected Word word;  // the containing word

  /**
   * Retrieves the containing word.
   *
   * @return the word
   */
  public Word getWord() {
    return word;
  }

  /**
   * Sets the containing word.
   *
   * @param word the containing word
   */
  public void setWord(Word word) {
    this.word = word;
  }

  /**
   * Constructs a new SubWord.
   *
   * @param start     the start time of the region
   * @param end       the end time of the region
   * @param label     a label for the subword
   * @param accent    an accent label
   * @param file_name the filename from which the subword was derived.
   */
  public SubWord(double start, double end, String label, String accent, String file_name) {
    super(start, end, label, accent, file_name);
  }

  /**
   * Constructs a new SubWord.
   *
   * @param start  the start time of the region
   * @param end    the end time of the region
   * @param label  a label for the subword
   * @param accent an accent label
   */
  public SubWord(double start, double end, String label, String accent) {
    super(start, end, label, accent);
  }

  /**
   * Constructs a new SubWord.
   *
   * @param start the start time of the region
   * @param end   the end time of the region
   * @param label a label for the subword
   */
  public SubWord(double start, double end, String label) {
    super(start, end, label);
  }

  /**
   * Constructs a string representation of the subregion.
   *
   * @return the string representation describing the subword and its containing word.
   */
  public String toString() {
    return super.toString() + " within " + word.toString();
  }
}