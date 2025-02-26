package ca.mcgill.ecse211.lab3;

import static ca.mcgill.ecse211.lab3.Resources.LCD;
import static ca.mcgill.ecse211.lab3.Resources.odometer;
import static ca.mcgill.ecse211.lab3.Resources.robotDriver;
import static ca.mcgill.ecse211.lab3.Resources.usPoller;
import java.text.DecimalFormat;

public class Display implements Runnable {

  private double[] position;
  private final long DISPLAY_PERIOD = 550;
  private long timeout = Long.MAX_VALUE;

  public void run() {
    LCD.clear();

    long updateStart, updateEnd;

    long tStart = System.currentTimeMillis();
    do {
      LCD.clear();
      updateStart = System.currentTimeMillis();
      
      // Retrieve x, y and Theta information
      position = odometer.getXYT();

      // Print x,y, and theta information
      DecimalFormat numberFormat = new DecimalFormat("######0.00");
      LCD.drawString("X: " + numberFormat.format(position[0]), 0, 0);
      LCD.drawString("Y: " + numberFormat.format(position[1]), 0, 1);
      LCD.drawString("T: " + numberFormat.format(position[2]), 0, 2);

      // Print the current state and the waypoint
      LCD.drawString(robotDriver.state.toString() + ":" + robotDriver.wayPointIndex, 0, 3);
      
      // Print the uspoller info
      LCD.drawString("Distance: " + usPoller.getDistance(), 0, 4);
      
      // this ensures that the data is updated only once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < DISPLAY_PERIOD) {
        try {
          Thread.sleep(DISPLAY_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while ((updateEnd - tStart) <= timeout);

  }

  /**
   * Sets the timeout in ms.
   * 
   * @param timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Shows the text on the LCD, line by line.
   * 
   * @param strings comma-separated list of strings, one per line
   */
  public static void showText(String... strings) {
    LCD.clear();
    for (int i = 0; i < strings.length; i++) {
      LCD.drawString(strings[i], 0, i);
    }
  }

}
