package ca.mcgill.ecse211.lab2;

import static ca.mcgill.ecse211.lab2.Resources.*;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  
  // sensor
  private SampleProvider sampleProvider = colorSensor.getRedMode();
  private float[] sampleColor = new float[colorSensor.sampleSize()];

  /*
   * Here is where the odometer correction code should be run.
   */
  public void run() {
    long correctionStart, correctionEnd;
    
    int numberOfLines = 0;
    boolean walkingOnWood = true;
    
    while (true) {
      correctionStart = System.currentTimeMillis();

      sampleProvider.fetchSample(sampleColor, 0);
      //System.out.println(Arrays.toString(sampleColor));
      if(sampleColor[0]  <  0.073 && walkingOnWood) {
        Sound.beep();
        numberOfLines++;
        System.out.println("increased to " + numberOfLines);
        walkingOnWood = false;
        Main.sleepFor(1500);
      }
      else {
        walkingOnWood = true;
      }
      // TODO Trigger correction (When do I have information to correct?)

      // TODO Calculate new (accurate) robot position

      // TODO Update odometer with new calculated (and more accurate) values, eg:
      // odometer.setXYT(0.3, 19.23, 5.0);

      // this ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }

}
