package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class BangBangController extends UltrasonicController {
	
	//private static int DISTANCE_BUFFER; //used to store the previous distance
	private static int GAP_BUFFER_COUNT = 0;
	private static int MOVING_BUFFER_COUNT = 0;
	
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
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= 120) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (GAP_BUFFER_COUNT < 60  && (distError <= 40)) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			GAP_BUFFER_COUNT++;
			if (GAP_BUFFER_COUNT > 61 || readUSDistance() < 12) {
				GAP_BUFFER_COUNT = 62;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0 && readUSDistance() < 10) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 30);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW - 50);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= 120) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 15);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW - 30);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= 120) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError < 0 && readUSDistance() > 75){
			LEFT_MOTOR.setSpeed(MOTOR_LOW - 50);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH + 30);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= 120) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else{
			LEFT_MOTOR.setSpeed(MOTOR_LOW);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= 120) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		
		//System.out.println("Buffer: " + BUFFER_COUNT);
    
	}

	@Override
	public int readUSDistance() {
		return this.distance;
		
	}
}
