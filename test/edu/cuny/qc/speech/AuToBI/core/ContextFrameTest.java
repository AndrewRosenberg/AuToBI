package edu.cuny.qc.speech.AuToBI.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Test Class for ContextFrame.
 *
 * @see ContextFrame
 */
public class ContextFrameTest {

  @Test
  public void testIncrementWorksWithNullAttributes() {
    List<Word> words = new ArrayList<Word>();
    words.add(new Word(0, 1, "one"));
    words.add(new Word(0, 1, "two"));
    words.add(new Word(0, 1, "three"));
    words.add(new Word(0, 1, "four"));
    words.add(new Word(0, 1, "five"));

    ContextFrame frame = new ContextFrame(words, "test_feature", 1, 0);

    frame.init();
    try {
    frame.increment();
    } catch (NullPointerException e) {
      fail();
    }
  }
}
