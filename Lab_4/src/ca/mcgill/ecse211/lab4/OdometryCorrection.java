package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;

	// distance form the sensor to the center of the vehicle (cm)
	static final double SENSOR_CENTER_CORRECTION = 12.2;

	// states
	enum WorkingState {
		TURN_BACK,
		//state seeking y=1 black line with unknown theta and aligning with that line
		SEEK_Y,
		// state aligning to line y=1 (theta = 90) with unknow x
		ALIGN_X,
		// state turning to theta = 180, marching forward, turning to theta = 270
		// backing up until x=1 line
		SEEK_X,
		// finish
		FINISHED;
	};

	WorkingState currentState = WorkingState.TURN_BACK;

	// sensor
	private SampleProvider sampleProvider = colorSensor.getRedMode();
	private float[] sampleColor = new float[colorSensor.sampleSize()];
	float prev;
	float derivative;

	/*
	 * Here is where the odometer correction code should be run.
	 */
	public void run() {
		long correctionStart, correctionEnd;
		while (true) {
			correctionStart = System.currentTimeMillis();
			sampleProvider.fetchSample(sampleColor, 0);
			prev = sampleColor[0];
			switch (currentState) {
			//First state
			case TURN_BACK:
				setSpeed(ROTATE_SPEED);
				// Turn back so the color sensor points towards positive y direction
				leftMotor.rotate(convertAngle(180.0), true);
				rightMotor.rotate(convertAngle(-180.0), false);
				stopTheRobot();
				// Start moving backward to meet the y=1 line
				leftMotor.backward();
				rightMotor.backward();
				currentState = WorkingState.SEEK_Y;
				break;
			//second state
			case SEEK_Y:
				if (ColorReader.detectBlackLine()) {
					//reset
					stopTheRobot();
					Sound.beep();
					odometer.setY(TILE_SIZE - SENSOR_CENTER_CORRECTION);
					//move further back until the wheels align with the line
					leftMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
					rightMotor.rotate(convertDistance(-OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
					//turn counterclock-wise until the color sensor detect the y=1 line, 
					//at which point the robot is parallel with the x axis
					leftMotor.backward();
					rightMotor.forward();
					currentState = WorkingState.ALIGN_X;
					try {
						Thread.sleep(700);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // sleeps the thread to avoid reading the same black line more than once
				}
				break;
			//third state
			case ALIGN_X:
				if (ColorReader.detectBlackLine()) {
					stopTheRobot();
					Sound.beep();
					odometer.setTheta(90.0);
					//turn 90 degree counterclock-wise so the robot faces the positive direction
					leftMotor.rotate(convertAngle(-90.0), true);
					rightMotor.rotate(convertAngle(90.0), false);
					//move 5cm back to avoid reading the y=1 line when aligning with the x=1 line
					leftMotor.rotate(convertDistance(-5.0), true);
					rightMotor.rotate(convertDistance(-5.0), false);
					//turn back facing the positive x direction
					//NB: the robot does not turn clockwise here to avoid the color sensor reading a black line while turning 
					//or going over the x=1 line
					leftMotor.rotate(convertAngle(90.0), true);
					rightMotor.rotate(convertAngle(-90.0), false);
					//mvoes the robot forward until a black line (y=1) is detected
					leftMotor.forward();
					rightMotor.forward();
					currentState = WorkingState.SEEK_X;
					try {
						Thread.sleep(700);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			//Fourth state
			case SEEK_X:
				if (ColorReader.detectBlackLine()) {
					Sound.beep();
					//reset
					stopTheRobot();
					odometer.setX(TILE_SIZE - SENSOR_CENTER_CORRECTION);
					//back up until the wheels are in line with the x=1 line
					leftMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), true);
					rightMotor.rotate(-convertDistance(OdometryCorrection.SENSOR_CENTER_CORRECTION), false);
					//turn 90 degrees counterclock-wise to face the positive y direction (theta = 0)
					leftMotor.rotate(convertAngle(-90.0), true);
					rightMotor.rotate(convertAngle(90.0), false);
					//recover the distance moved to avoid black lines
					leftMotor.rotate(convertDistance(5.0), true);
					rightMotor.rotate(convertDistance(5.0), false);
					currentState = WorkingState.FINISHED;
					try {
						Thread.sleep(700);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			//final state
			case FINISHED:
				// final stage, robot is stopped at (1,1), and the coordinates are reset
				stopTheRobot();
				odometer.setXYT(TILE_SIZE, TILE_SIZE, 0.0);
				break;
			}
			// this ensures the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));
			}
		}
	}

	/**
	 * Stop the robot
	 */
	private static void stopTheRobot() {
		leftMotor.stop(true);
		rightMotor.stop(false);
	}

	/**
	 * Converts input distance to the total rotation of each wheel needed to cover
	 * that distance.
	 * 
	 * @param distance
	 * @return the wheel rotations necessary to cover the distance
	 */
	private static int convertDistance(double distance) { // always positive
		return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
	}

	/**
	 * Converts input angle to the total rotation of each wheel needed to rotate the
	 * robot by that angle.
	 * 
	 * @param angle angle in degrees
	 * @return the wheel rotations necessary to rotate the robot by the angle
	 */
	private static int convertAngle(double angle) { // can be negative
		return convertDistance(Math.PI * TRACK * angle / 360.0);
	}

	/**
	 * set Speed
	 */
	private static void setSpeed(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}
}
