package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class BangBangController extends UltrasonicController {
	
	/**
	 * the buffers are used to lock the vehicle in a mode. Every sampling cycle the buffer does up by one until is reaches a limit
	 */
	private static int GAP_BUFFER_COUNT = 0;
	private static int MOVING_BUFFER_COUNT = 0;
	private static final int MOVING_BUFFER_COUNT_LIMIT = 60;
	private static final int GAP_BUFFER_COUNT_LIMIT = 30;
	
	public BangBangController() {
		LEFT_MOTOR.setSpeed(MOTOR_HIGH); // Start robot moving forward
	    RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
	    LEFT_MOTOR.forward();
	    RIGHT_MOTOR.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		
		filter(distance);
		
		int distError = BAND_CENTER - readUSDistance();		

		

		if (Math.abs(distError) <= BAND_WIDTH) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH );
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH - 15); 
			//moves the vehicle away from the wall by default.
			//this helps with right corners with a gap as well
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 40;
			}
		}
		else if (GAP_BUFFER_COUNT < GAP_BUFFER_COUNT_LIMIT  && (Math.abs(distError) <= 40)  && MOVING_BUFFER_COUNT > 40) { //ignore gap
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 5);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH - 5);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			GAP_BUFFER_COUNT++;
			if (GAP_BUFFER_COUNT >= GAP_BUFFER_COUNT_LIMIT || readUSDistance() < 20 || MOVING_BUFFER_COUNT > MOVING_BUFFER_COUNT_LIMIT) {
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0 && readUSDistance() < 23) {  //sharp right turn
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 70);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW + 60);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.backward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0) { //right turn
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 20);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW - 50);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError < 0 && readUSDistance() > 75){ //sharp left turn
			LEFT_MOTOR.setSpeed(MOTOR_LOW  + 10);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH + 40);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else{ //low speed left turn
			LEFT_MOTOR.setSpeed(MOTOR_LOW - 10);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}   
	}

	@Override
	public int readUSDistance() {
		return this.distance;
		
	}
}
