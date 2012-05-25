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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.AuToBITask;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureset.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for edu.cuny.qc.speech.AuToBI.util.AuToBIUtilsTest
 */
public class AuToBIUtilsTest {

  private final String TEST_DIR = "/Users/andrew/code/AuToBI/release/test_data";

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
      fail();
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

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("ACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTED() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("DEACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTEDWithType() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");
    w.setAttribute("hyp_pitch_accent_type", "L+H*");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("DEACCENTED", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationACCENTEDWithType() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "ACCENTED");
    w.setAttribute("hyp_pitch_accent_type", "L+H*");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("L+H*", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationACCENTEDWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "ACCENTED");
    w.setAttribute("hyp_pitch_accent_location_conf", 0.7);

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("ACCENTED: 0.7", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesPitchAccentLocationDEACCENTEDWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_pitch_accent_location", "DEACCENTED");
    w.setAttribute("hyp_pitch_accent_location_conf", 0.7);

    AuToBIUtils.mergeAuToBIHypotheses(words);

    // Bit of a floating point error here.
    assertEquals("ACCENTED: 0.30000000000000004", w.getAttribute("hyp_pitch_accent"));
  }

  @Test
  public void testMergeAuToBIHypothesesIPLocation() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");

    AuToBIUtils.mergeAuToBIHypotheses(words);

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

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("BOUNDARY: 0.7", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIPLocationFALSEWithConf() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_IP_location_conf", 0.7);

    AuToBIUtils.mergeAuToBIHypotheses(words);

    // Bit of a floating point error here.
    assertEquals("BOUNDARY: 0.30000000000000004", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationNoIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("INTERMEDIATE_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationWithFALSEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("INTERMEDIATE_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesIntermediateLocationWithTRUEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");
    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("INTONATIONAL_BOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryToneWithTRUEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "INTONATIONAL_BOUNDARY");
    w.setAttribute("hyp_boundary_tone", "H%");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("H%", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryToneWithFALSEIP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_IP_location", "NONBOUNDARY");
    w.setAttribute("hyp_boundary_tone", "H%");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("NONBOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesBoundaryTone() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_boundary_tone", "H%");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("H%", w.getAttribute("hyp_phrase_boundary"));
  }


  @Test
  public void testMergeAuToBIHypothesesPhraseAccentWithTRUEInterP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "INTERMEDIATE_BOUNDARY");
    w.setAttribute("hyp_phrase_accent", "L-");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("L-", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesPhraseAccentWithFALSEInterP() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_ip_location", "NONBOUNDARY");
    w.setAttribute("hyp_phrase_accent", "H-");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("NONBOUNDARY", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testMergeAuToBIHypothesesPhraseAccent() {
    Word w = new Word(0.0, 0.1, "hello");
    List<Word> words = new ArrayList<Word>();
    words.add(w);

    w.setAttribute("hyp_phrase_accent", "L-");

    AuToBIUtils.mergeAuToBIHypotheses(words);

    assertEquals("L-", w.getAttribute("hyp_phrase_boundary"));
  }

  @Test
  public void testGetPitchAccentDetectionTask() {
    AuToBITask task = AuToBIUtils.getPitchAccentDetectionTask();
    assertEquals("nominal_PitchAccent", task.getTrueFeature());
    assertEquals("hyp_pitch_accent_location", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PitchAccentDetectionFeatureSet);
  }

  @Test
  public void testGetPitchAccentClassificationTask() {
    AuToBITask task = AuToBIUtils.getPitchAccentClassificationTask();
    assertEquals("nominal_PitchAccentType", task.getTrueFeature());
    assertEquals("hyp_pitch_accent_type", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PitchAccentClassificationFeatureSet);
  }

  @Test
  public void testGetIntonationalPhraseDetectionTask() {
    AuToBITask task = AuToBIUtils.getIntonationalPhraseDetectionTask();
    assertEquals("nominal_IntonationalPhraseBoundary", task.getTrueFeature());
    assertEquals("hyp_intonational_phrase_boundary", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof IntonationalPhraseBoundaryDetectionFeatureSet);
  }

  @Test
  public void testGetIntermediatePhraseDetectionTask() {
    AuToBITask task = AuToBIUtils.getIntermediatePhraseDetectionTask();
    assertEquals("nominal_IntermediatePhraseBoundary", task.getTrueFeature());
    assertEquals("hyp_intermediate_phrase_boundary", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof IntermediatePhraseBoundaryDetectionFeatureSet);
  }

  @Test
  public void testGetPhraseAccentClassificationTask() {
    AuToBITask task = AuToBIUtils.getPhraseAccentClassificationTask();
    assertEquals("nominal_PhraseAccent", task.getTrueFeature());
    assertEquals("hyp_phrase_accent", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PhraseAccentClassificationFeatureSet);
  }

  @Test
  public void testGetPABTClassificationTask() {
    AuToBITask task = AuToBIUtils.getPABTClassificationTask();
    assertEquals("nominal_PhraseAccentBoundaryTone", task.getTrueFeature());
    assertEquals("hyp_phrase_accent_boundary_tone", task.getHypFeature());
    assertTrue(task.getFeatureSet() instanceof PhraseAccentBoundaryToneClassificationFeatureSet);
  }
}
