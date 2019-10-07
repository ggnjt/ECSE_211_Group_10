package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.LCD;
import static ca.mcgill.ecse211.lab4.Resources.oc;
import static ca.mcgill.ecse211.lab4.Resources.odometer;
import java.text.DecimalFormat;

public class Display implements Runnable {

  private double[] position;
  private final long DISPLAY_PERIOD = 450;
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

      LCD.drawString(oc.currentState.toString(), 0, 3);
      LCD.drawString(ColorReader.getSample() + "", 0, 4);

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
