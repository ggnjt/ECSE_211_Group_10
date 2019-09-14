package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class PController extends UltrasonicController {

  private static final int MOTOR_SPEED = 160;
  private static final int DELTA_SPEED = 50;
  private static final int MAX_SPEED = 290;
  private static final int MIN_SPEED = 50;
  private static final int MOVING_BUFFER_COUNT_LIMIT = 45;
  private static final double POWER = 3.3;
  private static final int GAP_BUFFER_COUNT_LIMIT = 23;
  //private static int BACKWARD_BUFFER = 30;
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
    
//    if ((readUSDistance() <= 10 || BACKWARD_BUFFER < 20) && MOVING_BUFFER_COUNT < 80) {
//		LEFT_MOTOR.setSpeed(MOTOR_HIGH);
//		RIGHT_MOTOR.setSpeed(MOTOR_HIGH-30);
//		LEFT_MOTOR.backward();
//		RIGHT_MOTOR.backward();
//		BACKWARD_BUFFER--;
//		if (BACKWARD_BUFFER == 0) {
//			BACKWARD_BUFFER = 20;
//		}
//	}
    if (Math.abs(distError) <= BAND_WIDTH) { //straight
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
	else if (GAP_BUFFER_COUNT < GAP_BUFFER_COUNT_LIMIT  && (distError <= 40)) { //gap ignore
		LEFT_MOTOR.setSpeed(MOTOR_SPEED);
		RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
		LEFT_MOTOR.forward();
		RIGHT_MOTOR.forward();
		GAP_BUFFER_COUNT++;
		if (GAP_BUFFER_COUNT > GAP_BUFFER_COUNT_LIMIT || readUSDistance() < 16) {
			MOVING_BUFFER_COUNT = 0;
		}
	}
	else {
		boolean LB = false;
		boolean RB = false;
		int d = (int)(getGain(distError, BAND_CENTER)*DELTA_SPEED*POWER);
		
		if (d == Integer.MAX_VALUE) {
			d = 10000;
		} else if (d == Integer.MIN_VALUE){
			d = -10000;
		}

		int LS = MOTOR_SPEED + d;
		if (LS < 0) {
			LB = true;
		}
		if (LS < MIN_SPEED) {
			LS = MIN_SPEED;
		}
		else if (LS > MAX_SPEED) {
			LS = MAX_SPEED;
		}
		
		int RS = MOTOR_SPEED - d;
		if (RS - 40 < 0) {
			RB = true;
		}
		if (RS < MIN_SPEED) {
			RS = MIN_SPEED;
		}
		else if (RS > MAX_SPEED) {
			RS = MAX_SPEED;
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
			MOVING_BUFFER_COUNT = 0;
		}
	}
    System.out.println(GAP_BUFFER_COUNT);
  }

  private double getGain(int error, int band) {
	  double result = (float)error / (float)band;
	  if (result < 0) {
		  return result;
	  }
	  else return result*(Math.abs(result));
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
