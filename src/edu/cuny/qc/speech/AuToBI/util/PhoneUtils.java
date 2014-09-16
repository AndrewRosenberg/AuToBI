/*  PhoneUtils.java

    Copyright 2010-2014 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
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
