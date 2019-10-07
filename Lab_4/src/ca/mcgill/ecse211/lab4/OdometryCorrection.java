package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import com.sun.corba.se.spi.orbutil.fsm.State;
import ca.mcgill.ecse211.lab4.OdometryCorrection.WorkingState;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;

  // distance form the sensor to the center of the vehicle (cm)
  static final double SENSOR_CENTER_CORRECTION = 12.2;

  // states
  enum WorkingState {
    // Turn back to our position
    TURN_BACK,
    // back up until you see a line state seeking y=1 black line with unknown theta
    SEEK_Y,
    // state aligning to line y=1 (theta = 90) with unknow x
    ALIGN_X,
    // state turning to theta = 180, marching forward, turning to theta = 270 backing up until x=1 line
    SEEK_X,
    // turn until x=1 line is met, travel to (1,1) way point with odometer
    ALIGN_Y,
    // finish
    FINISHED;
  };

  WorkingState currentState = WorkingState.TURN_BACK;

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
    while (true) {
      correctionStart = System.currentTimeMillis();
      sampleProvider.fetchSample(sampleColor, 0);
      prev = sampleColor[0];
      switch (currentState) {
        case TURN_BACK:
          setSpeed(ROTATE_SPEED);
          leftMotor.rotate(convertAngle(180.0), true);
          rightMotor.rotate(convertAngle(-180.0), false);
          stopTheRobot();
          leftMotor.backward();
          rightMotor.backward();
          currentState = WorkingState.SEEK_Y;
          break;
        case SEEK_Y:
          if (ColorReader.detectBlackLine()) {
            stopTheRobot();
            Sound.beep();
            odometer.setY(TILE_SIZE - SENSOR_CENTER_CORRECTION);
            currentState = WorkingState.ALIGN_X;
            leftMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
            rightMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
            leftMotor.backward();
            rightMotor.forward();
            try {
              Thread.sleep(700);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } // sleeps the thread to avoid reading the same black line more than once
            break;
          }
        case ALIGN_X:
          // seconds stage, the robot will turn counter clockwise until the sensor is directly on the y=1 line
          if (detectBlackLine()) {
            stopTheRobot();
            Sound.beep();
            odometer.setTheta(90.0);
            leftMotor.rotate(convertAngle(-90.0), true);
            rightMotor.rotate(convertAngle(90.0), false);
            leftMotor.rotate(-3, true);
            rightMotor.rotate(-3, false);
            currentState = WorkingState.SEEK_X;
            try {
              Thread.sleep(700);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            break;
          }
        case SEEK_X:
          // 4th statge, robot continues forward in -y direction, then turns to -x direction,
          // moves backwards until it detects a black line
          if (detectBlackLine()) {
            Sound.beep();
            odometer.setX(TILE_SIZE - SENSOR_CENTER_CORRECTION);
            currentState = WorkingState.ALIGN_Y;
            seekAndAlign();
            Main.sleepFor(700);
            break;
          }
          // 5th stage, the robot continues to move until the center is aligned with the black line, turns in place
          // towards
          // the +y direction until the sensor detects the x=1 line, then moves forward to the (1,1) point
        case ALIGN_Y:
          if (detectBlackLine()) {
            Sound.beep();
            odometer.setX(TILE_SIZE - SENSOR_CENTER_CORRECTION);
            seekAndAlign();
            currentState = WorkingState.FINISHED;
            Main.sleepFor(700);
            break;
          }
        case FINISHED:
          // final stage, robot is stopped at (1,1), and the coordinates are reset
          seekAndAlign();
          odometer.setXYT(TILE_SIZE, TILE_SIZE, 0.0);
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
    return sampleColor[0] < 0.45;
    // return false;
    // //TODO: this needs some more work
    // sampleProvider.fetchSample(sampleColor, 0);
    // derivative = sampleColor[0] - prev;
    // prev = sampleColor[0];
    // return derivative < -0.03;
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

  /**
   * using the color sensor (and assuming that the robot is facing the positive y general direction), this function
   * tells the robot to march forward until it is on the (x,1) line, then rotate clock-wise until the color sensor
   * aligns with the black line, at which moment the robot would have a theta value of 90
   */
  void seekAndAlign() {
    setSpeed(120);
   
   if (currentState == WorkingState.SEEK_X) {
      stopTheRobot();
      leftMotor.rotate(convertDistance(5.0), true);
      rightMotor.rotate(convertDistance(5.0), false);
      leftMotor.rotate(convertAngle(90.0), true);
      rightMotor.rotate(convertAngle(-90.0), false);
      leftMotor.backward();
      rightMotor.backward();
    } else if (currentState == WorkingState.ALIGN_Y) {
      stopTheRobot();
      leftMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
      rightMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
      leftMotor.rotate(convertAngle(90.0), true);
      rightMotor.rotate(convertAngle(-90.0), false);
      leftMotor.rotate(convertDistance(5.0 + OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
      rightMotor.rotate(convertDistance(5.0 + OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
    } else if (currentState == WorkingState.FINISHED) {
      stopTheRobot();
    }
  }


}
