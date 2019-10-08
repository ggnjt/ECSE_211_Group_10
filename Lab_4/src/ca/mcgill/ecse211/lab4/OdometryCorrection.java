package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;

  // distance form the sensor to the center of the vehicle (cm)
  static final double SENSOR_CENTER_CORRECTION = 12.2;

  // states
  enum WorkingState {
    // Turn 90 until facing the wall
    TURN_90,
    // back up until you see a line state seeking y=1 black line with unknown theta
    CRAM_THE_WALL,
    // state aligning to line y=1 (theta = 90) with unknow x
    GET_BACK,
    // state turning to theta = 180, marching forward, turning to theta = 270 backing up until x=1 line
    GO_TO_GOAL,
    // turn until x=1 line is met, travel to (1,1) way point with odometer
    ALIGN,
    // finish
    FINISHED;
  };

  WorkingState currentState = WorkingState.TURN_90;

  // sensor
  private SampleProvider sampleProvider = colorSensor.getRedMode();
  private float[] sampleColor = new float[colorSensor.sampleSize()];
  float prev;
  float derivative;
  int CrammerCounter =0;
  
  public void run() {
    long correctionStart, correctionEnd;
    while (true) {
      correctionStart = System.currentTimeMillis();
      sampleProvider.fetchSample(sampleColor, 0);
      prev = sampleColor[0];
      switch (currentState) {
        case TURN_90:
          setSpeed(ROTATE_SPEED);
          leftMotor.rotate(convertAngle(-90.0), true);
          rightMotor.rotate(convertAngle(90.0), false);
          stopTheRobot();
          currentState = WorkingState.CRAM_THE_WALL;
          break;
        case CRAM_THE_WALL:
          setSpeed(ROTATE_SPEED);
          leftMotor.forward();
          rightMotor.forward();
          CrammerCounter++;
          if(CrammerCounter > 1000) {
            currentState = WorkingState.GET_BACK;
            stopTheRobot();
          }
          break;
        case GET_BACK:
          setSpeed(ROTATE_SPEED);
          leftMotor.rotate(convertDistance(-10.0), true);
          rightMotor.rotate(convertDistance(-10.0), false);
          currentState = WorkingState.GO_TO_GOAL;
          break;
        case GO_TO_GOAL:
          setSpeed(ROTATE_SPEED);
          leftMotor.rotate(convertAngle(135.0), true);
          rightMotor.rotate(convertAngle(-135.0), false);
          leftMotor.rotate(convertDistance(20.0), true);
          rightMotor.rotate(convertDistance(20.0), false);
          currentState = WorkingState.ALIGN;
          break;
          
        case ALIGN:
          setSpeed(ROTATE_SPEED);
          leftMotor.rotate(convertAngle(-45.0), true);
          rightMotor.rotate(convertAngle(45.0), false);
          currentState = WorkingState.FINISHED;
          break;
        default:
          stopTheRobot();
          break;
      }
      
      // this ensures the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
      }
    }
  }

  /**
   * Stop the robot
   */
  private static void stopTheRobot() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance
   * @return the wheel rotations necessary to cover the distance
   */
  private static int convertDistance(double distance) { // always positive
    return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that angle.
   * 
   * @param angle angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  private static int convertAngle(double angle) { // can be negative
    return convertDistance(Math.PI * TRACK * angle / 360.0);
  }

  /**
   * calculates the displacement needed to move the the desired coordinates
   * 
   * @return distance needed to travel to a waypoint.
   */
  private static double calculateDistance(Odometer odo, int waypointX, int waypointY) {

    double currentX = odo.getXYT()[0];
    double currentY = odo.getXYT()[1];
    double Xtarget = waypointX * TILE_SIZE;
    double Ytarget = waypointY * TILE_SIZE;

    double X2go = Xtarget - currentX;
    double Y2go = Ytarget - currentY;

    return Math.sqrt(Math.pow(X2go, 2) + Math.pow(Y2go, 2));
  }

  /**
   * This calculates the angle needed to travel to a waypoint based on the current position stored in the odometer
   * 
   * @param odo the odometer
   * @param waypointX x-coordinate of the robot
   * @param waypointY y-coordinate of the robot
   * @return the minimum angle rotation needed to point to the waypoint
   */
  private static double calculateAngle(Odometer odo, int waypointX, int waypointY) {

    double currentX = odo.getXYT()[0];
    double currentY = odo.getXYT()[1];
    double currentTheta = odo.getXYT()[2];
    double Xtarget = waypointX * TILE_SIZE;
    double Ytarget = waypointY * TILE_SIZE;

    double X2go = Xtarget - currentX;
    double Y2go = Ytarget - currentY;

    double angleTarget = Math.atan(X2go / Y2go) / Math.PI * 180;
    if (Y2go < 0) {
      angleTarget += 180;
    }
    double angleDeviation = angleTarget - currentTheta;

    if (Math.abs(angleDeviation) < 180)
      return angleDeviation;
    else if (angleDeviation > 180) {
      return angleDeviation - 360;
    } else {
      return angleDeviation + 360;
    }
  }


  private static int getAngleRotation(Odometer odo, int X, int Y) {
    return convertAngle(calculateAngle(odo, X, Y));
  }

  // This is a blocking Turn (blocks other threads)
  private static void turnTo(int X, int Y) {
    leftMotor.rotate(getAngleRotation(odometer, X, Y), true);
    rightMotor.rotate(-getAngleRotation(odometer, X, Y), false);
  }

  // This is a blocking GoTo (blocks other threads)
  private static void goTo(int X, int Y) {
    leftMotor.rotate(convertDistance(calculateDistance(odometer, X, Y)), true);
    rightMotor.rotate(convertDistance(calculateDistance(odometer, X, Y)), false);
  }

  /**
   * scans the field in a clockwise fashion the first low in distance reading AFTER HIGH READING should be when theta =
   * 180 the second low in distance should be when theta = 270
   */
  private static void scanField() {
    setSpeed(100);
    leftMotor.rotate(convertAngle(360.0), true);
    rightMotor.rotate(-convertAngle(360.0), false);
  }

  /**
   * set Speed
   */
  private static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

}
