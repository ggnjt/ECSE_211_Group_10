package ca.mcgill.ecse211.lab4;

// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.lab4.Resources.*;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class AlignmentDriver implements Runnable {
	// states
	enum SearchingState {
		INIT, // First state
		GAZING_THE_ABYSS, // seeing the wild emptyness
		YWALL, // it thinks it sees the YWALL
		GAP, // the corner between the two walls
		XWALL, // it thinks it sees the XWALL
		FINISHING, FINISHED;
	};

	public static SearchingState state = SearchingState.INIT;

	public AlignmentDriver() {
	}

	/**
	 * Stop the robot
	 */
	private static void stopTheRobot() {
		leftMotor.stop(true);
		rightMotor.stop(false);
	}

	/**
	 * set Speed
	 */
	private static void setSpeed(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}

	/**
	 * rotate counter clock wise
	 * 
	 * @param value some rotation
	 */
	private static void rotateCounterClockWiseNonBLocking() {
		leftMotor.backward();
		rightMotor.forward();
	}

	@Override
	public void run() {
		setSpeed(ROTATE_SPEED);
		//start by rotating the robot counterclock wise
		rotateCounterClockWiseNonBLocking();
		int spaceCounter = 0; //buffer for counting derivative jumps
		int der = 0; //used to store derivative
		int prev = 500; //used to store the previous value
		boolean cont = true; //boolean used to break our of while loop
		while (cont) {
			int reading = usPoller.getDistance();
			if (reading == -1)
				continue;

			// logic
			switch (state) {
			case INIT: 
			//initial stage, filling up the median filter and turning until the reading from US sensor becomes very large
				if (reading > TILE_SIZE * 5.0) {
					spaceCounter++;
				} else
					spaceCounter = 0;
				if (spaceCounter > 3) {
					state = SearchingState.GAZING_THE_ABYSS;
					spaceCounter = 0;
				}
				break;
			case GAZING_THE_ABYSS:
			//when the robot is facing away from the wall
				if (reading < TILE_SIZE * 1.5) {
					spaceCounter++;
				} else
					spaceCounter = 0;
				if (spaceCounter > 2) {
					state = SearchingState.XWALL;
					setSpeed(15); //slows the robot down to get better readings
					spaceCounter = 0;
				}
				break;
			case XWALL:
			//when the robot is facing the x=0 wall
				der = reading - prev;
				prev = reading;
				if (der > 0) {
					spaceCounter++;
				}
				if (spaceCounter > 3) {
					state = SearchingState.GAP;
					prev = 500;
					spaceCounter = 0;
				}
			case GAP: 
			//corner between the two walls
				der = reading - prev;
				prev = reading;
				if (der < 0) {
					spaceCounter++;
				}
				if (spaceCounter > 3) {
					state = SearchingState.YWALL;
					prev = 500;
					spaceCounter = 0;
				}
				break;
			case YWALL:
			//when robot is facing the y=0 wall
				der = reading - prev;
				prev = reading;
				if (der > 0 || spaceCounter > 80) {
					
					stopTheRobot();
					//===========================================
					setSpeed(80);			//These steps are used
				//	leftMotor.forward();	//to ensure alignment
				//	rightMotor.forward();	//by ramming the wall
					//===========================================
					if (prev > 9) {
						leftMotor.rotate(convertDistance(prev-9), true);
						rightMotor.rotate(convertDistance(prev-9), false);
					}
					
					spaceCounter = 0;
					state = SearchingState.FINISHED;
				} else if (der == 0) {
					spaceCounter++;
				}
				break;
			case FINISHING:
				if (spaceCounter < 150) {
					spaceCounter++;
				} else {
					leftMotor.rotate(convertDistance(-8.0), true);
					rightMotor.rotate(convertDistance(-8.0), false); 
					//backing up from the wall
					state = SearchingState.FINISHED;
				}
				break;
			case FINISHED:
				stopTheRobot();
				cont = false;
				break;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		leftMotor.rotate(convertAngle(180.0), true);
		rightMotor.rotate(-convertAngle(180.0), false);
		//after the robot is settled, rotates 180 degrees to face the positive y direction
	}

	private static int convertDistance(double distance) { // always positive
		return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
	}

	private static int convertAngle(double angle) { // can be negative
		return convertDistance(Math.PI * TRACK * angle / 360.0);
	}
}
