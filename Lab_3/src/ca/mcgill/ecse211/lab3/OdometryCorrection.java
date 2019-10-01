package ca.mcgill.ecse211.lab3;
//
//import static ca.mcgill.ecse211.lab3.Resources.*;
//import java.util.Arrays;
//import lejos.hardware.Sound;
//import lejos.robotics.SampleProvider;

public class OdometryCorrection{ //implements Runnable {
//	private static final long CORRECTION_PERIOD = 10;
//	private static final double SENSOR_CENTER_CORRECTION = 4.5; //distance form the sensor to the center of the vehicle (cm)
//	private static final double CRITICAL_DISTANCE  = 5.0;
//	// sensor
//	private SampleProvider sampleProvider = colorSensor.getRedMode();
//	private float[] sampleColor = new float[colorSensor.sampleSize()];
//  
//	/*
//	 * Here is where the odometer correction code should be run.
//	 */
//	public void run() {
//		long correctionStart, correctionEnd;
//    
//    
//		while (true) {
//			correctionStart = System.currentTimeMillis();
//			sampleProvider.fetchSample(sampleColor, 0);
//			if(sampleColor[0]  <  0.12) {
//				Sound.beep();
//       
//       
//				Main.sleepFor(1500); //sleeps the thread to avoid reading the same black line more than once
//			}
//
//			// this ensures the odometry correction occurs only once every period
//			correctionEnd = System.currentTimeMillis();
//			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
//				Main.sleepFor(CORRECTION_PERIOD - (correctionEnd - correctionStart));				}	
//		}
//	}
//	
//	/**
//	 * returns the pair of points made by the trajectory of the robot 
//	 * and the 2 lines of the square it is in which it will intersect with
//	 * +ve values for x, -ve values for y
//	 * @return 2 values of the x/y values of the lines
//	 */
//	public int[] getCollisionPoints() {
//		int [] result = new int [2];
//		int [] currentSquare = new int [2];
//		
//		currentSquare [0] = (int)(odometer.getXYT()[0]/TILE_SIZE);
//		currentSquare [1] = (int)(odometer.getXYT()[1]/TILE_SIZE);
//		
//		double x2 = odometer.getXYT()[0]%TILE_SIZE; //current x within square
//		double y2 = odometer.getXYT()[1]%TILE_SIZE; //current y within square
//		double x1 = TILE_SIZE-x2;
//		double y1 = TILE_SIZE-y2;
//		
//		double t = odometer.getXYT()[2] / 180 * Math.PI; // current theta 
//		double m = Math.tan(t); // (x/y), slope
//		boolean tIsPos = t > 0;
//		
//		double[] p1 = new double[2];
//		double[] p2 = new double[2];
//		
//		if (tIsPos) {
//			p1[0] = x1;
//			p1[1] = x1*m;
//			p2[0] = y1/m;
//			p2[1] = y1;
//		} else {
//			p1[0] = -x2;
//			p1[1] = -x2*m;
//			p2[0] = -y2/m;
//			p2[1] = -y2;
//		}
//
//		double distance = Math.sqrt((p1[0] - p2[0])*(p1[0] - p2[0])+(p1[1] - p2[1])*(p1[1] - p2[1]));
//		boolean turn = false;
//		
//		if (distance < CRITICAL_DISTANCE) {
//			turn = true;
//		}
//		
//		
//		return result;
//	}
}
