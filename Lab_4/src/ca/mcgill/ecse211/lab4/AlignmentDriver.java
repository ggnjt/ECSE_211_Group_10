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
		rotateCounterClockWiseNonBLocking();
		int spaceCounter = 0;
		int der = 0;
		int prev = 500;
		boolean cont = true;
		while (cont) {
			int reading = usPoller.getDistance();
			if (reading == -1)
				continue;

			// logic
			switch (state) {
			case INIT:
				if (reading > TILE_SIZE * 5.0) {
					spaceCounter++;
				} else
					spaceCounter = 0;
				if (spaceCounter > 5) {
					state = SearchingState.GAZING_THE_ABYSS;
					spaceCounter = 0;
				}
				break;
			case GAZING_THE_ABYSS:
				if (reading < TILE_SIZE * 1.5) {
					spaceCounter++;
				} else
					spaceCounter = 0;
				if (spaceCounter > 2) {
					state = SearchingState.XWALL;
					setSpeed(15);
					spaceCounter = 0;
				}
				break;
			case XWALL:
				der = reading - prev;
				prev = reading;
				if (der > 0) {
					spaceCounter++;
				}
				if (spaceCounter > 4) {
					state = SearchingState.GAP;
					prev = 500;
					spaceCounter = 0;
				}
			case GAP:
				der = reading - prev;
				prev = reading;
				if (der < 0) {
					spaceCounter++;
				}
				if (spaceCounter > 4) {
					state = SearchingState.YWALL;
					prev = 500;
					spaceCounter = 0;
				}
				break;
			case YWALL:
				der = reading - prev;
				prev = reading;
				if (der > 0 || spaceCounter > 75) {
					state = SearchingState.FINISHING;
					stopTheRobot();
					setSpeed(80);
					leftMotor.forward();
					rightMotor.forward();
					spaceCounter = 0;
				} else if (der == 0) {
					spaceCounter++;
				}
				break;
			case FINISHING:
				if (spaceCounter < 200) {
					spaceCounter++;
				} else {
					state = SearchingState.FINISHED;
					leftMotor.rotate(-180, true);
					rightMotor.rotate(-180, false);
					spaceCounter = 0;
				}
				break;
			case FINISHED:
				if (spaceCounter < 10) {
					spaceCounter++;
				} else
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
	}

	private static int convertDistance(double distance) { // always positive
		return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
	}

	private static int convertAngle(double angle) { // can be negative
		return convertDistance(Math.PI * TRACK * angle / 360.0);
	}
}
