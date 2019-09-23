package ca.mcgill.ecse211.lab2;

import static ca.mcgill.ecse211.lab2.Resources.*;
import java.util.Arrays;
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
    boolean verti = true;
    
    while (true) {
      correctionStart = System.currentTimeMillis();

      sampleProvider.fetchSample(sampleColor, 0);
      if(sampleColor[0]  <  0.12 && walkingOnWood) {
        Sound.beep();
        numberOfLines++;
        
        //get robots current position
        double[] position = odometer.getXYT();
        
        if(verti) {
          //vertical
          switch(numberOfLines) {
            case 1:
              odometer.setY(TILE_SIZE - 4.0);
              break;
            case 2:
              odometer.setY(TILE_SIZE * 2.0 - 4.0);
              break;
            case 3:
              odometer.setY(TILE_SIZE * 3.0 - 4.0);
              verti = false;
              break;
            case 7:
              odometer.setY(TILE_SIZE * 3.0 + 4.0);
              break;
            case 8:
              odometer.setY(TILE_SIZE * 2.0 + 4.0);
              break;
            case 9:
              odometer.setY(TILE_SIZE + 4.0);
              verti = false;
              break;
          }
        } else {
          //horizental
          switch(numberOfLines) {
            case 4:
              odometer.setX(TILE_SIZE - 4.0);
              break;
            case 5:
              odometer.setX(TILE_SIZE * 2.0 - 4.0);
              break;
            case 6:
              odometer.setX(TILE_SIZE * 3.0 - 4.0);
              verti = true;
              break;
            case 10:
              odometer.setX(TILE_SIZE * 3.0 + 4.0);
              break;
            case 11:
              odometer.setX(TILE_SIZE * 2.0 + 4.0);
              break;
            case 12:
              odometer.setX(TILE_SIZE + 4.0);
              verti = true;
              break;
          }
        }
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
