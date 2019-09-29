package ca.mcgill.ecse211.lab3;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Resources {

  /**
   * Offset from the wall (cm).
   */
  public static final int BAND_CENTER = 24;

  /**
   * Width of dead band (cm).
   */
  public static final int BAND_WIDTH = 2;

  /**
   * The wheel radius in centimeters.
   */
  public static final double WHEEL_RAD = 2.21;

  /**
   * The robot width in centimeters.
   */
  public static final double TRACK = 11.89;

  /**
   * The speed at which the robot moves forward in degrees per second.
   */
  public static final int FORWARD_SPEED = 250;

  /**
   * turning speed
   */

  public static final int TURN_SPEED = 160;

  /**
   * The speed at which the robot rotates in degrees per second.
   */
  public static final int ROTATE_SPEED = 150;

  /**
   * The motor acceleration in degrees per second squared.
   */
  public static final int ACCELERATION = 2000;

  /**
   * Timeout period in milliseconds.
   */
  public static final int TIMEOUT_PERIOD = 2000;

  /**
   * The tile size in centimeters.
   */
  public static final double TILE_SIZE = 30.48;

  /**
   * The left motor.
   */
  public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

  /**
   * The right motor.
   */
  public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  /**
   * The color sensor.
   */
  public static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);

  /**
   * The LCD.
   */
  public static final TextLCD LCD = LocalEV3.get().getTextLCD();

  /**
   * The odometer.
   */
  public static Odometer odometer = Odometer.getOdometer();

  public static final int FILTER_OUT = 23;

  /**
   * US sensor
   */
  public static final EV3UltrasonicSensor US_SENSOR = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));

}