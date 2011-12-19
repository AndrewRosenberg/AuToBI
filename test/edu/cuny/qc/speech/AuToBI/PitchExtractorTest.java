/*  PitchExtractorTest.java

    Copyright (c) 2011 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import org.junit.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for PitchExtractor.
 */
public class PitchExtractorTest {


//  TODO: write tests for this functionality
//  public static void main(String[] args) throws IOException, UnsupportedAudioFileException, AuToBIException {
//    File file = new File(args[0]);
//    AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
//
//    WavReader reader = new WavReader();
//    WavData wav;
//    if (args.length > 1)
//      wav = reader.read(soundIn, Double.parseDouble(args[1]), Double.parseDouble(args[2]));
//    else
//      wav = reader.read(soundIn);
//
//
//    System.out.println(wav.sampleRate);
//    System.out.println(wav.sampleSize);
//    System.out.println(wav.getFrameSize());
//    System.out.println(wav.getDuration());
//
//    PitchExtractor pitchFactory = new PitchExtractor(wav);
//    Contour pitch = pitchFactory.soundToPitch();
//    System.out.println("wav length: " + wav.getDuration());
//
//    wav = null;
//
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    System.gc();
//    long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//    System.out.println("Memory Used (Mb): " + (memory / 1024.0 / 1024));
//
//    System.out.println("pitch points:" + pitch.contentSize());
//  }
}
