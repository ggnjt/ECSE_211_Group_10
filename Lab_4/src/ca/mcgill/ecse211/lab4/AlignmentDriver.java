package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.leftMotor;
import static ca.mcgill.ecse211.lab4.Resources.rightMotor;
// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab4.Resources.*;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class AlignmentDriver implements Runnable {
  // states
  enum SearchingState {
    INIT, // First state
    BIG_EMPTY_SPACE, // seeing the wild emptyness
    YWALL, // it thinks it sees the YWALL
    GAP, // the corner between the two walls
    XWALL, // it thinks it sees the XWALL
    FINISHED;
  };

  public static SearchingState state = SearchingState.INIT;

  public AlignmentDriver() {}

  /**
   * Stop the robot
   */
  private static void stopTheRobot() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * set Speed
   */
  private static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * rotate counter clock wise
   * 
   * @param value some rotation
   */
  private static void rotateCounterClockWiseNonBLocking() {
    leftMotor.backward();
    rightMotor.forward();
  }

  @Override
  public void run() {
    setSpeed(ROTATE_SPEED);
    rotateCounterClockWiseNonBLocking();
    int spaceCounter = 0;
    int movingAverage = 0;
    while (true) {
      int reading = usPoller.getDistance();
      if (reading == -1)
        continue;

      // logic
      switch (state) {
        case INIT:
          if (reading > TILE_SIZE * 10.0) {
            spaceCounter++;
          }
          if (spaceCounter > 20) {
            state = SearchingState.BIG_EMPTY_SPACE;
            spaceCounter = 0;
          }
          break;
        case BIG_EMPTY_SPACE:
          if (reading < TILE_SIZE) {
            spaceCounter++;
          }

          if (spaceCounter > 4) {
            state = SearchingState.XWALL;
            spaceCounter = 0;
          }
          break;
        case XWALL:
          if (reading > TILE_SIZE / 3.0) {
            spaceCounter++;
          }
          if (spaceCounter > 2) {
            state = SearchingState.GAP;
            spaceCounter = 0;
          }
          break;
        case GAP:
          if (reading < TILE_SIZE / 2.0) {
            spaceCounter++;
          }
          if (spaceCounter > 3) {
            state = SearchingState.YWALL;
            spaceCounter = 0;
          }
          break;
        case YWALL:
          state = SearchingState.FINISHED;
          break;
        case FINISHED:
          stopTheRobot();
          break;
      }

      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
