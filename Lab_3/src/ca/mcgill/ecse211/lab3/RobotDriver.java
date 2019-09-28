package ca.mcgill.ecse211.lab3;

// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab3.Resources.*;
import java.util.Arrays;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class RobotDriver {

  /**
   * Drives the robot in a square of size 3x3 Tiles. It is to be run in parallel with the odometer and odometer
   * correction classes to allow testing their functionality.
   */
	public static void drive() {// spawn a new Thread to avoid this method blocking
		(new Thread() {
			public void run() {
				// reset the motors
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setAcceleration(ACCELERATION);
				rightMotor.setAcceleration(ACCELERATION);


				// Sleep for 2 seconds
				Main.sleepFor(TIMEOUT_PERIOD);
        
				turnTo(2,3);
				LorR();
				goTo(2,3);
				LorR();
				turnTo(1,3);
				LorR();
				goTo(1,3);
				LorR();
				turnTo(3,2);
				LorR();
				goTo(3,2);
				LorR();        
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
	
		double angleTarget = Math.atan(X2go/Y2go)/Math.PI*180;
		if (Y2go < 0) {
			angleTarget += 180;
		}
		double angleDeviation = angleTarget - currentTheta;
	
		if (Math.abs(angleDeviation) < 180) return angleDeviation;
		else if (angleDeviation > 180){
			return angleDeviation-360;
		}
		else {
			return angleDeviation+360;
		}	
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
  
	/**
	 * returns whether vehicle should go left or right base on its position
	 * @return true if right, false if left (not right)
	 */
	private static boolean LorR() {
		
		/**
		 * MATH VOODOO BELOW
		 */
		double x2 = odometer.getXYT()[0]; //current x position
		double y2 = odometer.getXYT()[1]; //current y position
		double theta = odometer.getXYT()[2];
		double t = theta/180*Math.PI; //current theta
		
//		double x = b/Math.tan(t) + a; //x intersect
//		double y = a*Math.atan(t) + b;
		
		double m = Math.atan(t+Math.PI/2); //slope
		
		double x1 = TILE_SIZE*4 - x2; // y
		double y1 = TILE_SIZE*4 - y2; // x
		
		double [] listOfX = new double [4];
		listOfX [0] = y1/m;
		listOfX [1] = -x2;
		listOfX [2] = x1;
		listOfX [3] = -y2/m;
		
		Arrays.sort(listOfX);
		
		double d1 = Math.sqrt(Math.pow(listOfX[1], 2) + Math.pow(listOfX[1]*m, 2));
		double d2 = Math.sqrt(Math.pow(listOfX[2], 2) + Math.pow(listOfX[2]*m, 2));
		
		double [] point = new double [2];
		point [0]=(d1 > d2? listOfX[1] : listOfX[2]);
		point [1]=(d1 > d2? listOfX[1]*m : listOfX[2]*m);
		boolean neg = point[1]<0;
		//System.out.println(point[0] + "," + point[1]);
		double add = 0;
		if (neg) {
			add = Math.PI;
		}
//		System.out.println(t);
//		System.out.println(Math.atan(point[0]/point[1])-t+add);
//		System.out.println((Math.atan(point[0]/point[1])-t+add > 0));
		return Math.atan(point[0]/point[1])-t+add > 0; 
	}
  
}
