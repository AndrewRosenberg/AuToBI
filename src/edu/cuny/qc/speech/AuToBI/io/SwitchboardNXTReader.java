/*  SwitchboardNXTReader.java

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.AlignmentUtils;
import edu.cuny.qc.speech.AuToBI.util.WordReaderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 2/18/12 Time: 9:28 AM To change this template use File | Settings | File
 * Templates.
 */
public class SwitchboardNXTReader extends AuToBIWordReader {
  private String filestem;  // the base filestem containing word offset data

  /**
   * Constructs a new SwitchboardNXTReader.
   * <p/>
   * The files required include STEM.terminals.xml, STEM.accents.xml, STEM.breaks.xml
   *
   * @param filestem The stem of a set of switchboard NXT annotations.
   */
  public SwitchboardNXTReader(String filestem) {
    this.filestem = filestem;
  }

  /**
   * Reads words and associated ToBI annotations from switchboard xml annotations.
   *
   * @return a list of words with associated annotations
   * @throws IOException     if there is a problem reading any of the files.
   * @throws AuToBIException if there is a problem aligning tones.
   */
  @Override
  public List<Word> readWords() throws IOException, AuToBIException {

    NXTTier words_tier = new NXTTier();
    NXTTier accent_tier = new NXTTier();
    NXTTier breaks_tier = new NXTTier();

    words_tier.readTier(new AuToBIFileReader(filestem + ".terminals.xml"));
    accent_tier.readTier(new AuToBIFileReader(filestem + ".accents.xml"));
    breaks_tier.readTier(new AuToBIFileReader(filestem + ".breaks.xml"));

    List<Word> words = generateWordList(words_tier.getRegions());
    AlignmentUtils.copyToBITonesByTime(words, accent_tier.getRegions());
    AlignmentUtils.copyToBITonesByTime(words, breaks_tier.getRegions());

    List<Region> break_indices = new ArrayList<Region>();
    for (Region r : breaks_tier.getRegions()) {
      if (r.getLabel().startsWith("0") || r.getLabel().startsWith("1") || r.getLabel().startsWith("2") ||
          r.getLabel().startsWith("3") || r.getLabel().startsWith("4")) {
        break_indices.add(r);
      }
    }
    AlignmentUtils.copyToBIBreaksByTime(words, break_indices);

    return words;
  }

  /**
   * Converts the list of regions held in a Tier to a list of words.
   * <p/>
   * Omits silent regions when creating the list of words.
   * <p/>
   * Note: words can hold ToBI annotations, while regions are more general objects.
   *
   * @param regions the regions to convert
   * @return a list of words
   */
  protected List<Word> generateWordList(List<Region> regions) {
    List<Word> words = new ArrayList<Word>();
    for (Region r : regions) {
      if (!WordReaderUtils.isSilentRegion(r.getLabel(), silence_regex)) {
        Word w = new Word(r.getStart(), r.getEnd(), r.getLabel(), null, r.getFile());
        words.add(w);
        if (r.hasAttribute("following_punc")) {
          w.setAttribute("following_punc", r.getAttribute("following_punc"));
        }
      }
    }
    return words;
  }
}
