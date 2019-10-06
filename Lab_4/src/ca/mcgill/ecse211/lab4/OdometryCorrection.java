package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;



public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  static final double SENSOR_CENTER_CORRECTION = 2.2; //distance form the sensor to the center of the vehicle (cm)
 //states
  enum WorkingState {
	    //state seeking y=1 black line with unknown theta
	    SEEK_Y,
	    //state aligning to line y=1 (theta = 90) with unknow x
	    ALIGN_X,
	    //state of first crossing the y=1 line after aligning with the y=1 line
	    CROSS_Y,
	    //state turning to theta = 180, marching forward, turning to theta = 90 marching forward until x=1 line
	    SEEK_X,
	    //turn until y line is met, travel to (1,1) way point with odometer
	    ALIGN_Y,
	    //finish
	    FINISHED;
  };
  WorkingState currentState;
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
    	      Main.sleepFor(1500); //sleeps the thread to avoid reading the same black line more than once
    		  break;
    	  }
      case ALIGN_X: 
    	  //seconds stage, the robot will turn counter clockwise until the sensor is directly on the y=1 line
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setTheta(90.0);
    		  currentState = WorkingState.CROSS_Y;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(1500);
    		  break;
    	  }
      case CROSS_Y:
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setY(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  currentState = WorkingState.SEEK_X;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(1500); //sleeps the thread to avoid reading the same black line more than once
    		  break;
    	  }
      case SEEK_X:
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setX(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  currentState = WorkingState.ALIGN_Y;
    		  robotDriver.seekAndAlign(this);
    	      Main.sleepFor(1500);
    		  break;
    	  }
      case ALIGN_Y:
    	  if (detectBlackLine()) {
    		  Sound.beep();
    		  odometer.setX(TILE_SIZE-SENSOR_CENTER_CORRECTION);
    		  robotDriver.seekAndAlign(this);
    		  currentState = WorkingState.FINISHED;
    	      Main.sleepFor(1500);
    		  break;
    	  }
      case FINISHED:
    	  robotDriver.seekAndAlign(this);
		  odometer.setTheta(0.0);
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
	 // System.out.println(sampleColor[0]);
	  return sampleColor[0] < 2.0;
	  
//	  //TODO: this needs some more work
//	  sampleProvider.fetchSample(sampleColor, 0);
//      derivative = sampleColor[0] - prev;
//      prev = sampleColor[0];
//      return derivative < -0.03;
  }

}
