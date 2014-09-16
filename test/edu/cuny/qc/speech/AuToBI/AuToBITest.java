/*  AuToBITest.java

    Copyright (c) 2011-2012 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.MockClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import edu.cuny.qc.speech.AuToBI.io.FormattedFile;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for AuToBI.
 */
@SuppressWarnings("unchecked")
public class AuToBITest {

  public static class MockF0NoParamFeatureExtractor extends FeatureExtractor {
    public final static String moniker = "f0";

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      // does nothing
    }

    public MockF0NoParamFeatureExtractor() {
      // empty constructor
      this.extracted_features.add("f0");
    }
  }

  public static class MockLogVariableParamFeatureExtractor extends FeatureExtractor {
    public final static String moniker = "log";

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      // does nothing
    }

    public MockLogVariableParamFeatureExtractor(String one, String two) {
      // empty constructor
      this.extracted_features.add("log[" + one + "," + two + "]");
    }

    public MockLogVariableParamFeatureExtractor(String one) {
      // empty constructor
      this.extracted_features.add("log[" + one + "]");
    }
  }

  public static class MockTestFeatureExtractor extends FeatureExtractor {
    public final static String moniker = "test";

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      // does nothing
    }

    public MockTestFeatureExtractor(String one, String two) {
      // empty constructor
      this.extracted_features.add("test[" + one + "," + two + "]");
    }
  }

  private AuToBI autobi;
  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Before
  public void setUp() throws Exception {
    autobi = new AuToBI();
  }

  public HashMap<String, AuToBITask> initializeMockTasks() {
    AuToBITask pad_task = new AuToBITask();
    pad_task.setClassifier(new MockClassifier());

    pad_task.setConfFeature("pad_conf");
    pad_task.setDistFeature("pad_dist");
    pad_task.setFeatureSet(new PitchAccentDetectionFeatureSet());
    pad_task.setHypFeature("pad_hyp");
    pad_task.setTrueFeature("pad_true");

    AuToBITask pac_task = new AuToBITask();
    pac_task.setClassifier(new MockClassifier());

    pac_task.setConfFeature("pac_conf");
    pac_task.setDistFeature("pac_dist");
    pac_task.setFeatureSet(new PitchAccentClassificationFeatureSet());
    pac_task.setHypFeature("pac_hyp");
    pac_task.setTrueFeature("pac_true");

    AuToBITask IPd_task = new AuToBITask();
    IPd_task.setClassifier(new MockClassifier());

    IPd_task.setConfFeature("IPd_conf");
    IPd_task.setDistFeature("IPd_dist");
    IPd_task.setFeatureSet(new IntonationalPhraseBoundaryDetectionFeatureSet());
    IPd_task.setHypFeature("IPd_hyp");
    IPd_task.setTrueFeature("IPd_true");

    AuToBITask IPc_task = new AuToBITask();
    IPc_task.setClassifier(new MockClassifier());

    IPc_task.setConfFeature("IPc_conf");
    IPc_task.setDistFeature("IPc_dist");
    IPc_task.setFeatureSet(new PhraseAccentBoundaryToneClassificationFeatureSet());
    IPc_task.setHypFeature("IPc_hyp");
    IPc_task.setTrueFeature("IPc_true");

    AuToBITask ipd_task = new AuToBITask();
    ipd_task.setClassifier(new MockClassifier());

    ipd_task.setConfFeature("ipd_conf");
    ipd_task.setDistFeature("ipd_dist");
    ipd_task.setFeatureSet(new IntermediatePhraseBoundaryDetectionFeatureSet());
    ipd_task.setHypFeature("ipd_hyp");
    ipd_task.setTrueFeature("ipd_true");

    AuToBITask ipc_task = new AuToBITask();
    ipc_task.setClassifier(new MockClassifier());

    ipc_task.setConfFeature("ipc_conf");
    ipc_task.setDistFeature("ipc_dist");
    ipc_task.setFeatureSet(new PhraseAccentClassificationFeatureSet());
    ipc_task.setHypFeature("ipc_hyp");
    ipc_task.setTrueFeature("ipc_true");

    HashMap<String, AuToBITask> tasks = new HashMap<String, AuToBITask>();
    tasks.put("pitch_accent_detection", pad_task);
    tasks.put("pitch_accent_classification", pac_task);
    tasks.put("intonational_phrase_boundary_detection", IPd_task);
    tasks.put("boundary_tone_classification", IPc_task);
    tasks.put("intermediate_phrase_boundary_detection", ipd_task);
    tasks.put("phrase_accent_classification", ipc_task);
    return tasks;
  }

  @Test
  public void testMonikerMapInitializesCorrectly() {
    autobi.registerFeatureExtractorMonikers("edu.cuny.qc.speech.AuToBI.AuToBITest");

    assertEquals(5, autobi.getMonikerMap().size());
  }

  @Test
  public void testMonikerMapRegistersAtomicFeatureExtractorsCorrectly() {
    String feature = "f0";
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute(feature);
    fs.constructFeatures();

    autobi.getMonikerMap().put("f0", MockF0NoParamFeatureExtractor.class);

    try {
      autobi.initializeFeatureRegistry(fs);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertTrue(autobi.getFeatureRegistry().containsKey("f0"));
  }

  @Test
  public void testMonikerMapRegistersNestedFeatureExtractorsCorrectly() {
    String feature = "log[f0]";
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute(feature);
    fs.constructFeatures();

    autobi.getMonikerMap().put("f0", MockF0NoParamFeatureExtractor.class);
    autobi.getMonikerMap().put("log", MockLogVariableParamFeatureExtractor.class);

    try {
      autobi.initializeFeatureRegistry(fs);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertTrue(autobi.getFeatureRegistry().containsKey("log[f0]"));
    assertTrue(autobi.getFeatureRegistry().containsKey("f0"));
  }

  @Test
  public void testMonikerMapRegistersMultiplyNestedFeatureExtractorsCorrectly() {
    String feature = "log[f0,f0]";
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute(feature);
    fs.constructFeatures();

    autobi.getMonikerMap().put("f0", MockF0NoParamFeatureExtractor.class);
    autobi.getMonikerMap().put("log", MockLogVariableParamFeatureExtractor.class);

    try {
      autobi.initializeFeatureRegistry(fs);
    } catch (Exception e) {
      fail();
    }

    assertTrue(autobi.getFeatureRegistry().containsKey("log[f0,f0]"));
    assertTrue(autobi.getFeatureRegistry().containsKey("f0"));
  }

  @Test
  public void testMonikerMapRegistersParameterizedFeatureExtractorsCorrectly() {
    String feature = "test[1,2]";
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute(feature);
    fs.constructFeatures();

    autobi.getMonikerMap().put("test", MockTestFeatureExtractor.class);

    try {
      autobi.initializeFeatureRegistry(fs);
    } catch (Exception e) {
      fail();
    }

    assertTrue(autobi.getFeatureRegistry().containsKey("test[1,2]"));
    assertFalse(autobi.getFeatureRegistry().containsKey("1"));
    assertFalse(autobi.getFeatureRegistry().containsKey("2"));
  }


  @Test
  public void testInitializeReferenceCounting() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getRequiredFeatures().add("feature2");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe2.getExtractedFeatures().add("feature2");

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testInitializeReferenceCountingTwoFeaturesExtractedByOneExtractor() {

    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.insertRequiredFeature("feature2");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getExtractedFeatures().add("feature2");
    fe.getRequiredFeatures().add("feature3");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe2.getExtractedFeatures().add("feature3");

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
      assertEquals(2, autobi.getReferenceCount("feature3"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testInitializeReferenceCountingWorksWithNullFeatureExtractors() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getRequiredFeatures().add("feature2");

    autobi.registerFeatureExtractor(fe);
    autobi.registerNullFeatureExtractor("feature2");

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testFeatureGarbageCollectionDeletesZeroCountFeatures() {
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0.0, 0.1, "test_point");
    fs.insertDataPoint(w);

    try {
      autobi.initializeReferenceCounting(fs);
    } catch (AuToBIException e) {
      fail();
    }
    autobi.incrementReferenceCount("test_feature");
    w.setAttribute("test_feature", true);
    autobi.decrementReferenceCount("test_feature");

    autobi.featureGarbageCollection(fs);
    assertTrue(!w.hasAttribute("test_feature"));
  }

  public static class MockRequiresF3FeatureExtractor extends FeatureExtractor {
    public static final String moniker = "mock";

    public MockRequiresF3FeatureExtractor() {
      this.getExtractedFeatures().add("feature1");
      this.getExtractedFeatures().add("feature2");
      this.getRequiredFeatures().add("feature3");
    }

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      for (Region r : (List<Region>) regions) {
        r.setAttribute("feature1", true);
        r.setAttribute("feature2", true);
      }
    }
  }

  public static class MockProvidesF12FeatureExtractor extends FeatureExtractor {
    public static final String moniker = "mock";

    public MockProvidesF12FeatureExtractor() {
      this.getExtractedFeatures().add("feature1");
      this.getExtractedFeatures().add("feature2");
    }

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      for (Region r : (List<Region>) regions) {
        r.setAttribute("feature1", true);
        r.setAttribute("feature2", true);
      }
    }
  }

  public static class MockProvidesF3FeatureExtractor extends FeatureExtractor {
    public static final String moniker = "mockf3";

    public MockProvidesF3FeatureExtractor() {
      this.getExtractedFeatures().add("feature3");
    }

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      for (Region r : (List<Region>) regions) {
        r.setAttribute("feature3", true);
      }
    }
  }

  @Test
  public void testFeatureExtractionRemovesUnusedFeatures() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0.0, 0.1, "test_point");
    fs.insertDataPoint(w);

    fs.setClassAttribute("feature1");
    fs.insertRequiredFeature("feature2");
    fs.constructFeatures();

    FeatureExtractor fe = new MockRequiresF3FeatureExtractor();

    FeatureExtractor fe2 = new MockProvidesF3FeatureExtractor();

    autobi.getMonikerMap().put("feature1", fe.getClass());
    autobi.getMonikerMap().put("feature2", fe.getClass());
    autobi.getMonikerMap().put("feature3", fe2.getClass());

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      autobi.extractFeatures(fs);
      assertEquals(2, w.getAttributeNames().size());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testUnregisterFeatureExtractorsClearsTheFeatureRegistry() {
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getRequiredFeatures().add("feature2");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe2.getExtractedFeatures().add("feature2");

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    assertEquals(2, autobi.getFeatureRegistry().size());
    autobi.unregisterAllFeatureExtractors();
    assertEquals(0, autobi.getFeatureRegistry().size());
    assertEquals(0, autobi.executed_feature_extractors.size());
  }

  @Test
  public void testCollectFeatureExtractorMonikerFindsAnything() {
    autobi.registerDefaultFeatureExtractorMonikers();
    assertTrue(autobi.getMonikerMap().size() > 0);
  }


  @Test
  public void testInitSetsTrueBooleanParameter() {
    autobi.init(new String[]{"-test_param=true"});
    assertTrue(autobi.getBooleanParameter("test_param", false));
  }

  @Test
  public void testInitSetsFalseBooleanParameter() {
    autobi.init(new String[]{"-test_param=false"});
    assertFalse(autobi.getBooleanParameter("test_param", true));
  }

  @Test
  public void testInitSetsStringParameter() {
    autobi.init(new String[]{"-test_param=hello"});
    try {
      assertEquals("hello", autobi.getParameter("test_param"));
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetOptionalParameterReturnsNull() {
    autobi.init(new String[]{"-test_param=hello"});
    assertEquals(null, autobi.getOptionalParameter("no_such_parameter"));
  }

  @Test
  public void testInitSetsParameters() {
    autobi.init(new String[]{"-test_param=hello"});
    assertTrue(autobi.hasParameter("test_param"));
  }

  @Test
  public void testEvaluateTaskPerformanceFailsOnInvalidTask() {
    try {
      autobi.evaluateTaskPerformance("NO_SUCH_TASK", new FeatureSet());
      fail();
    } catch (AuToBIException expected) {
      // expected
    }
  }

  @Test
  public void testEvaluateTaskPerformanceWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    try {
      assertTrue(autobi.evaluateTaskPerformance("pitch_accent_detection", new FeatureSet()).length() > 0);
      assertTrue(autobi.evaluateTaskPerformance("pitch_accent_classification", new FeatureSet()).length() > 0);
      assertTrue(
          autobi.evaluateTaskPerformance("intonational_phrase_boundary_detection", new FeatureSet()).length() > 0);
      assertTrue(
          autobi.evaluateTaskPerformance("intermediate_phrase_boundary_detection", new FeatureSet()).length() > 0);
      assertTrue(autobi.evaluateTaskPerformance("boundary_tone_classification", new FeatureSet()).length() > 0);
      assertTrue(autobi.evaluateTaskPerformance("phrase_accent_classification", new FeatureSet()).length() > 0);
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGeneratePredictionsFailsOnInvalidTask() {
    autobi.tasks = initializeMockTasks();
    try {
      autobi.generatePredictions("NO_SUCH_TASK", new FeatureSet());
      fail();
    } catch (AuToBIException expected) {
      // expected
    }
  }

  @Test
  public void testGeneratePredictionsWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      autobi.generatePredictions("pitch_accent_detection", fs);
      autobi.generatePredictions("pitch_accent_classification", fs);
      autobi.generatePredictions("intonational_phrase_boundary_detection", fs);
      autobi.generatePredictions("intermediate_phrase_boundary_detection", fs);
      autobi.generatePredictions("boundary_tone_classification", fs);
      autobi.generatePredictions("phrase_accent_classification", fs);
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGeneratePredictionsWithConfidenceScoresWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      autobi.generatePredictionsWithConfidenceScores("pitch_accent_detection", fs);
      autobi.generatePredictionsWithConfidenceScores("pitch_accent_classification", fs);
      autobi.generatePredictionsWithConfidenceScores("intonational_phrase_boundary_detection", fs);
      autobi.generatePredictionsWithConfidenceScores("intermediate_phrase_boundary_detection", fs);
      autobi.generatePredictionsWithConfidenceScores("boundary_tone_classification", fs);
      autobi.generatePredictionsWithConfidenceScores("phrase_accent_classification", fs);
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGetTaskFeatureSetWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      assertNotNull(autobi.getTaskFeatureSet("pitch_accent_detection"));
      assertNotNull(autobi.getTaskFeatureSet("pitch_accent_classification"));
      assertNotNull(autobi.getTaskFeatureSet("intonational_phrase_boundary_detection"));
      assertNotNull(autobi.getTaskFeatureSet("intermediate_phrase_boundary_detection"));
      assertNotNull(autobi.getTaskFeatureSet("boundary_tone_classification"));
      assertNotNull(autobi.getTaskFeatureSet("phrase_accent_classification"));
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGetHypothesizedFeatureWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      assertNotNull(autobi.getHypothesizedFeature("pitch_accent_detection"));
      assertNotNull(autobi.getHypothesizedFeature("pitch_accent_classification"));
      assertNotNull(autobi.getHypothesizedFeature("intonational_phrase_boundary_detection"));
      assertNotNull(autobi.getHypothesizedFeature("intermediate_phrase_boundary_detection"));
      assertNotNull(autobi.getHypothesizedFeature("boundary_tone_classification"));
      assertNotNull(autobi.getHypothesizedFeature("phrase_accent_classification"));
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGetDistributionFeatureWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      assertNotNull(autobi.getDistributionFeature("pitch_accent_detection"));
      assertNotNull(autobi.getDistributionFeature("pitch_accent_classification"));
      assertNotNull(autobi.getDistributionFeature("intonational_phrase_boundary_detection"));
      assertNotNull(autobi.getDistributionFeature("intermediate_phrase_boundary_detection"));
      assertNotNull(autobi.getDistributionFeature("boundary_tone_classification"));
      assertNotNull(autobi.getDistributionFeature("phrase_accent_classification"));
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testGetTaskClassifierWorksForValidTasks() {
    autobi.tasks = initializeMockTasks();
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "testing");
    fs.insertDataPoint(w);

    try {
      assertNotNull(autobi.getTaskClassifier("pitch_accent_detection"));
      assertNotNull(autobi.getTaskClassifier("pitch_accent_classification"));
      assertNotNull(autobi.getTaskClassifier("intonational_phrase_boundary_detection"));
      assertNotNull(autobi.getTaskClassifier("intermediate_phrase_boundary_detection"));
      assertNotNull(autobi.getTaskClassifier("boundary_tone_classification"));
      assertNotNull(autobi.getTaskClassifier("phrase_accent_classification"));
    } catch (AuToBIException expected) {
      fail();
    }
  }

  @Test
  public void testPropagateFeatureSetPropagatesFeatures() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("feature3");

    FeatureExtractor fe = new MockProvidesF3FeatureExtractor();

    autobi.getMonikerMap().put("feature3", fe.getClass());

    try {
      autobi.initializeFeatureRegistry(fs);

      List<FormattedFile> filenames = new ArrayList<FormattedFile>();
      filenames.add(new FormattedFile(TEST_DIR + "/test.txt", FormattedFile.Format.SIMPLE_WORD));
      autobi.propagateFeatureSet(filenames, fs);
      for (Word w : fs.getDataPoints()) {
        // Only the one required feature, not the wav feature
        assertEquals(1, w.getAttributeNames().size());
      }
    } catch (AuToBIException e) {
      fail(e.getMessage());
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (InvocationTargetException e) {
      fail(e.getMessage());
    } catch (InstantiationException e) {
      fail(e.getMessage());
    } catch (IllegalAccessException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testPropagateFeatureSetPreserveFeaturesPropagatesFeatures() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.insertRequiredFeature("feature2");

    FeatureExtractor fe = new MockProvidesF12FeatureExtractor();
    autobi.getMonikerMap().put("feature1", fe.getClass());
    autobi.getMonikerMap().put("feature2", fe.getClass());

    try {
      autobi.initializeFeatureRegistry(fs);

      List<FormattedFile> filenames = new ArrayList<FormattedFile>();
      filenames.add(new FormattedFile(TEST_DIR + "/test.txt", FormattedFile.Format.SIMPLE_WORD));
      autobi.getParameters().setParameter("feature_preservation", "true");
      autobi.propagateFeatureSet(filenames, fs);
      for (Word w : fs.getDataPoints()) {
        // Only the two required features, not the wav feature
        assertEquals(3, w.getAttributeNames().size());
      }
    } catch (AuToBIException e) {
      fail(e.getMessage());
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (InvocationTargetException e) {
      fail(e.getMessage());
    } catch (InstantiationException e) {
      fail(e.getMessage());
    } catch (IllegalAccessException e) {
      fail(e.getMessage());
    }
  }

  public static class MockLateStartFE extends FeatureExtractor {
    public MockLateStartFE() {
      this.getExtractedFeatures().add("feature1");
      this.getExtractedFeatures().add("late_start");
    }

    @Override
    public void extractFeatures(List regions) throws FeatureExtractorException {
      for (Region r : (List<Region>) regions) {
        r.setAttribute("feature1", true);
        if (r.getStart() > 0.5) {
          r.setAttribute("late_start", "YES");
        } else {
          r.setAttribute("late_start", "NO");
        }
      }
    }
  }

  @Test
  public void testPropagateFeatureSetOmitsPointsBasedOnAttributesCorrectly() {
    // Set up Test Feature Extraction Configuration
    /**
     * the test.txt file contains the following information corresponding to five data points
     *
     * TESTING 0.1     0.2
     * AUTOBI  0.3     0.4
     * ONE     0.5     0.6
     * TWO     0.7     0.8
     * THREE   0.8     0.9
     */
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.insertRequiredFeature("late_start");

    FeatureExtractor fe = new MockLateStartFE();

    autobi.getMonikerMap().put("feature1", fe.getClass());
    autobi.getMonikerMap().put("late_start", fe.getClass());

    try {
      autobi.initializeFeatureRegistry(fs);
      autobi.getParameters().setParameter("attribute_omit", "late_start:NO");

      List<FormattedFile> filenames = new ArrayList<FormattedFile>();
      filenames.add(new FormattedFile(TEST_DIR + "/test.txt", FormattedFile.Format.SIMPLE_WORD));
      autobi.propagateFeatureSet(filenames, fs);
      assertEquals(2, fs.getDataPoints().size());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    } catch (UnsupportedAudioFileException e) {
      fail(e.getMessage());
    } catch (InvocationTargetException e) {
      fail(e.getMessage());
    } catch (InstantiationException e) {
      fail(e.getMessage());
    } catch (IllegalAccessException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testInitializeAuToBITasksGeneratesNoTasksWithoutClassifiers() {
    HashMap<String, AuToBITask> tasks = initializeMockTasks();
    try {
      writeMockClassifiersToTestDir(tasks);
    } catch (IOException e) {
      fail(e.getMessage());
    }

    autobi.initializeAuToBITasks();
    assertEquals(0, autobi.tasks.size());
  }

  @Test
  public void testInitializeAuToBITasksGeneratesPADTask() {
    HashMap<String, AuToBITask> tasks = initializeMockTasks();
    try {
      writeMockClassifiersToTestDir(tasks);
    } catch (IOException e) {
      fail();
    }
    autobi.getParameters().setParameter("pitch_accent_detector", TEST_DIR + "/pitch_accent_detection.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("pitch_accent_detection"));
  }

  @Test
  public void testInitializeAuToBITasksGeneratesPACTask() {
    HashMap<String, AuToBITask> tasks = initializeMockTasks();
    try {
      writeMockClassifiersToTestDir(tasks);
    } catch (IOException e) {
      fail();
    }
    autobi.getParameters()
        .setParameter("pitch_accent_classifier", TEST_DIR + "/pitch_accent_classification.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("pitch_accent_classification"));
  }

  @Test
  public void testInitializeAuToBITasksGeneratesIPDTask() {
    HashMap<String, AuToBITask> tasks = initializeMockTasks();
    try {
      writeMockClassifiersToTestDir(tasks);
    } catch (IOException e) {
      fail();
    }
    autobi.getParameters().setParameter("intonational_phrase_boundary_detector",
        TEST_DIR + "/intonational_phrase_boundary_detection.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("intonational_phrase_boundary_detection"));
  }

  @Test
  public void testInitializeAuToBITasksGeneratesipDTask() {
    autobi.getParameters().setParameter("intermediate_phrase_boundary_detector",
        TEST_DIR + "/intermediate_phrase_boundary_detection.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("intermediate_phrase_boundary_detection"));
  }

  @Test
  public void testInitializeAuToBITasksGeneratesPhraseAccentClassificationTask() {
    autobi.getParameters()
        .setParameter("phrase_accent_classifier", TEST_DIR + "/phrase_accent_classification.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("phrase_accent_classification"));
  }

  @Test
  public void testInitializeAuToBITasksGeneratesPABTTask() {
    autobi.getParameters()
        .setParameter("phrase_accent_boundary_tone_classifier", TEST_DIR + "/boundary_tone_classification.classifier");
    autobi.initializeAuToBITasks();
    assertEquals(1, autobi.tasks.size());
    assertTrue(autobi.tasks.containsKey("phrase_accent_boundary_tone_classification"));
  }

  @Test
  public void testGenerateTextGridFileString() {
    // TODO: this will require additional testing to make sure all of the task attributes are filtering through
    // correctly
    autobi.tasks = initializeMockTasks();
    ArrayList<Word> words = new ArrayList<Word>();
    Word w = new Word(0, 1, "testing");
    w.setAttribute("pad_hyp", "ACCENTED");
    w.setAttribute("pac_hyp", "H*");
    w.setAttribute("ipc_hyp", "BOUNDARY");
    w.setAttribute("ipc_hyp", "H-");
    w.setAttribute("IPd_hyp", "BOUNDARY");
    w.setAttribute("IPc_hyp", "H%");
    words.add(w);
    // Approximate mergeAuToBIHypotheses
    w.setAttribute("hyp_pitch_accent", "ACCENTED");
    String text_grid = autobi.generateTextGridString(words);
    assertEquals("File type = \"ooTextFile\"\n" +
        "Object class = \"TextGrid\"\n" +
        "xmin = 0\n" +
        "xmax = 1.0\n" +
        "tiers? <exists>\n" +
        "size = 3\n" +
        "item []:\n" +
        "item [1]:\n" +
        "class = \"IntervalTier\"\n" +
        "name = \"words\"\n" +
        "xmin = 0\n" +
        "xmax = 1.0\n" +
        "intervals: size = 1\n" +
        "intervals [1]:\n" +
        "xmin = 0.0\n" +
        "xmax = 1.0\n" +
        "text = \"testing\"\n" +
        "item [2]:\n" +
        "class = \"IntervalTier\"\n" +
        "name = \"pitch_accent_hypothesis\"\n" +
        "xmin = 0\n" +
        "xmax = 1.0\n" +
        "intervals: size = 1\n" +
        "intervals [1]:\n" +
        "xmin = 0.0\n" +
        "xmax = 1.0\n" +
        "text = \"ACCENTED\"\n" +
        "item [3]:\n" +
        "class = \"IntervalTier\"\n" +
        "name = \"phrase_hypothesis\"\n" +
        "xmin = 0\n" +
        "xmax = 1.0\n" +
        "intervals: size = 1\n" +
        "intervals [1]:\n" +
        "xmin = 0.0\n" +
        "xmax = 1.0\n" +
        "text = \"\"\n", text_grid);
  }

  @Test
  public void testLoadSpeakerNormalizationParameterMapping() {
    /**
     * This test file contains a single line:
     * test_speaker,test.spkrnorm
     */
    try {
      autobi.loadSpeakerNormalizationMapping(TEST_DIR + "/speaker_normalization_mapping.txt");

      assertNotNull(autobi.getSpeakerNormParamFilename("test_speaker"));
      assertEquals("test.spkrnorm", autobi.getSpeakerNormParamFilename("test_speaker"));
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Helper functions *
   */

  /**
   * Write classifiers to test directory.
   * Used to test unserialization
   *
   * @param tasks hashmap of strings to AuToBITasks
   * @throws IOException
   */
  private void writeMockClassifiersToTestDir(HashMap<String, AuToBITask> tasks) throws IOException {
    for (String task : tasks.keySet()) {
      writeClassifierToFile(TEST_DIR + "/" + task + ".classifier", tasks.get(task).getClassifier());
    }
  }

  private void writeClassifierToFile(String filename, AuToBIClassifier classifier) throws IOException {
    FileOutputStream fos;
    ObjectOutputStream out;
    fos = new FileOutputStream(filename);
    out = new ObjectOutputStream(fos);
    out.writeObject(classifier);
    out.close();
  }
}
