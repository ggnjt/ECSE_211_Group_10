package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.LCD;
import static ca.mcgill.ecse211.lab4.Resources.usPoller;
import static ca.mcgill.ecse211.lab4.Resources.alignmentDriver;


public class AlignmentDriverDisplay implements Runnable {

  private final long DISPLAY_PERIOD = 550; //display refresh rate
  public static boolean kill = false; //kill switch for the thread
  private long timeout = Long.MAX_VALUE;

  public void run() {
    LCD.clear();
    long updateStart, updateEnd;
    long tStart = System.currentTimeMillis();
    do {
      if(kill) break;
      LCD.clear();
      updateStart = System.currentTimeMillis();
      LCD.drawString(alignmentDriver.state.toString(), 0, 0);
      LCD.drawString("" + usPoller.getDistance(), 0, 1);
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
