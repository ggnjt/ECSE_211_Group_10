package ca.mcgill.ecse211.lab3;

import static ca.mcgill.ecse211.lab3.Resources.*;

/**
 * Samples the US sensor and invokes the selected controller on each cycle.
 * 
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while loop at the bottom
 * executes in a loop. Assuming that the us.fetchSample, and cont.processUSData methods operate in about 20ms, and that
 * the thread sleeps for 50 ms at the end of each loop, then one cycle through the loop is approximately 70 ms. This
 * corresponds to a sampling rate of 1/70ms or about 14 Hz.
 */
public class UltrasonicPoller implements Runnable {
  private int distance;
  private float[] usData;


  public UltrasonicPoller() {
    usData = new float[US_SENSOR.sampleSize()];
  }

  /*
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  public void run() {
    int reading;
    while (true) {
      US_SENSOR.getDistanceMode().fetchSample(usData, 0); // acquire distance data in meters
      reading = (int) (usData[0] * 100.0); // extract from buffer, convert to cm, cast to int
      distance = reading;
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }

  public int getDistance() {
    return this.distance;
  }

}
