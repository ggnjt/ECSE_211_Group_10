package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.hardware.Button;

public class Main {

  public static void main(String[] args) {
    odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
    new Thread(odometer).start();
    //new Thread(usPoller).start();
    new Thread(display).start();

    new Thread(oc).start();
    while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
    }

    System.exit(0);
  }


  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // derp
    }
  }
}
