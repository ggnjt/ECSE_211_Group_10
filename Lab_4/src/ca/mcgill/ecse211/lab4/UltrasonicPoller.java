package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;

import java.util.Arrays;

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
  private static final short BUFFER_SIZE = 21;
  private int [] filterBuffer = new int [BUFFER_SIZE];
  public static boolean kill = false;
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
    int count = 0;

    while (true) {
      if (kill) break;
      US_SENSOR.getDistanceMode().fetchSample(usData, 0); // acquire distance data in meters
      reading = (int) (usData[0] * 100.0); // extract from buffer, convert to cm, cast to int

      if (count < BUFFER_SIZE) {
        filterBuffer[count] = reading;
        distance = -1;
        count++;
      } else { // median filter
        shiftArray(filterBuffer, reading);
        int[] sample = filterBuffer.clone();
        Arrays.sort(sample);
        distance = sample[BUFFER_SIZE / 2];
      }
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }

  void shiftArray(int[] arr, int newI) {
    int size = arr.length;
    for (int i = 0; i < size - 1; i++) {
      arr[i] = arr[i + 1];
    }
    arr[size - 1] = newI;
  }

  public int getDistance() {
    return this.distance;
  }

}
