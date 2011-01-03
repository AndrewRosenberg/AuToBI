/*  TextGridTier.java

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
   * @throws IOException if there is a problem
   */
  public boolean readTier(AuToBIFileReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = AuToBIReaderUtils.removeTabsAndTrim(line);

      if (line.matches("item\\s\\[[\\d]+\\]:")) {
        return true;
      }
      if (line.contains("class = \"IntervalTier\"")) {
        is_point_tier = false;
      }
      if (line.contains("class = \"TextTier\"")) {
        is_point_tier = true;
      } else if (line.contains("name = ")) {
        name = line.replaceFirst("name = \"(.*?)\"", "$1");
      } else if (line.matches("points\\s\\[[\\d]+\\]:")) {
        if (!is_point_tier) {
          throw new TextGridSyntaxErrorException("Found point label in interval tier.");
        }
        addPoint(reader);
      } else if (line.matches("intervals\\s\\[[\\d]+\\]:")) {
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
   * @throws IOException If there is a problem reading the file
   */
  private void addPoint(AuToBIFileReader reader)
      throws IOException {
    String time = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (time == null || !time.contains("time ="))
      throw new TextGridSyntaxErrorException("missing point at line: " + reader.getLineNumber());

    time = time.replace("time = ", "").trim();
    Region region = new Region(Double.valueOf(time));
    region.setFile(reader.getFilename());

    String mark = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (mark == null || !mark.contains("mark ="))
      throw new TextGridSyntaxErrorException("missing mark at line: " + reader.readLine());

    mark = mark.replaceAll("mark = \"(.*?)\"", "$1").trim();
    region.setLabel(mark);
    regions.add(region);
  }

  /**
   * Reads an interval region from the AuToBIFileReader.
   *
   * @param reader the active AuToBIFileReader
   * @throws IOException If there is a problem reading the file
   */
  private void addInterval(AuToBIFileReader reader)
      throws IOException {
    String xmin = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (xmin == null || !xmin.contains("xmin ="))
      throw new TextGridSyntaxErrorException("missing xmin at line: " + reader.getLineNumber());

    xmin = xmin.replace("xmin = ", "").trim();

    String xmax = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (xmax == null || !xmax.contains("xmax ="))
      throw new TextGridSyntaxErrorException("missing xmax at line: " + reader.getLineNumber());

    xmax = xmax.replace("xmax = ", "").trim();

    Region region = new Region(Double.valueOf(xmin), Double.valueOf(xmax));
    region.setFile(reader.getFilename());

    String text = AuToBIReaderUtils.removeTabsAndTrim(reader.readLine());
    if (text == null || !text.contains("text ="))
      throw new TextGridSyntaxErrorException("missing mark at line: " + reader.getLineNumber());

    text = text.replaceAll("text = \"(.*?)\"", "$1").trim();
    region.setLabel(text);

    regions.add(region);
  }
}
