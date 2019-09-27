package ca.mcgill.ecse211.lab3;

import static ca.mcgill.ecse211.lab3.Resources.*;

/**
 * Controller that controls the robot's movements based on ultrasonic data.
 */
public class UltrasonicController {

  int distance;
  
  int filterControl;
  
  int prev;
  
  private static final int DELTA_SPEED = 40; //default motor speed modifier
  private static final int MAX_SPEED = 290; //maximum motor speed, forward or back
  private static final int MIN_SPEED = 95; //minimum motor speed
  private static final double POWER = 0.33; //speed modifier constant
  /**
	 * the buffers are used to lock the vehicle in a mode. Every sampling cycle the buffer does up by one until is reaches a limit
	 */
  private static final int MOVING_BUFFER_COUNT_LIMIT = 40; //(limit/sampleing rate) = time it takes to reach limit
  private static final int GAP_BUFFER_COUNT_LIMIT = 23; //limit for how long vehicle is allow to be in a gap
  private static int GAP_BUFFER_COUNT = 0;
  private static int MOVING_BUFFER_COUNT = 0;
  
  /**
   * Perform an action based on the US data input.
   * 
   * @param distance the distance to the wall in cm
   */
  public void processUSData(int distance) {
	    filter(distance);
	    
	    int distError = BAND_CENTER - readUSDistance();
	    
	    if (Math.abs(distError) <= BAND_WIDTH) { //straight
			leftMotor.setSpeed(TURN_SPEED + 5);
			rightMotor.setSpeed(TURN_SPEED - 5); //again, the vehicle slowly drifts away from the wall to help with collision and prevent corner gap cases
			leftMotor.forward();
			rightMotor.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0; //resets the GAP buffer so it can detect gaps again
				MOVING_BUFFER_COUNT = 15;
			}
		}
	    
	    //if the vehicle meets a sudden but small change in the distance, it slows down until better reading
		else if (GAP_BUFFER_COUNT < GAP_BUFFER_COUNT_LIMIT  && (Math.abs(distError) <= 40) && MOVING_BUFFER_COUNT < 15) { //gap ignore
			leftMotor.setSpeed(TURN_SPEED - 20);
			rightMotor.setSpeed(TURN_SPEED - 30);
			leftMotor.forward();
			rightMotor.forward();
			GAP_BUFFER_COUNT++;
			if (GAP_BUFFER_COUNT > GAP_BUFFER_COUNT_LIMIT || readUSDistance() < 24) { 
			//the cycle breaks if the distance is too close to a wall or it has moved along for enough time
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else {
			boolean LB = false; //booleans to decide whether to go backward
			boolean RB = false;

			int d = (int)(getGain(distError, BAND_CENTER)*DELTA_SPEED*POWER);
			
			if (d == Integer.MAX_VALUE) { //prevent int overflow
				d = MAX_SPEED;
			} else if (d == Integer.MIN_VALUE){
				d = -MAX_SPEED;
			}

			int LS = TURN_SPEED + d;
			if (LS < 0) { 
				LB = true; //set left motor backwards
			}
			if (LS < MIN_SPEED) {
				LS = MIN_SPEED; //limiting min speed
			}
			else if (LS > MAX_SPEED) {
				LS = MAX_SPEED; //limiting max speed
			}
			
			int RS = TURN_SPEED - d;
			if (RS  < 0) {
				RB = true; //set right motor backwards
			}
			if (RS < MIN_SPEED) {
				RS = MIN_SPEED; //limiting min speed
			}
			else if (RS > MAX_SPEED) {
				RS = MAX_SPEED; //limiting max speed
			}
			
			leftMotor.setSpeed(LS);
			rightMotor.setSpeed(RS);
			if (LB) {
				leftMotor.backward();
			}
			else {
				leftMotor.forward();
			}
			if (RB) {
				rightMotor.backward();
			}
			else {
				rightMotor.forward();
			}

			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 15;
			}
		}
  }
  
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
  
  /**
   * This method calculates a ratio to modify the motor speed modifier
   * @param error error calculated by the distance from the band center +ve means too close, -ve means too far
   * @param band constant band_center
   * @return returns a gain multiplier based on the error
   */
  private double getGain(int error, int band) {
	  double result = (float)band - (float)error / (float)band;
	  if (error > 0) {
		  return result * Math.sqrt(result); //much larger gain for right turns, allowing the robot to turn in place
	  }
	  else return -Math.sqrt(result) * 1.4; // the sqrt allows for a smoother left turn because the sensor is on the left
	  //return result;
  }

  public int readUSDistance() {
    return this.distance;
  }
  
}
