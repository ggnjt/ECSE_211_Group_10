package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

/**
 * Controller that controls the robot's movements based on ultrasonic data.
 */
public abstract class UltrasonicController {

  int distance;
  
  int filterControl;
  
  int prev;
  
  /**
   * Perform an action based on the US data input.
   * 
   * @param distance the distance to the wall in cm
   */
  public abstract void processUSData(int distance);

  /**
   * Returns the distance between the US sensor and an obstacle in cm.
   * 
   * @return the distance between the US sensor and an obstacle in cm
   */
  public abstract int readUSDistance();
  
  /**
   * Rudimentary filter - toss out invalid samples corresponding to null signal.
   * @param distance distance in cm
   */
  void filter(int distance) {
	if (distance >= 180 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the filter value
      filterControl++;
      //this.distance = prev;
    } else if (distance >= 180) {
      // Repeated large values, so there is nothing there: leave the distance alone
      this.distance = distance;
      //this.prev = distance;
    } else {
      // distance went below 180: reset filter and leave distance alone.
      filterControl = 0;
      this.distance = distance;
      //this.prev = distance;
    }
  }
  
}
