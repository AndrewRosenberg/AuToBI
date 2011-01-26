/*  QCMClassifier.java

    Copyright (c) 2009-2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.ContourQuantizerException;
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.QuantizedContourModel;
import edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling.QuantizedContourModelTrainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * QCMClassifier is a bayesian classifier which models the likelihood of each class using a QuantizedContourModel and a
 * multinomial prior.
 */
public class QCMClassifier extends AuToBIClassifier {
  private String class_attribute; // the class attribute to predict
  private String contour_attribute; // the attribute to use for classification
  private int time_bins; // time bins in the QCM
  private int value_bins;  // value bins in the QCM
  private HashMap<String, QuantizedContourModel> models; // a set of QCM for classification
  private Distribution prior; // Prior distribution of class labels

  public QCMClassifier(String class_attribute, String contour_attribute, int time_bins, int value_bins) {
    this.class_attribute = class_attribute;
    this.contour_attribute = contour_attribute;
    this.time_bins = time_bins;
    this.value_bins = value_bins;
    this.models = new HashMap<String, QuantizedContourModel>();
    this.prior = new Distribution();
  }

  /**
   * Constructs a distribution of likelihoods over the classes.
   *
   * @param testing_point The point to evaluate
   * @return A distribution of likelihoods
   * @throws Exception
   */
  @Override
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    Distribution results = new Distribution();

    Contour c = (Contour) testing_point.getAttribute(contour_attribute);

    for (String key : models.keySet()) {
      double likelihood = prior.get(key);
      try {
        if (c != null) {
          likelihood += Math.exp(models.get(key).evaluateContour(c));
        }
      } catch (ContourQuantizerException e) {
        e.printStackTrace();
      }
      results.put(key, likelihood);
    }
    results.normalize();

    return results;
  }

  /**
   * Trains a new QCM Classifier.
   *
   * @param feature_set The training data
   * @throws Exception if there is a problem with the training
   */
  @Override
  public void train(FeatureSet feature_set) throws Exception {
    models.clear();
    prior = new Distribution();
    HashMap<String, List<Contour>> data = new HashMap<String, List<Contour>>();

    for (Word w : feature_set.getDataPoints()) {
      prior.add((String) w.getAttribute(class_attribute));

      if (!data.containsKey(w.getAttribute(class_attribute))) {
        data.put((String) w.getAttribute(class_attribute), new ArrayList<Contour>());
      }
      if (w.hasAttribute(contour_attribute)) {
        data.get(w.getAttribute(class_attribute)).add((Contour) w.getAttribute(contour_attribute));
      }
    }

    prior.normalize();

    for (String key : data.keySet()) {
      QuantizedContourModelTrainer trainer = new QuantizedContourModelTrainer(time_bins, value_bins, 0.025);
      QuantizedContourModel qcm = trainer.train(data.get(key));
      models.put(key, qcm);
    }
  }

  /**
   * Constructs a new untrained copy of the classifier with the same parameters.
   *
   * @return
   */
  @Override
  public AuToBIClassifier newInstance() {
    return new QCMClassifier(class_attribute, contour_attribute, time_bins, value_bins);
  }
}
