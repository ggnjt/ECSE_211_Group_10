package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.robotics.SampleProvider;

public class ColorReader implements Runnable {
  private static final long SENSORTIMERlIMIT = 50;

  // sensor
  private SampleProvider sampleProvider = colorSensor.getRedMode();
  private float[] sampleColor = new float[colorSensor.sampleSize()];
  private float sample;

  public void run() {
    long readingStart, readingEnd;
    readingStart = System.currentTimeMillis();

    sampleProvider.fetchSample(sampleColor, 0);
    sample = sampleColor[0];

    // this ensures reading
    readingEnd = System.currentTimeMillis();
    if (readingEnd - readingStart < SENSORTIMERlIMIT) {
      Main.sleepFor(SENSORTIMERlIMIT - (readingEnd - readingStart));
    }
  }

  public float getSample() {
    return sample;
  }
}

