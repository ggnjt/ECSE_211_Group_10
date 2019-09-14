package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class PController extends UltrasonicController {

  private static final int MOTOR_SPEED = 160;
  private static final int DELTA_SPEED = 50;
  private static final int MAX_SPEED = 240;
  private static final int MIN_SPEED = 5;
  private static int GAP_BUFFER_COUNT = 0;
  private static int MOVING_BUFFER_COUNT = 0;
  private static final int MOVING_BUFFER_COUNT_LIMIT = 120;
  private static final double POWER = 6.0;
  	

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
    
    if (Math.abs(distError) <= BAND_WIDTH) {
		LEFT_MOTOR.setSpeed(MOTOR_SPEED);
		RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
		MOVING_BUFFER_COUNT++;
		if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
			GAP_BUFFER_COUNT = 0;
			MOVING_BUFFER_COUNT = 0;
		}
	}
	else if (GAP_BUFFER_COUNT < 60  && (distError <= 40)) {
		LEFT_MOTOR.setSpeed(MOTOR_SPEED);
		RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
		GAP_BUFFER_COUNT++;
		if (GAP_BUFFER_COUNT > 61 || readUSDistance() < 12) {
			GAP_BUFFER_COUNT = 62;
			MOVING_BUFFER_COUNT = 0;
		}
	}
	else {
		int d = (int)(getGain(distError, BAND_CENTER)*DELTA_SPEED*POWER);
		
		if (d == Integer.MAX_VALUE) {
			d = 10000;
		} else if (d == Integer.MIN_VALUE){
			d = -10000;
		}

		int LS = MOTOR_SPEED + d;
		if (LS < MIN_SPEED) {
			LS = MIN_SPEED;
		}
		else if (LS > MAX_SPEED) {
			LS = MAX_SPEED;
		}
		int RS = MOTOR_SPEED - d;
		if (RS < MIN_SPEED) {
			RS = MIN_SPEED;
		}
		else if (RS > MAX_SPEED) {
			RS = MAX_SPEED;
		}
		
		LEFT_MOTOR.setSpeed(LS);
		RIGHT_MOTOR.setSpeed(RS);
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
		MOVING_BUFFER_COUNT++;
		if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
			GAP_BUFFER_COUNT = 0;
			MOVING_BUFFER_COUNT = 0;
		}
	}
  }

  private double getGain(int error, int band) {
	  double result = (float)error / (float)band;
	  return result*(Math.abs(result));
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
