package frc.robot.util.controller;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * @author goofball06
 * @co-author godbrigero
 */
public class TestController extends Joystick {

  public final ButtonList Buttons = new ButtonList();
  public final AxisList Axes = new AxisList();

  public class ButtonList {
    public Button
    X = new Button(1),
    A = new Button(2),
    B = new Button(3),
    Y = new Button(4),
    B5 = new Button(5),
    B6 = new Button(6),
    B7 = new Button(7),
    B8 = new Button(8),
    LEFTSLIDERUP = new Button(9),
    LEFTSLIDERDOWN = new Button(10),
    MYSTERYBUTTON = new Button(13),
    MYSTERYBUTTON2 = new Button(14),
    SCROLLPRESS = new Button(15),
    B16 = new Button(16),
    B17 = new Button(17),
    TRIGGER = new Button(18),
    B19 = new Button(19),
    XBOX = new Button(20),
    SCREENSHARE = new Button(21),
    UPLOAD = new Button(22);

    /**
     * @implNote the default button enum for the logitech controller
     */
    public class Button {

      public int value;

      public JoystickButton pressTrigger() {
        return new JoystickButton(TestController.this, this.value);
      }

      public boolean getRawButton() {
        return TestController.this.getRawButton(value);
      }

      public Button(int val) {
        this.value = val;
      }
    }
  }

  /**
   * @apiNote this is for axis channels in a controller for the logitec
   */
  public class AxisList {

    public Axis placeholder = new Axis(0);

    public class Axis {

      public int value;

      public double getRawAxis() {
        return TestController.this.getRawAxis(value);
      }

      public Axis(int val) {
        this.value = val;
      }
    }
  }

  /**
   * @param port the port of the controller
   */
  public TestController(int port) {
    super(port);
  }
}
