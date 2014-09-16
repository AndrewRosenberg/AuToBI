/*  NXTTier.java

    Copyright 2012-14 Andrew Rosenberg

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


package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains and reads information from NXT Annotation of the Switchboard Corpus.  NXT annotations are stored in xml.
 * <p/>
 * Currently, we only use a subset of these annotations.  Rather than performing a full xml parse, pull out only the
 * relevant information relating to ToBI tone annotations and word breaks.
 */
public class NXTTier extends Tier {

  /**
   * Reads the information from the reader into the Tier regions.
   *
   * @param reader the reader to read from
   * @throws java.io.IOException     if there is an input output problem
   * @throws NXTSyntaxErrorException If there is a formatting problem
   */
  public void readTier(AuToBIFileReader reader) throws NXTSyntaxErrorException, IOException {
    String line;
    Region prev_r = null;
    while ((line = reader.readLine()) != null) {
      if (line.contains("<word")) {
        Region r = parseWord(line);
        if (r != null) {
          if (prev_r != null && r.getStart() == Double.NaN) {
            prev_r.setEnd(r.getEnd());
            prev_r.setLabel(prev_r.getLabel() + r.getLabel());
            this.regions.add(prev_r);
            prev_r = null;
          } else if (Double.isNaN(r.getEnd())) {
            prev_r = r;
          } else {
            this.regions.add(parseWord(line));
          }
        }
      }
      if (line.contains("<punc")) {
        String punc = parsePunc(line);
        this.regions.get(this.regions.size() - 1).setAttribute("following_punc", punc);
      }
      if (line.contains("<accent")) {
        this.regions.add(parseAccent(line));
      }
      if (line.contains("<break")) {
        this.regions.addAll(parseBreak(line));
      }
    }
  }

  protected List<Region> parseBreak(String line) {
    Pattern break_p = Pattern.compile("<break.*?>");
    Matcher break_m = break_p.matcher(line);

    if (break_m.find()) {
      List<Region> regions = new ArrayList<Region>();

      Pattern time_p = Pattern.compile("UWtime=\"(.*?)\"");
      Pattern breakid_p = Pattern.compile("index=\"(.*?)\"");
      Pattern phraseacc_p = Pattern.compile("phraseTone=\"(.*?)\"");
      Pattern btone_p = Pattern.compile("boundaryTone=\"(.*?)\"");

      Matcher time_m = time_p.matcher(break_m.group());
      Matcher breakid_m = breakid_p.matcher(break_m.group());
      Matcher phraseacc_m = phraseacc_p.matcher(break_m.group());
      Matcher btone_m = btone_p.matcher(break_m.group());

      if (time_m.find()) {
        double time = Double.parseDouble(time_m.group(1));
        breakid_m.find();
        regions.add(new Region(time, time, breakid_m.group(1)));

        if (phraseacc_m.find()) {
          regions.add(new Region(time, time, phraseacc_m.group(1) + "-"));
        }
        if (btone_m.find()) {
          regions.add(new Region(time, time, btone_m.group(1) + "%"));
        }
      }
      return regions;
    }
    return null;
  }

  protected Region parseAccent(String line) {
    Pattern accent_line_p = Pattern.compile("<accent.*?>");
    Matcher accent_line_m = accent_line_p.matcher(line);

    if (accent_line_m.find()) {

      Pattern start_p = Pattern.compile("nite:start=\"(.*?)\"");
      Pattern end_p = Pattern.compile("nite:end=\"(.*?)\"");

      Matcher start_m = start_p.matcher(accent_line_m.group());
      Matcher end_m = end_p.matcher(accent_line_m.group());

      if (start_m.find() && end_m.find()) {
        double start_time = Double.parseDouble(start_m.group(1));
        double end_time = Double.parseDouble(end_m.group(1));
        return new Region(start_time, end_time, "X*?");
      }
    }
    return null;
  }

  protected Region parseWord(String line) {
    Pattern wordline_p = Pattern.compile("<word.*?>");
    Matcher wordline_m = wordline_p.matcher(line);

    if (wordline_m.find()) {

      Pattern start_p = Pattern.compile("nite:start=\"(.*?)\"");
      Pattern end_p = Pattern.compile("nite:end=\"(.*?)\"");
      Pattern word_p = Pattern.compile("orth=\"(.*?)\"");

      Matcher start_m = start_p.matcher(wordline_m.group());
      Matcher end_m = end_p.matcher(wordline_m.group());
      Matcher word_m = word_p.matcher(wordline_m.group());

      // Start and end times can be "n/a" when a word is not complete, as in "don't" being partitioned into two words
      // "do" and "n't"
      // Start and end times are "non-aligned" when a word was unable to be aligned to time.
      if (start_m.find() && end_m.find() && word_m.find() && !start_m.group(1).equals("non-aligned")) {

        double start_time = start_m.group(1).equals("n/a") ? Double.NaN : Double.parseDouble(start_m.group(1));
        double end_time = end_m.group(1).equals("n/a") ? Double.NaN : Double.parseDouble(end_m.group(1));
        return new Word(start_time, end_time, word_m.group(1));
      }
    }
    return null;
  }

  protected String parsePunc(String line) {
    Pattern puncline_p = Pattern.compile("<punc.*?>.*?</punc>");
    Matcher puncline_m = puncline_p.matcher(line);

    if (puncline_m.find()) {

      Pattern punc_p = Pattern.compile(">(.*?)<");
      Matcher punc_m = punc_p.matcher(puncline_m.group());

      if (punc_m.find()) {
        return punc_m.group(1);
      } else {
        return null;
      }
    }
    return null;
  }
}
