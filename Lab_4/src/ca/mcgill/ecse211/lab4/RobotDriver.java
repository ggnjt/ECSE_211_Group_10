package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.leftMotor;
import static ca.mcgill.ecse211.lab4.Resources.rightMotor;
// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab4.Resources.*;
import java.util.Arrays;

import ca.mcgill.ecse211.lab4.OdometryCorrection.WorkingState;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class RobotDriver {
	
	
	

  /**
   * Stop the robot
   */
  private static void stopTheRobot() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }
  
  /**
   * set acceleration for both motors
   */
  private static void setAcceleration(int acceleration) {
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }
  
  /**
   * set Speed
   */
  private static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
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
   * scans the field in a clockwise fashion
   * the first low in distance reading AFTER HIGH READING should be when theta = 180
   * the second low in distance should be when theta = 270
   */
  private static void scanField() {
	  setSpeed(100);
	  leftMotor.rotate(convertAngle(360.0), true);
      rightMotor.rotate(-convertAngle(360.0), false); 
  }
  
  /**
   * using the color sensor (and assuming that the robot is facing the positive y general direction),
   * this function tells the robot to march forward until it is on the (x,1) line, then rotate clock-wise
   * until the color sensor aligns with the black line, at which moment the robot would have a theta value of 90
   */
  void seekAndAlign(OdometryCorrection oc) {
	  setSpeed(120);
	  if (oc.currentState == WorkingState.SEEK_Y) {
		  leftMotor.backward();
		  rightMotor.backward();

	  }
	  else if (oc.currentState == WorkingState.ALIGN_X) {
		  stopTheRobot();
		  leftMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
		  rightMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
		  leftMotor.backward();
		  rightMotor.forward();
	  }
	  else if (oc.currentState == WorkingState.CROSS_Y) {
		  stopTheRobot();
		  leftMotor.rotate(convertAngle(90.0), true);
		  rightMotor.rotate(convertAngle(-90.0), false);
		  leftMotor.forward();
		  rightMotor.forward();		  
	  }
	  else if (oc.currentState == WorkingState.SEEK_X) {
		  stopTheRobot();
		  leftMotor.rotate(convertDistance(5.0), true);
		  rightMotor.rotate(convertDistance(5.0), false);
		  leftMotor.rotate(convertAngle(90.0), true);
		  rightMotor.rotate(convertAngle(-90.0), false);
		  leftMotor.backward();
		  rightMotor.backward();		  
	  }
	  else if (oc.currentState == WorkingState.ALIGN_Y) {
		  stopTheRobot();
		  leftMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
		  rightMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
		  leftMotor.rotate(convertAngle(90.0), true);
		  rightMotor.rotate(convertAngle(-90.0), false);
		  leftMotor.rotate(convertDistance(5.0+OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
		  rightMotor.rotate(convertDistance(5.0+OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
	  }
	  else if (oc.currentState == WorkingState.FINISHED) {
		  stopTheRobot();
	  }
  }
}
