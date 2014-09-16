/*  Word.java

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
 * Word are Regions that have a number of ToBI based variables defined.
 */
public class Word extends Region {
  private String accent;  // null if this word is not accented
  private double accent_time;  // -1 if this word is not accented
  private String break_before;  // null if there is no break before this word
  private String break_after;  // null if there is no break after this word
  private String phrase_accent; // the phrase accent (if any)
  private String boundary_tone;  // the boudnary tone (if any)

  /**
   * Constructs a new Word.
   *
   * @param start    the start time
   * @param end      the end time
   * @param text     the orthography of the word
   * @param accent   an associated pitch accent label.
   * @param filename the source file of the word.
   */
  public Word(double start, double end, String text, String accent, String filename) {
    super(start, end, text, filename);
    setAccent(accent);
    this.accent_time = -1;
  }

  /**
   * Constructs a new Word.
   *
   * @param start  the start time
   * @param end    the end time
   * @param label  the orthography of the word
   * @param accent an associated pitch accent label.
   */
  public Word(double start, double end, String label, String accent) {
    this(start, end, label, accent, null);
  }

  /**
   * Constructs a new Word.
   *
   * @param start the start time
   * @param end   the end time
   * @param label the orthography of the word
   */
  public Word(double start, double end, String label) {
    this(start, end, label, null);
  }

  /**
   * Constructs a new Word from an existing Word.
   *
   * @param w the existing word
   */
  public Word(Word w) {
    super(w);
    accent = w.accent;
    accent_time = w.accent_time;
    break_before = w.break_before;
    break_after = w.break_after;
    phrase_accent = w.phrase_accent;
    boundary_tone = w.boundary_tone;
  }

  /**
   * Retrieves the accent label.
   *
   * @return the accent label
   */
  public String getAccent() {
    return accent;
  }

  /**
   * Sets the accent label.
   * <p/>
   * The accent string is trimmed before assignment.
   *
   * @param accent the accent string
   */
  public void setAccent(String accent) {
    if (accent != null) {
      accent = accent.trim();
    }
    this.accent = accent;
  }

  /**
   * Determines if the word has a non empty pitch accent label.
   *
   * @return true if the word is accented, false otherwise.
   */
  public boolean isAccented() {
    return (accent != null && accent.length() > 0);
  }

  /**
   * Gets the time of the accent annotation.
   *
   * @return the accent time
   */
  public double getAccentTime() {
    return accent_time;
  }

  /**
   * Setst the accent time
   *
   * @param accent_time the accent time
   */
  public void setAccentTime(double accent_time) {
    this.accent_time = accent_time;
  }

  /**
   * Retrieves the preceding break index.
   *
   * @return the preceding break index.
   */
  public String getBreakBefore() {
    return break_before;
  }

  /**
   * Sets the preceding break.
   *
   * @param break_before the preceding break
   */
  public void setBreakBefore(String break_before) {
    this.break_before = break_before;
  }

  /**
   * Retrieves the following break index.
   *
   * @return the following break index.
   */
  public String getBreakAfter() {
    return break_after;
  }

  /**
   * Sets the following break.
   *
   * @param break_after the following break
   */
  public void setBreakAfter(String break_after) {
    this.break_after = break_after;
  }

  /**
   * Constructs a string representation of the Word.
   *
   * @return the string representation
   */
  public String toString() {
    return super.toString() + "[" + break_before + ", " + break_after + "]" +
        (accent == null ? "" : " " + accent + " " + accent_time);
  }

  /**
   * Determines if the word is intonational phrase final.
   *
   * @return true if the word is intonational phrase final
   */
  public boolean isIntonationalPhraseFinal() {
    return (getBreakAfter() != null) && (getBreakAfter().equals("4-") || getBreakAfter().equals("4"));
  }

  /**
   * Determines if the word is intermediate phrase final.
   *
   * @return true if the word is intermediate phrase final
   */
  public boolean isIntermediatePhraseFinal() {
    return (getBreakAfter() != null) && (getBreakAfter().startsWith("4") || getBreakAfter().startsWith("3"));
  }

  /**
   * Sets a new phrase accent.
   *
   * @param phrase_accent the new phrase accent
   */
  public void setPhraseAccent(String phrase_accent) {
    this.phrase_accent = phrase_accent;
  }

  /**
   * Sets a new boundary tone.
   *
   * @param boundary_tone the new boundary tone
   */
  public void setBoundaryTone(String boundary_tone) {
    this.boundary_tone = boundary_tone;
  }

  /**
   * Retrieves the phrase accent.
   *
   * @return the phrase accent
   */
  public String getPhraseAccent() {
    return phrase_accent;
  }

  /**
   * Retrieves the boundary tone.
   *
   * @return the boundary tone
   */
  public String getBoundaryTone() {
    return boundary_tone;
  }

  /**
   * Determines if the word has a non-empty phrase accent
   *
   * @return true if the word has a phrase accent
   */
  public boolean hasPhraseAccent() {
    return (phrase_accent != null && phrase_accent.length() > 0);
  }

  /**
   * Determines if the word has a non-empty boundary tone
   *
   * @return true if the word has a boundary tone
   */
  public boolean hasBoundaryTone() {
    return (boundary_tone != null && boundary_tone.length() > 0);
  }
}
