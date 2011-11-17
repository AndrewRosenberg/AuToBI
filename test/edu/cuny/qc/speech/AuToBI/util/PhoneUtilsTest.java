/*  PhoneUtilsTest.java

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
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 1/4/11 Time: 10:30 AM To change this template use File | Settings | File
 * Templates.
 */
public class PhoneUtilsTest {

  @Test
  public void testIsVowelTrue() {
    assertTrue(PhoneUtils.isVowel("A"));
    assertTrue(PhoneUtils.isVowel("E"));
    assertTrue(PhoneUtils.isVowel("I"));
    assertTrue(PhoneUtils.isVowel("O"));
    assertTrue(PhoneUtils.isVowel("U"));

    assertTrue(PhoneUtils.isVowel("a"));
    assertTrue(PhoneUtils.isVowel("e"));
    assertTrue(PhoneUtils.isVowel("i"));
    assertTrue(PhoneUtils.isVowel("o"));
    assertTrue(PhoneUtils.isVowel("u"));
  }

  @Test
  public void testIsVowelFalse() {
    assertFalse(PhoneUtils.isVowel("V"));
    assertFalse(PhoneUtils.isVowel("B"));
    assertFalse(PhoneUtils.isVowel("M"));
    assertFalse(PhoneUtils.isVowel("P"));
    assertFalse(PhoneUtils.isVowel("N"));
    assertFalse(PhoneUtils.isVowel("Q"));
  }

  @Test
  public void testIsVowelNull() {
    assertFalse(PhoneUtils.isVowel(null));
  }

  @Test
  public void testIsVowelEmpty() {
    assertFalse(PhoneUtils.isVowel(""));
  }


}
