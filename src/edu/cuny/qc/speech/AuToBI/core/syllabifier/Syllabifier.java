/**
 Syllabifier.java

 Copyright 2013 Andrew Rosenberg
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

package edu.cuny.qc.speech.AuToBI.core.syllabifier;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;

import java.util.List;

/**
 * A baseclass for acoustic based pseudosyllabification techniques.
 */
public abstract class Syllabifier {

  /**
   * Generates pseudosyllables based on a full wav file.
   *
   * @param wav the wav file.
   * @return a time ordered list of pseudosyllable regions.
   */
  public abstract List<Region> generatePseudosyllableRegions(WavData wav);
}
