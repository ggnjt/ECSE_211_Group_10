package ca.mcgill.ecse211.lab3;

// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab3.Resources.*;
import java.util.Arrays;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class RobotDriver {
	private static final double TILE_RATIO = 1.8;
  public static final int[][] ROUTE = {{1, 3}, {2, 2}, {3, 3}, {3, 2}, {2, 1}};
  //public static final int[][] ROUTE = {{1, 3}, {3, 3}, {3, 1}, {1, 1}};
  
  enum WorkingState {
    /** The initial state. */
    INIT,
    /** The turning state. */
    TURNING,
    /** The traveling state. */
    TRAVELING,
    /** The emergency state. */
    EMERGENCY
  };

  /**
   * The current state of the robot.
   */
  volatile WorkingState state = WorkingState.INIT;
  volatile int wayPointIndex = 0;

  /**
   * Drives the robot in a square of size 3x3 Tiles. It is to be run in parallel with the odometer and odometer
   * correction classes to allow testing their functionality.
   */
  public void drive() {// spawn a new Thread to avoid this method blocking
    (new Thread() {
      public void run() {
        // reset the motors and set the speed
        stopTheRobot();
        setAcceleration(ACCELERATION);
        setSpeed(FORWARD_SPEED);

        // Sleep for 2 seconds
        Main.sleepFor(TIMEOUT_PERIOD);

        // Start Routing
        state = WorkingState.INIT;

        while (wayPointIndex < ROUTE.length) {
          int wayPointX = ROUTE[wayPointIndex][0];
          int wayPointY = ROUTE[wayPointIndex][1];

          switch (state) {
            case INIT:
              // is there more points to reach?
              if (wayPointIndex < ROUTE.length) {
                state = WorkingState.TURNING;
              } else {
                // done arrived at destination and no more way-points
                return;
              }
              break;
            case TURNING:
              turnTo(wayPointX, wayPointY);
              state = WorkingState.TRAVELING;
              break;
            case TRAVELING:
              Thread detectObstacle = new Thread(new Runnable() {
                @Override
                public void run() {
                  while (!Thread.interrupted()) {
                    int distance = usPoller.getDistance();
                  //if the distance detected by the US sensor is less than a set number, enter the EMERGENCY state
                    if (distance < 14) { 
                      stopTheRobot();
                      synchronized (state) {
                        state = WorkingState.EMERGENCY;
                      }
                      break;
                    }
                  }
                }
              });

              detectObstacle.start();

              goTo(wayPointX, wayPointY);
              try {
                Thread.sleep(1000);
              } catch (Exception e) {
              } // Poor man's timed sampling
              synchronized (state) {
                if (state == WorkingState.TRAVELING) {
                  state = WorkingState.INIT;
                  try {
                    detectObstacle.interrupt();
                  } catch (Exception e) {

                  }
                  wayPointIndex++;
                }
              }
              break;
            case EMERGENCY:
              // avoid the obstacle
              int angle = convertAngle(90);
              int distance = convertDistance(TILE_RATIO * TILE_SIZE);

              if (LorR()) {
                // turn right
                leftMotor.rotate(angle, true);
                rightMotor.rotate(-angle, false);
                rightMotor.setSpeed(TURN_SPEED);
                
                leftMotor.rotate(distance, true);
                rightMotor.rotate((int)(distance * (TURN_SPEED * 1.0)/FORWARD_SPEED ), false);
              } else {
                // turn left
                leftMotor.rotate(-angle, true);
                rightMotor.rotate(angle, false);
                leftMotor.setSpeed(TURN_SPEED);

                leftMotor.rotate((int)(distance * (TURN_SPEED * 1.0)/FORWARD_SPEED ), true);
                rightMotor.rotate(distance, false);
              }
              
              
              setSpeed(FORWARD_SPEED);
              state = WorkingState.INIT;
              break;
          }
        }
      }
    }).start();
  }

  /**
   * Stop the robot
   */
  public static void stopTheRobot() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }
  
  /**
   * set acceleration for both motors
   */
  public static void setAcceleration(int acceleration) {
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }
  
  /**
   * set Speed
   */
  public static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) { // always positive
    return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that angle.
   * 
   * @param angle
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) { // can be negative
    return convertDistance(Math.PI * TRACK * angle / 360.0);
  }

  /**
   * calculates the displacement needed to move the the desired coordinates
   * 
   * @return
   */
  public static double calculateDistance(Odometer odo, int waypointX, int waypointY) {

    double currentX = odo.getXYT()[0];
    double currentY = odo.getXYT()[1];
    double Xtarget = waypointX * TILE_SIZE;
    double Ytarget = waypointY * TILE_SIZE;

    double X2go = Xtarget - currentX;
    double Y2go = Ytarget - currentY;

    return Math.sqrt(Math.pow(X2go, 2) + Math.pow(Y2go, 2));
  }

  public static double calculateAngle(Odometer odo, int waypointX, int waypointY) {

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
   * returns whether vehicle should go left or right base on its position
   * 
   * @return true if right, false if left (not right)
   */
  private static boolean LorR() {

    /**
     * MATH VOODOO BELOW
     */
    double x2 = odometer.getXYT()[0]; // current x position
    double y2 = odometer.getXYT()[1]; // current y position
    double theta = odometer.getXYT()[2]; //theta in degrees
    if (theta > 180) {
    	theta -= 360;
    }
    double t = theta / 180 * Math.PI; // current theta in radians
    double m = 1/(Math.tan((Math.PI/2.0) + t)); // slope
    double x1 = TILE_SIZE * 4 - x2; 
    double y1 = TILE_SIZE * 4 - y2;


    double[] listOfX = new double[4];
    listOfX[0] = x1;
    listOfX[1] = -x2;
    listOfX[2] = y1 / m;
    listOfX[3] = -y2 / m;
    Arrays.sort(listOfX);
    double d1 = Math.sqrt(listOfX[1]*listOfX[1] + (listOfX[1] * m)*(listOfX[1] * m));
    double d2 = Math.sqrt(listOfX[2]*listOfX[2] + (listOfX[2] * m)*(listOfX[2] * m));

    double[] point = new double[2];
    point[0] = (d1 > d2 ? listOfX[1] : listOfX[2]);
    point[1] = (d1 > d2 ? listOfX[1] * m : listOfX[2] * m);
    boolean neg = point[1] < 0;

    double add = 0;
    if (neg) {
      add = Math.PI;
    }
    return Math.atan(point[0] / point[1]) - t + add > 0;
  }

}
