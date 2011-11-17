/*  PhoneUtils.java

    Copyright 2010 Andrew Rosenberg

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to hold utility function related to phones.
 */
public class PhoneUtils {

  // Utility classes cannot be constructed.
  private PhoneUtils() {
    throw new AssertionError();
  }

  /**
   * Return true if the phone represents a vowel.
   *
   * @param phone_label the phone label to evaluateGaussianPDF.
   * @return true if the phone is a vowel
   */
  public static boolean isVowel(String phone_label) {

    if (phone_label == null) {
      return false;
    }
    Pattern vowel_p = Pattern.compile("[AIEOUaeiou]");
    Matcher vowel_m = vowel_p.matcher(phone_label);
    return vowel_m.find();
  }
}
