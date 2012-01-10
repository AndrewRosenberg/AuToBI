/*  SpectrumPADFeatureSet.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureset;

import edu.cuny.qc.speech.AuToBI.core.ContextDesc;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;

import java.util.ArrayList;
import java.util.List;

/**
 * SpectrumPADFeatureSet describes the features that are required for generating pitch accent detection (PAD) hypotheses
 * based on energy information and spectral tilt from a specific frequency region.
 */
public class SpectrumPADFeatureSet extends FeatureSet {

  /**
   * Constructs a new SpectrumPADFeatureSet for a given spectral region specified in bark.
   *
   * @param low  the bottom of the spectral region
   * @param high the top of the spectral region
   */
  public SpectrumPADFeatureSet(int low, int high) {
    super();
    for (String acoustic : new String[]{"bark", "bark_tilt"}) {
      for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
        insertRequiredFeature(acoustic + "_" + low + "_" + high + "__" + agg);
      }
    }

    List<ContextDesc> contexts = new ArrayList<ContextDesc>();
    contexts.add(new ContextDesc("f2b2", 2, 2));
    contexts.add(new ContextDesc("f2b1", 2, 1));
    contexts.add(new ContextDesc("f2b0", 2, 0));
    contexts.add(new ContextDesc("f1b2", 1, 2));
    contexts.add(new ContextDesc("f0b2", 0, 2));
    contexts.add(new ContextDesc("f0b1", 0, 1));
    contexts.add(new ContextDesc("f1b0", 1, 0));
    contexts.add(new ContextDesc("f1b1", 1, 1));
    for (ContextDesc context : contexts) {
      for (String acoustic : new String[]{"bark", "bark_tilt"}) {
        for (String agg : new String[]{"zMax", "zMean"}) {
          insertRequiredFeature(acoustic + "_" + low + "_" + high + "_" + context.getLabel() + "__" + agg);
        }
      }
    }

    this.class_attribute = "nominal_PitchAccent";
  }
}
