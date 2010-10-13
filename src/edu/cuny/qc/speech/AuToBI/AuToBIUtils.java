/*  AuToBIUtils.java

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
package edu.cuny.qc.speech.AuToBI;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

import org.apache.oro.io.GlobFilenameFilter;
import org.apache.log4j.Logger;

/**
 * Stores general utility functions for AuToBI.
 * <p/>
 * Currently handles the resolution of filenames, an interface to log4j logging and string manipulation functions.
 */
public class AuToBIUtils {
  // A log4j logger
  private static Logger logger = Logger.getLogger(AuToBIUtils.class);

  /**
   * Logs a message to the logger.
   *
   * @param s the message
   */
  public static void log(String s) {
    logger.info(s);
  }

  /**
   * Sends a debug message to the logger.
   *
   * @param s the message
   */
  public static void debug(String s) {
    logger.debug(s);
  }

  /**
   * Sends a warning message to the logger.
   *
   * @param s the message
   */
  public static void warn(String s) {
    logger.warn(s);
  }

  /**
   * Sends an error message to the logger.
   *
   * @param s the message
   */
  public static void error(String s) {
    logger.error(s);
  }

  /**
   * Sends an information message to the logger.
   *
   * @param s the message
   */
  public static void info(String s) {
    logger.info(s);
  }


  /**
   * Globs a file pattern into a list of full path names that match the pattern.
   *
   * @param pattern The file pattern
   * @return a list of matching files.
   */
  public static List<String> glob(String pattern) {
    File dir;

    ArrayList<String> filenames = new ArrayList<String>();
    for (String file_pattern : pattern.split(",")) {
      file_pattern = file_pattern.replaceAll("~", System.getProperty("user.home"));
      if (!file_pattern.startsWith("/")) {
        file_pattern = System.getProperty("user.dir") + "/" + file_pattern;
      }
      file_pattern = file_pattern.substring(1, file_pattern.length());
      dir = new File("/");

      for (String file_name : getFileList(dir, file_pattern))
        filenames.add(file_name);
    }
    return filenames;
  }

  /**
   * Globs a single file from a list of full path names that match the pattern.
   * <p/>
   * Throws an error if the pattern matches multiple files.
   *
   * @param pattern The file pattern.
   * @return the matching file
   * @throws AuToBIException if the pattern matches multiple files
   */
  public static String globSingleFile(String pattern) throws AuToBIException {
    if (pattern.contains(",")) {
      throw new AuToBIException("Pattern, " + pattern + ", matches multiple files");
    }

    ArrayList<String> filenames = new ArrayList<String>();
    String file_pattern = pattern.replaceAll("~", System.getProperty("user.home"));
    if (!file_pattern.startsWith("/")) {
      file_pattern = System.getProperty("user.dir") + "/" + file_pattern;
    }
    file_pattern = file_pattern.substring(1, file_pattern.length());
    File dir = new File("/");

    for (String file_name : getFileList(dir, file_pattern))
      filenames.add(file_name);

    if (filenames.size() > 1) {
      throw new AuToBIException("Pattern, " + pattern + ", matches multiple files");
    }
    if (filenames.size() == 0) {
      throw new AuToBIException("Pattern, " + pattern + ", does not match any files");
    }
    return filenames.get(1);
  }

  /**
   * Convert a collection to a string with elements joined by a delimiter.
   * <p/>
   * e.g. ["a" "b" "c"] -> "a,b,c"
   *
   * @param collection the collection
   * @param delimiter  the delimiter
   * @return a string containing the elements of the collection
   */
  public static String join(Collection<?> collection, String delimiter) {
    if (collection.isEmpty()) return "";
    Iterator<?> iter = collection.iterator();
    StringBuffer buffer = new StringBuffer(iter.next().toString());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next().toString());
    return buffer.toString();
  }

  /**
   * Recursively generates the list of files within directory that match the file_pattern
   *
   * @param dir          The directory
   * @param file_pattern The filename pattern
   * @return A list of file names  in the directory that match the patter
   */
  public static List<String> getFileList(File dir, String file_pattern) {
    ArrayList<String> file_names = new ArrayList<String>();
    String[] path_elements = file_pattern.split("/");

    if (path_elements[0].equals(".")) {
      String next_file_pattern = construct_path_string(path_elements);
      file_names.addAll(getFileList(dir, next_file_pattern));
    } else if (path_elements[0].equals("..")) {// traverse up one directory
      String next_file_pattern = construct_path_string(path_elements);
      file_names.addAll(getFileList(dir.getParentFile(), next_file_pattern));
    } else {
      GlobFilenameFilter filter = new GlobFilenameFilter(path_elements[0]);
      File[] files = dir.listFiles((FilenameFilter) filter);
      for (File f : files) {
        if (path_elements.length > 1) {// traverse sub directories
          if (f.exists() && f.isDirectory()) {
            String next_file_pattern = construct_path_string(path_elements);
            file_names.addAll(getFileList(f, next_file_pattern));
          }
        } else {
          if (f.exists()) {
            file_names.add(f.getAbsolutePath());
          }
        }
      }
    }
    return file_names;
  }

  /**
   * Constructs a string representation of a path from an array of path elements
   * <p/>
   * e.g.
   * <p/>
   * ["" "usr" "local" "bin"] -> "/usr/local/bin"
   *
   * @param path_elements an array of path components
   * @return a string representation of the path
   */
  private static String construct_path_string(String[] path_elements) {
    String next_file_pattern = "";
    for (int i = 1; i < path_elements.length; ++i) {
      next_file_pattern += path_elements[i] + "/";
    }
    next_file_pattern = next_file_pattern.substring(0, next_file_pattern.length() - 1);
    return next_file_pattern;
  }

  /**
   * Constructs merged hypotheses for phrase ending tones and pitch accents by merging hypotheses from the six detection
   * and classification tasks.
   *
   * @param words the words to analyse
   */
  public static void mergeAuToBIHypotheses(List<Word> words) {

    for (Word word : words) {

      // Assigns pitch accents to words.  If only accent detection is available, a binary True/False hypothesis
      // will be assigned.  If location and type hypotheses are available, the hypothesized type will be assigned.
      // Finally, if only type information is available, every word will be assigned its best guess for accent type.
      if (word.hasAttribute("hyp_pitch_accent_location")) {
        if (word.hasAttribute("hyp_pitch_accent_location_conf")) {
          Double conf = (Double) word.getAttribute("hyp_pitch_accent_location_conf");
          if (!word.getAttribute("hyp_pitch_accent_location").equals("ACCENTED")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_pitch_accent", "ACCENTED: " + conf);
        } else {
          word.setAttribute("hyp_pitch_accent", word.getAttribute("hyp_pitch_accent_location"));
        }
      }
      if (word.hasAttribute("hyp_pitch_accent_type")) {
        if (!word.hasAttribute("hyp_pitch_accent") || word.getAttribute("hyp_pitch_accent").equals("ACCENTED")) {
          word.setAttribute("hyp_pitch_accent", word.getAttribute("hyp_pitch_accent_type"));
        }
      }

      // Assigns phrase ending tones.
      if (word.hasAttribute("hyp_IP_location")) {
        if (word.hasAttribute("hyp_IP_location_conf")) {
          Double conf = (Double) word.getAttribute("hyp_IP_location_conf");
          if (!word.getAttribute("hyp_IP_location").equals("INTONATIONAL_BOUNDARY")) {
            conf = 1 - conf;
          }
          word.setAttribute("hyp_phrase_boundary", "BOUNDARY: " + conf);
        } else {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_IP_location"));
        }
      }

      if (word.hasAttribute("hyp_ip_location")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("NONBOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_ip_location"));
        }
      }

      if (word.hasAttribute("hyp_boundary_tone")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTONATIONAL_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_boundary_tone"));
        }
      }

      if (word.hasAttribute("hyp_phrase_accent")) {
        if (!word.hasAttribute("hyp_phrase_boundary") ||
            word.getAttribute("hyp_phrase_boundary").equals("INTERMEDIATE_BOUNDARY")) {
          word.setAttribute("hyp_phrase_boundary", word.getAttribute("hyp_phrase_accent"));
        }
      }
    }
  }
}