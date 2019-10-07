package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.robotics.SampleProvider;

public class ColorReader implements Runnable {

	private static boolean isFalling;
	private static final long SENSORTIMERLIMIT = 50;
	
	public ColorReader(boolean fal) {
		isFalling = fal;
	}

	// sensor
	private static SampleProvider sampleProvider = colorSensor.getRedMode();
	private static float[] sampleColor = new float[colorSensor.sampleSize()];
	private static float sample;
	private static float der = 0;
	private static float prev = 0;
	private static int counter = 0;

	public void run() {
		long readingStart, readingEnd;

		while(true) {
			readingStart = System.currentTimeMillis();
			sampleProvider.fetchSample(sampleColor, 0);
			sample = sampleColor[0];
			der = sample - prev;
			prev = sample;
			// this ensures reading
			readingEnd = System.currentTimeMillis();
			if (readingEnd - readingStart < SENSORTIMERLIMIT) {
				try {
					Thread.sleep(SENSORTIMERLIMIT - (readingEnd - readingStart));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
	}

	public static float getSample() {
		return sample;
	}
	
	public static boolean detectBlackLine() {
		if (isFalling) {
			if (counter > 3) {
				counter = 0;
				return true;
			}
			else if (der < 0 && Math.abs(der) > 0.07) {
				counter++;
				return false;
			} 
			else return false;
		}
		else {
			if (counter > 3) {
				counter = 0;
				return true;
			}
			else if (der > 0 && Math.abs(der) > 0.07) {
				counter++;
				return false;
			} 
			else return false;
		}
	}
}
