package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class PController extends UltrasonicController {

  private static final int MOTOR_SPEED = 180; //default motor speed
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
  
  public PController() {
    LEFT_MOTOR.setSpeed(MOTOR_SPEED); // Initialize motor rolling forward
    RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  @Override
  public void processUSData(int distance) {
    filter(distance);
    
    int distError = BAND_CENTER - readUSDistance();
    
    if (Math.abs(distError) <= BAND_WIDTH) { //straight
		LEFT_MOTOR.setSpeed(MOTOR_SPEED + 5);
		RIGHT_MOTOR.setSpeed(MOTOR_SPEED - 5); //again, the vehicle slowly drifts away from the wall to help with collision and prevent corner gap cases
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
		MOVING_BUFFER_COUNT++;
		if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
			GAP_BUFFER_COUNT = 0; //resets the GAP buffer so it can detect gaps again
			MOVING_BUFFER_COUNT = 15;
		}
	}
    
    //if the vehicle meets a sudden but small change in the distance, it slows down until better reading
	else if (GAP_BUFFER_COUNT < GAP_BUFFER_COUNT_LIMIT  && (Math.abs(distError) <= 40) && MOVING_BUFFER_COUNT < 15) { //gap ignore
		LEFT_MOTOR.setSpeed(MOTOR_SPEED - 20);
		RIGHT_MOTOR.setSpeed(MOTOR_SPEED - 30);
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
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

		int LS = MOTOR_SPEED + d;
		if (LS < 0) { 
			LB = true; //set left motor backwards
		}
		if (LS < MIN_SPEED) {
			LS = MIN_SPEED; //limiting min speed
		}
		else if (LS > MAX_SPEED) {
			LS = MAX_SPEED; //limiting max speed
		}
		
		int RS = MOTOR_SPEED - d;
		if (RS  < 0) {
			RB = true; //set right motor backwards
		}
		if (RS < MIN_SPEED) {
			RS = MIN_SPEED; //limiting min speed
		}
		else if (RS > MAX_SPEED) {
			RS = MAX_SPEED; //limiting max speed
		}
		
		LEFT_MOTOR.setSpeed(LS);
		RIGHT_MOTOR.setSpeed(RS);
		if (LB) {
			LEFT_MOTOR.backward();
		}
		else {
			LEFT_MOTOR.forward();
		}
		if (RB) {
			RIGHT_MOTOR.backward();
		}
		else {
			RIGHT_MOTOR.forward();
		}

		MOVING_BUFFER_COUNT++;
		if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
			GAP_BUFFER_COUNT = 0;
			MOVING_BUFFER_COUNT = 15;
		}
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

  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
