package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Resources.*;
import lejos.hardware.Button;

public class Main {
  public static void main(String[] args) {
    try {
		firstPhase();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    secondPhase();
    System.exit(0);
  }

  @SuppressWarnings("deprecation")
  public static void firstPhase() throws InterruptedException { //First phase when the robot uses the US sensor to align
    Thread a = new Thread(usPoller);
    Thread b = new Thread(alignmentDriver);
    //Thread c = new Thread(new AlignmentDriverDisplay());
    a.start();
    b.start();
   // c.start();
    Button.waitForAnyPress();
    UltrasonicPoller.kill = true;
    AlignmentDriverDisplay.kill = true;
    a.join();
    b.join();
    //c.join();
  }

  public static void secondPhase() { //Second phase where the robot uses the color sensor to align
//    LCD.clear();
//    LCD.drawString("left: falling edge", 0, 0);
//    LCD.drawString("right: rising edge", 0, 1);
    int res = Button.waitForAnyPress();
 //   LCD.clear();

    // start the party
    new Thread(odometer).start();
   // new Thread(display).start();
    
    new Thread(oc).start();
    new Thread(new ColorReader(res == Button.ID_LEFT)).start();
    while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
    }
  }

  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
