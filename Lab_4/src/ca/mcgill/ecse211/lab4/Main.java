package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.hardware.Button;

public class Main {

  public static void main(String[] args) {
    // start first part
    Thread a = new Thread(usPoller);
    Thread b = new Thread(alignmentDriver);
    Thread c = new Thread(new AlignmentDriverDisplay());
    a.start();
    b.start();
    c.start();
    //wait for a button
    Button.waitForAnyPress();
    // kill first part
    a.stop();
    b.stop();
    c.stop();

    // start second part
    new Thread(odometer).start();
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
