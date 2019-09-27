package ca.mcgill.ecse211.lab2;

import static ca.mcgill.ecse211.lab2.Resources.*;
import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private static final double SENSOR_CENTER_CORRECTION = 4.5; //distance form the sensor to the center of the vehicle (cm)
  
  // sensor
  private SampleProvider sampleProvider = colorSensor.getRedMode();
  private float[] sampleColor = new float[colorSensor.sampleSize()];
  
  /*
   * Here is where the odometer correction code should be run.
   */
  public void run() {
    long correctionStart, correctionEnd;
    
    int numberOfLines = 0; //line counter for the number of black lines the vehicle crossed
    boolean walkingOnWood = true;
    boolean verti = true; //boolean to store whether the vehicle is moving in the vertical direction
    double internalReading;
    while (true) {
      correctionStart = System.currentTimeMillis();
      sampleProvider.fetchSample(sampleColor, 0);
      if(sampleColor[0]  <  0.12 && walkingOnWood) {
        Sound.beep();
        numberOfLines++;
        
        if(verti) {
          //vertical
          switch(numberOfLines) {
          //moving in positive y direction
            case 1: 
              odometer.setY(TILE_SIZE - SENSOR_CENTER_CORRECTION);
              break;
            case 2:
            	//internalReading = odometer.getXYT()[1]-(TILE_SIZE - SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal Y: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
              odometer.setY(TILE_SIZE * 2.0 - SENSOR_CENTER_CORRECTION);
              break;
            case 3:
            	//internalReading = odometer.getXYT()[1]-(TILE_SIZE * 2.0 - SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal Y: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setY(TILE_SIZE * 3.0 - SENSOR_CENTER_CORRECTION);
            	verti = false;
            	break;
          //moving in negative y direction
            case 7:
              odometer.setY(TILE_SIZE * 3.0 + SENSOR_CENTER_CORRECTION);
              break;
            case 8:
            	//internalReading = odometer.getXYT()[1]-(TILE_SIZE * 3.0 + SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal Y: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setY(TILE_SIZE * 2.0 + SENSOR_CENTER_CORRECTION);
            	break;
            case 9:
            	//internalReading = odometer.getXYT()[1]-(TILE_SIZE * 2.0 + SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal Y: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setY(TILE_SIZE + SENSOR_CENTER_CORRECTION);
            	verti = false;
            	break;
          }
        } else {
          //horizental
          switch(numberOfLines) {
          //moving in positive x direction
            case 4:
              odometer.setX(TILE_SIZE - SENSOR_CENTER_CORRECTION);
              break;
            case 5:
            	//internalReading = odometer.getXYT()[0]-(TILE_SIZE - SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal X: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setX(TILE_SIZE * 2.0 - SENSOR_CENTER_CORRECTION);
            	break;
            case 6:
            	//internalReading = odometer.getXYT()[0]-(TILE_SIZE * 2.0 - SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal X: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setX(TILE_SIZE * 3.0 - SENSOR_CENTER_CORRECTION);
            	verti = true;
            	break;
          //moving in negative x direction
            case 10:
              odometer.setX(TILE_SIZE * 3.0 - SENSOR_CENTER_CORRECTION);
              break;
            case 11:
            	//internalReading = odometer.getXYT()[0]-(TILE_SIZE * 3.0 + SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal X: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setX(TILE_SIZE * 2.0 + SENSOR_CENTER_CORRECTION);
            	break;
            case 12:
            	//internalReading = odometer.getXYT()[0]-(TILE_SIZE *2.0 + SENSOR_CENTER_CORRECTION);
            	//System.out.println("Internal X: " + internalReading + ", ratio: " + internalReading/TILE_SIZE);
            	odometer.setX(TILE_SIZE + SENSOR_CENTER_CORRECTION);
            	verti = true;
            	break;
          }
        }
        walkingOnWood = false;
        Main.sleepFor(1500); //sleeps the thread to avoid reading the same black line more than once
      }
      else {
        walkingOnWood = true;
      }

      // this ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }

}
