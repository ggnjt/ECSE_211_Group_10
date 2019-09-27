package ca.mcgill.ecse211.lab3;

// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab3.Resources.*;
import static ca.mcgill.ecse211.lab3.UltrasonicPoller.*;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class RobotDriver {

  /**
   * Drives the robot in a square of size 3x3 Tiles. It is to be run in parallel with the odometer and odometer
   * correction classes to allow testing their functionality.
   */
  public static void drive() {
    // spawn a new Thread to avoid this method blocking
    (new Thread() {
      public void run() {
        // reset the motors
        leftMotor.stop();
        rightMotor.stop();
        leftMotor.setAcceleration(ACCELERATION);
        rightMotor.setAcceleration(ACCELERATION);


        // Sleep for 2 seconds
        Main.sleepFor(TIMEOUT_PERIOD);
        
        turnTo(3,2);
        goTo(3,2);
        turnTo(1,3);
        goTo(1,3);
        turnTo(2,2);
        goTo(2,2);
        
        /**
         * YP: not too sure what is the best way to switch between the threads...
         */
      }
    }).start();
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) { //always positive
    return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that angle.
   * 
   * @param angle
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) { //can be negative
    return convertDistance(Math.PI * TRACK * angle / 360.0);
  }
  
  /**
   * calculates the displacement needed to move the the desired coordinates
   * @return
   */
  public static double calculateDistance(Odometer odo, int waypointX, int waypointY) {
	
	double currentX = odo.getXYT()[0];
	double currentY = odo.getXYT()[1];
	double Xtarget = waypointX * TILE_SIZE;
	double Ytarget = waypointY * TILE_SIZE;

	double X2go = Xtarget - currentX;
	double Y2go = Ytarget - currentY;
	
	return Math.sqrt(Math.pow(X2go, 2)+Math.pow(Y2go, 2));
  }
  
  public static double calculateAngle (Odometer odo, int waypointX, int waypointY) {
	  
	double currentX = odo.getXYT()[0];
	double currentY = odo.getXYT()[1];
	double currentTheta = odo.getXYT()[2];
	double Xtarget = waypointX * TILE_SIZE;
	double Ytarget = waypointY * TILE_SIZE;
	
	double X2go = Xtarget - currentX;
	double Y2go = Ytarget - currentY;
	
	//System.out.println(X2go/Y2go);
	double angleTarget = Math.atan(X2go/Y2go)/Math.PI*180;
	if (Y2go < 0) {
		angleTarget += 180;
	}
	//System.out.println(+Math.atan(X2go/Y2go)/Math.PI*180);
	double angleDeviation = angleTarget - currentTheta;
	
//	System.out.println("Current coord: " + currentX + "," + currentY);
//	System.out.println("Current t: " + currentTheta);
//	System.out.println("distance to " + waypointX + ","+waypointY +": " + X2go + "," + Y2go);
//	System.out.println("Angle destiny: " + angleTarget + ", deviation: " + angleDeviation);
	return (angleDeviation > 180 ? angleDeviation-360 : angleDeviation);
	
	
  }
  
  private static int getAngleRotation (Odometer odo, int X, int Y) {
	  return convertAngle(calculateAngle(odo, X, Y));
  }
  
  private static void turnTo(int X, int Y) {
	  leftMotor.rotate(getAngleRotation(odometer, X, Y), true);
      rightMotor.rotate(-getAngleRotation(odometer, X, Y), false);
  }
  private static void goTo(int X, int Y) {
	  
	  
      leftMotor.rotate(convertDistance(calculateDistance(odometer, X, Y)), true);
      rightMotor.rotate(convertDistance(calculateDistance(odometer, X, Y)), false);  
  }
  
  
}
