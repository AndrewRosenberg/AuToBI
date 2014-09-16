/*  TextGridTier.java

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
package edu.cuny.qc.speech.AuToBI.io;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.AuToBIReaderUtils;

import java.io.IOException;


/**
 * TextGridTier is a Tier object tailored to the format of TextGrid tiers.
 *
 * @see Tier
 */
public class TextGridTier extends Tier {

  /**
   * Reads the information from the reader into the Tier regions, and sets the name of the tier.
   * <p/>
   * Returns true if there are additional items in the reader.
   *
   * @param reader the reader to read from
   * @return true if there are more items.
   * @throws IOException                  if there is an input output problem
   * @throws TextGridSyntaxErrorException If there is a formatting problem
   */
  public boolean readTier(AuToBIFileReader reader) throws TextGridSyntaxErrorException, IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = AuToBIReaderUtils.removeTabsAndTrim(line);

      if (line.matches("item\\s\\[[\\d]+\\]:")) {
        return true;
      }
      if (line.matches("class = \"IntervalTier\"")) {
        is_point_tier = false;
      }
      if (line.matches("class ?= ?\"TextTier\"")) {
        is_point_tier = true;
      } else if (line.matches("name ?= ?.*")) {
        name = line.replaceFirst("name ?= ?\"(.*?)\"", "$1");
      } else if (line.matches("points\\s\\[[\\d]+\\]:")) {
        if (!is_point_tier) {
          throw new TextGridSyntaxErrorException("Found point label in interval tier.");
        }
        addPoint(reader);
      } else if (line.matches("intervals\\s+\\[[\\d]+\\]:") || line.matches("intervals:\\s+\\[[\\d]+\\]")) {
        if (is_point_tier) {
          throw new TextGridSyntaxErrorException("Found interval label in point tier.");
        }
        addInterval(reader);
      }
    }
    return false;
  }

  /**
   * Reads a point region from the AuToBIFileReader.
   *
   * @param reader the active AuToBIFileReader
   * @throws IOException                  If there is a problem reading the file
   * @throws TextGridSyntaxErrorException If there is a formatting problem
   */
  protected void addPoint(AuToBIFileReader reader)
      throws IOException, TextGridSyntaxErrorException {
    String time = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (time == null || !(time.matches("time ?=.*") || time.matches("number ?=.*"))) {
      throw new TextGridSyntaxErrorException("missing point at line: " + reader.getLineNumber());
    }

    time = time.replaceFirst("time ?= ?", "").trim();
    time = time.replaceFirst("number ?= ?", "").trim();
    Region region = new Region(Double.valueOf(time));
    region.setFile(reader.getFilename());

    String mark = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (mark == null || !mark.matches("mark ?=.*")) {
      throw new TextGridSyntaxErrorException("missing mark at line: " + reader.readLine());
    }

    mark = mark.replaceAll("mark ?= ?\"(.*?)\"", "$1").trim();
    region.setLabel(mark);
    regions.add(region);
  }

  /**
   * Reads an interval region from the AuToBIFileReader.
   *
   * @param reader the active AuToBIFileReader
   * @throws IOException                  If there is a problem reading the file
   * @throws TextGridSyntaxErrorException If there is a formatting problem
   */
  protected void addInterval(AuToBIFileReader reader)
      throws IOException, TextGridSyntaxErrorException {
    String xmin = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (xmin == null || !xmin.matches("xmin ?=.*")) {
      throw new TextGridSyntaxErrorException("missing xmin at line: " + reader.getLineNumber());
    }

    xmin = xmin.replaceFirst("xmin ?= ?", "").trim();

    String xmax = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (xmax == null || !xmax.matches("xmax ?=.*")) {
      throw new TextGridSyntaxErrorException("missing xmax at line: " + reader.getLineNumber());
    }

    xmax = xmax.replaceFirst("xmax ?= ?", "").trim();

    Region region = new Region(Double.valueOf(xmin), Double.valueOf(xmax));
    region.setFile(reader.getFilename());

    String text = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (text == null || !text.matches("text ?=.*")) {
      throw new TextGridSyntaxErrorException("missing mark at line: " + reader.getLineNumber());
    }

    text = text.replaceAll("text ?= ?\"(.*?)\"", "$1").trim();
    region.setLabel(text);

    regions.add(region);
  }
}
