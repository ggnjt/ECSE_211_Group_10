package ca.mcgill.ecse211.lab1;

import static ca.mcgill.ecse211.lab1.Resources.*;

public class BangBangController extends UltrasonicController {
	
	private static int DISTANCE_BUFFER; //used to store the previous distance
	//private static int BUFFER_COUNTER = 0;
	
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
		}
		else if (readUSDistance() - DISTANCE_BUFFER <= 70) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
		}
		else if (distError > 0) {
			LEFT_MOTOR.setSpeed(MOTOR_HIGH);
			RIGHT_MOTOR.setSpeed(MOTOR_LOW);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
		}
		else {
			LEFT_MOTOR.setSpeed(MOTOR_LOW);
			RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
		}
		
		DISTANCE_BUFFER = this.distance;
    
	}

	@Override
	public int readUSDistance() {
		return this.distance;
		
	}
}
