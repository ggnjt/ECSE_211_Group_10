package ca.mcgill.ecse211.lab3;

import static ca.mcgill.ecse211.lab3.Resources.*;
import ca.mcgill.ecse211.lab3.Display;
import ca.mcgill.ecse211.lab3.RobotDriver;
import lejos.hardware.Button;

public class Main {

  public static void main(String[] args) {
    Display.showText("Press any button");
    Button.waitForAnyPress();
    new Thread(odometer).start();
    odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
   
    RobotDriver.drive();

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
