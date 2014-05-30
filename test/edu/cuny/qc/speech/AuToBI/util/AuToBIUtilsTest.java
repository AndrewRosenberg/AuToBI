/*  ContourUtilsTest.java

    Copyright 2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.AuToBI;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.AuToBITask;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.util.AuToBIUtilsTest
 */
public class AuToBIUtilsTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");
  private AuToBI autobi;

  @Before
  public void setup() {
    autobi = new AuToBI();
    initializeAuToBITasksWithoutClassifiers(autobi);
  }

  public void initializeAuToBITasksWithoutClassifiers(AuToBI autobi) {
    AuToBITask task = new AuToBITask();
    task.setTrueFeature("nominal_PitchAccent");
    task.setHypFeature("hyp_pitch_accent_location");
    task.setConfFeature("hyp_pitch_accent_location_conf");
    task.setDistFeature("hyp_pitch_accent_location_dist");
    task.setFeatureSet(new PitchAccentDetectionFeatureSet());
    autobi.getTasks().put("pitch_accent_detection", task);

    task = new AuToBITask();
    task.setTrueFeature("nominal_PitchAccentType");
    task.setHypFeature("hyp_pitch_accent_type");
    task.setConfFeature("hyp_pitch_accent_type_conf");
    task.setDistFeature("hyp_pitch_accent_type_dist");
    task.setFeatureSet(new PitchAccentClassificationFeatureSet());
    autobi.getTasks().put("pitch_accent_classification", task);

    task = new AuToBITask();
    task.setTrueFeature("nominal_IntonationalPhraseBoundary");
    task.setHypFeature("hyp_IP_location");
    task.setConfFeature("hyp_IP_location_conf");
    task.setDistFeature("hyp_IP_location_dist");
    task.setFeatureSet(new IntonationalPhraseBoundaryDetectionFeatureSet());
    autobi.getTasks().put("intonational_phrase_boundary_detection", task);

    task = new AuToBITask();
    task.setTrueFeature("nominal_IntermediatePhraseBoundary");
    task.setHypFeature("hyp_ip_location");
    task.setConfFeature("hyp_ip_location_conf");
    task.setDistFeature("hyp_ip_location_dist");
    task.setFeatureSet(new IntermediatePhraseBoundaryDetectionFeatureSet());
    autobi.getTasks().put("intermediate_phrase_boundary_detection", task);

    task = new AuToBITask();
    task.setTrueFeature("nominal_PhraseAccent");
    task.setHypFeature("hyp_phrase_accent");
    task.setConfFeature("hyp_phrase_accent_conf");
    task.setDistFeature("hyp_phrase_accent_dist");
    task.setFeatureSet(new PhraseAccentClassificationFeatureSet());
    autobi.getTasks().put("phrase_accent_classification", task);

    task = new AuToBITask();
    task.setTrueFeature("nominal_PhraseAccentBoundaryTone");
    task.setHypFeature("hyp_pabt");
    task.setConfFeature("hyp_pabt_conf");
    task.setDistFeature("hyp_pabt_dist");
    task.setFeatureSet(new PhraseAccentBoundaryToneClassificationFeatureSet());
    autobi.getTasks().put("phrase_accent_boundary_tone_classification", task);
  }

  @Test
  public void testLog() {
    AuToBIUtils.log("test");
  }

  @Test
  public void testError() {
    AuToBIUtils.error("test error");
  }

  @Test
  public void testInfo() {
    AuToBIUtils.info("test info");
  }

  @Test
  public void testGlobWithNullPattern() {
    List<String> files = AuToBIUtils.glob(null);

    assertEquals(0, files.size());
  }

  @Test
  public void testGlobWithHomeDirectory() {
    List<String> files = AuToBIUtils.glob("~/*");

    assertTrue(files.size() > 0);
  }

  @Test
  public void testGlobWithBaseDirectory() {
    List<String> files = AuToBIUtils.glob("/*");

    assertTrue(files.size() > 0);
  }

  @Test
  public void testGlobFromCurrentDirectory() {
    List<String> files = AuToBIUtils.glob("*");

    assertTrue(files.size() > 0);
  }

  @Test
  public void testGlobWithCurrentDirectory() {
    List<String> files = AuToBIUtils.glob("./*");

    assertTrue(files.size() > 0);
  }

  @Test
  public void testGlobWithHigherDirectory() {
    List<String> files = AuToBIUtils.glob("../*");

    assertTrue(files.size() > 0);
  }

  @Test
  public void testGlobSingleFileHasMultiple() {
    try {
      AuToBIUtils.globSingleFile(TEST_DIR + "/*");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGlobSingleFileHasMultipleWithComma() {
    try {
      AuToBIUtils.globSingleFile(TEST_DIR + "/*,test.file");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGlobSingleFile() {
    try {
      String file = AuToBIUtils.globSingleFile(TEST_DIR + "/sineWithNoise.wav");
      assertEquals(TEST_DIR + "/sineWithNoise.wav", file);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGlobSingleFileMatchesNone() {
    try {
      AuToBIUtils.globSingleFile(TEST_DIR + "/FALSE_FILENAME.wav");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testJoin() {
    List<String> s = new ArrayList<String>();
    s.add("one");
    s.add("two");

    String joined = AuToBIUtils.join(s, ",");
    assertEquals("one,two", joined);
  }

  @Test
  public void testEmptyJoin() {
    List<String> s = new ArrayList<String>();

    String joined = AuToBIUtils.join(s, ",");
    assertEquals("", joined);
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationACCENTED() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "ACCENTED");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("ACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTED() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("DEACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTEDWithType() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");
    w.setAttribute("hyp_pitch_accent_type", "L+H*");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("DEACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationACCENTEDWithType() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "ACCENTED");
    w.setAttribute("hyp_pitch_accent_type", "L+H*");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("L+H*", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationACCENTEDWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "ACCENTED");
    w.setAttribute("hyp_pitch_accent_location_conf", 0.7);

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("ACCENTED: 0.7", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTEDWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");
    w.setAttribute("hyp_pitch_accent_location_conf", 0.7);

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    // Bit of a floating point error here.
    assertEquals("ACCENTED: 0.30000000000000004", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesIPLocation() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    // Bit of a floating point error here.
    assertEquals("INTONATIONAL_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIPLocationWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");
    w.setAttribute("hyp_IP_location_conf", 0.7);

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("BOUNDARY: 0.7", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIPLocationFALSEWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_IP_location_conf", 0.7);

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    // Bit of a floating point error here.
    assertEquals("BOUNDARY: 0.30000000000000004", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationNoIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("INTERMEDIATE_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationWithFALSEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("INTERMEDIATE_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationWithTRUEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");
    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("INTONATIONAL_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryToneWithTRUEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");
    w.setAttribute("hyp_pabt", "L-H%");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("L-H%", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryToneWithFALSEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_boundary_tone", "H%");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("NONBOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryTone() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pabt", "L-H%");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("L-H%", w.getAttribute("hyp_phrase_boundary"));
  }


  @Test
  public void testMergeAuToBIHypothesesPhraseAccentWithTRUEInterP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");
    w.setAttribute("hyp_phrase_accent", "L-");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("L-", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesPhraseAccentWithFALSEInterP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "NONBOUNDARY");
    w.setAttribute("hyp_phrase_accent", "H-");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("NONBOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesPhraseAccent() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_phrase_accent", "L-");

    try {
      AuToBIUtils.mergeAuToBIHypotheses(autobi, words);
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }

    assertEquals("L-", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testGetPitchAccentDetectionTask() {
    AuToBITask task = AuToBIUtils.getPitchAccentDetectionTask(null);
    assertEquals("nominal_PitchAccent", task.getTrueFeature());
    assertEquals("hyp_pitch_accent_location", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PitchAccentDetectionFeatureSet);
  }

  @Test
  public void testGetPitchAccentClassificationTask() {
    AuToBITask task = AuToBIUtils.getPitchAccentClassificationTask(null);
    assertEquals("nominal_PitchAccentType", task.getTrueFeature());
    assertEquals("hyp_pitch_accent_type", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PitchAccentClassificationFeatureSet);
  }

  @Test
  public void testGetIntonationalPhraseDetectionTask() {
    AuToBITask task = AuToBIUtils.getIntonationalPhraseDetectionTask(null);
    assertEquals("nominal_IntonationalPhraseBoundary", task.getTrueFeature());
    assertEquals("hyp_intonational_phrase_boundary", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof IntonationalPhraseBoundaryDetectionFeatureSet);
  }

  @Test
  public void testGetIntermediatePhraseDetectionTask() {
    AuToBITask task = AuToBIUtils.getIntermediatePhraseDetectionTask(null);
    assertEquals("nominal_IntermediatePhraseBoundary", task.getTrueFeature());
    assertEquals("hyp_intermediate_phrase_boundary", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof IntermediatePhraseBoundaryDetectionFeatureSet);
  }

  @Test
  public void testGetPhraseAccentClassificationTask() {
    AuToBITask task = AuToBIUtils.getPhraseAccentClassificationTask(null);
    assertEquals("nominal_PhraseAccent", task.getTrueFeature());
    assertEquals("hyp_phrase_accent", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PhraseAccentClassificationFeatureSet);
  }

  @Test
  public void testGetPABTClassificationTask() {
    AuToBITask task = AuToBIUtils.getPABTClassificationTask(null);
    assertEquals("nominal_PhraseAccentBoundaryTone", task.getTrueFeature());
    assertEquals("hyp_phrase_accent_boundary_tone", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PhraseAccentBoundaryToneClassificationFeatureSet);
  }

  @Test
  public void testParseFeatureNameWorksOnAtomicFeatures() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("f0");
    } catch (AuToBIException e) {
      fail();
    }
    assertEquals(1, params.size());
    assertEquals("f0", params.get(0));
  }

  @Test
  public void testParseFeatureNameWorksOnOneParameterFeatures() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("test[f0]");
    } catch (AuToBIException e) {
      fail();
    }
    assertEquals(2, params.size());
    assertEquals("test", params.get(0));
    assertEquals("f0", params.get(1));
  }

  @Test
  public void testParseFeatureNameWorksOnNestedParameterFeatures() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("test[f0[booo]]");
    } catch (AuToBIException e) {
      fail();
    }
    assertEquals(2, params.size());
    assertEquals("test", params.get(0));
    assertEquals("f0[booo]", params.get(1));
  }

  @Test
  public void testParseFeatureNameWorksOnMultipleParameterFeatures() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("test[f0,I,spectrum]");
    } catch (AuToBIException e) {
      fail();
    }
    assertEquals(4, params.size());
    assertEquals("test", params.get(0));
    assertEquals("f0", params.get(1));
    assertEquals("I", params.get(2));
    assertEquals("spectrum", params.get(3));
  }

  @Test
  public void testParseFeatureNameWorksOnMultipleNestedParameterFeatures() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("test[f0[I,spectrum],I[f0,spectrum]]");
    } catch (AuToBIException e) {
      fail(e.getMessage());
    }
    assertEquals(3, params.size());
    assertEquals("test", params.get(0));
    assertEquals("f0[I,spectrum]", params.get(1));
    assertEquals("I[f0,spectrum]", params.get(2));
  }


  @Test
  public void testParseFeatureNameFailsOnMisMatchedBrackets() {
    List<String> params = null;
    try {
      params = AuToBIUtils.parseFeatureName("f0[test");
      fail();
    } catch (AuToBIException e) {
      assertTrue(true);
    }
  }
}
