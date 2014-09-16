/*  SubWord.java

    Copyright 2009-2014 Andrew Rosenberg
   
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