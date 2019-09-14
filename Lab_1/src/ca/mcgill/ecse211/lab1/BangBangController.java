package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class BangBangController extends UltrasonicController {
	
	//private static int DISTANCE_BUFFER; //used to store the previous distance
	private static int GAP_BUFFER_COUNT = 0;
	private static int MOVING_BUFFER_COUNT = 0;
	private static final int MOVING_BUFFER_COUNT_LIMIT = 80;
	private static final int GAP_BUFFER_COUNT_LIMIT = 23;
	//private static int BACKWARD_BUFFER = 30;
	
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

		
//		if ((readUSDistance() <= 10 || BACKWARD_BUFFER < 20) && MOVING_BUFFER_COUNT < 80) {
//			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
//			RIGHT_MOTOR.setSpeed(MOTOR_HIGH-30);
//			LEFT_MOTOR.backward();
//			RIGHT_MOTOR.backward();
//			BACKWARD_BUFFER--;
//			if (BACKWARD_BUFFER == 0) {
//				BACKWARD_BUFFER = 20;
//			}
//		}
		if (Math.abs(distError) <= BAND_WIDTH) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (GAP_BUFFER_COUNT < GAP_BUFFER_COUNT_LIMIT  && (distError <= 40)) { //ignore gap
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			GAP_BUFFER_COUNT++;
			if (GAP_BUFFER_COUNT >= GAP_BUFFER_COUNT_LIMIT || readUSDistance() < 12) {
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0 && readUSDistance() < 10) {  //sharp right turn
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 30);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.backward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else if (distError > 0) { //right turn
			LEFT_MOTOR.setSpeed(MOTOR_HIGH + 15);
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
			LEFT_MOTOR.setSpeed(MOTOR_LOW - 30);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH + 30);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		else{ //low speed left turn
			LEFT_MOTOR.setSpeed(MOTOR_LOW);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			MOVING_BUFFER_COUNT++;
			if (MOVING_BUFFER_COUNT >= MOVING_BUFFER_COUNT_LIMIT) {
				GAP_BUFFER_COUNT = 0;
				MOVING_BUFFER_COUNT = 0;
			}
		}
		
		System.out.println(GAP_BUFFER_COUNT);
    
	}

	@Override
	public int readUSDistance() {
		return this.distance;
		
	}
}
