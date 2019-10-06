package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import java.util.Arrays;

import ca.mcgill.ecse211.lab4.OdometryCorrection.WorkingState;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;



public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  static final double SENSOR_CENTER_CORRECTION = 12.2; //distance form the sensor to the center of the vehicle (cm)
 //states
  enum WorkingState {
	    //state seeking y=1 black line with unknown theta
	    SEEK_Y,
	    //state aligning to line y=1 (theta = 90) with unknow x
	    ALIGN_X,
	    //state of first crossing the y=1 line after aligning with the y=1 line
	    CROSS_Y,
	    //state turning to theta = 180, marching forward, turning to theta = 270 backing up until x=1 line
	    SEEK_X,
	    //turn until x=1 line is met, travel to (1,1) way point with odometer
	    ALIGN_Y,
	    //finish
	    FINISHED;
  };
  WorkingState currentState =WorkingState.SEEK_Y;
  // sensor
  private SampleProvider sampleProvider = colorSensor.getRedMode();
  private float[] sampleColor = new float[colorSensor.sampleSize()];
  float prev;
  float derivative;
  /*
   * Here is where the odometer correction code should be run.
   */
  public void run() {
    long correctionStart, correctionEnd;
    currentState = WorkingState.SEEK_Y;
    leftMotor.forward();
    rightMotor.forward();
    while (true) {
      correctionStart = System.currentTimeMillis();
      sampleProvider.fetchSample(sampleColor, 0);
      prev = sampleColor[0];
      switch (currentState){
      case SEEK_Y: 
    	  //at the start, the robot point roughly at the horizontal wall, and backs onto the y=1 line
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setY(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  currentState = WorkingState.ALIGN_X;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(700); //sleeps the thread to avoid reading the same black line more than once
    		  break;
    	  }
      case ALIGN_X: 
    	  //seconds stage, the robot will turn counter clockwise until the sensor is directly on the y=1 line
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setTheta(90.0);
    		  currentState = WorkingState.CROSS_Y;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(700);
    		  break;
    	  }
      case CROSS_Y:
    	  //third stage, the robot moves in the -y direction until the sensor meets the line again
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setY(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  currentState = WorkingState.SEEK_X;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(700); //sleeps the thread to avoid reading the same black line more than once
    		  break;
    	  }
      case SEEK_X:
    	  //4th statge, robot continues forward in -y direction, then turns to -x direction, 
    	  //moves backwards until it detects a black line
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setX(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  currentState = WorkingState.ALIGN_Y;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(700);
    		  break;
    	  }
    	  //5th stage, the robot continues to move until the center is aligned with the black line, turns in place towards
    	  //the +y direction until the sensor detects the x=1 line, then moves forward to the (1,1) point
      case ALIGN_Y:
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setX(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  robotDriver.seekAndAlign(this);
    		  currentState = WorkingState.FINISHED;
    	      Main.sleepFor(700);
    		  break;
    	  } 
      case FINISHED:
    	  //final statge, robot is stopped at (1,1), and the coordinates are reset
    	  robotDriver.seekAndAlign(this);
		  odometer.setXYT(TILE_SIZE,TILE_SIZE,0.0);
    	  break; 
      }
      // this ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }
  
  boolean detectBlackLine() { 
	  //System.out.println(sampleColor[0]);
	  return sampleColor[0] < 0.45;
	  //return false;
//	  //TODO: this needs some more work
//	  sampleProvider.fetchSample(sampleColor, 0);
//      derivative = sampleColor[0] - prev;
//      prev = sampleColor[0];
//      return derivative < -0.03;
  }

}
