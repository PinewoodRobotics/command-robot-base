package frc.robot.util.controller;

import java.util.List;

import edu.wpi.first.wpilibj.Joystick;

public class FlightStick extends Joystick {

  /**
   * @implNote the default button enum for the Flightstick
   */
  public enum ButtonEnum {
    // button indexes begin at 1 for some reason
    A(1),
    B(2),
    X(3),
    Y(4),
    B5(5),
    B6(6),
    B7(7),
    B8(8),
    LEFTSLIDERUP(9),
    LEFTSLIDERDOWN(10),
    RIGHTSLIDERUP(11),
    RIGHTSLIDERDOWN(12),
    MYSTERYBUTTON(13),
    MYSTERYBUTTON2(14),
    SCROLLPRESS(15),
    B16(16),
    B17(17),
    TRIGGER(18),
    B19(19),
    XBOX(20),
    SCREENSHARE(21),
    UPLOAD(22);

    public int value;

    /**
     * @param val for enum setting
     */
    ButtonEnum(int val) {
      this.value = val;
    }

    /**
     * @apiNote stringifies the enum
     */
    @Override
    public String toString() {
      return ("ButtonEnum{" + "name='" + name() + '\'' + ", intValue=" + value + '}');
    }

    /**
     * @apiNote DO NOT SET VALUES! THIS IS ONLY FOR CUSTOM CONFIGS!
     * @param newVal the new value of the enum
     */
    public void setValue(int newVal) {
      this.value = newVal;
    }
  }

  /**
   * @apiNote this is for axis channels in a controller for the logitec
   */
  public enum AxisEnum {
    JOYSTICKX(0),
    JOYSTICKY(1),
    JOYSTICKROTATION(2),
    H2X(3),
    H2Y(4),
    LEFTSLIDER(5),
    SCROLLWHEEL(6),
    RIGHTSLIDER(7);

    public int value;

    /**
     * @param val for setting the port
     */
    AxisEnum(int val) {
      this.value = val;
    }

    /**
     * @apiNote stringifies the enum
     */
    @Override
    public String toString() {
      return ("ButtonEnum{" + "name='" + name() + '\'' + ", intValue=" + value + '}');
    }

    /**
     * @apiNote DO NOT SET VALUES! THIS IS ONLY FOR CUSTOM CONFIGS!
     * @param newVal the new value of the enum
     */
    public void setValue(int newVal) {
      this.value = newVal;
    }
  }

  /**
   * @param port the port of the controller
   * @throws NoChannelFoundException if the channel is invalid that means that
   *                                 some code upstairs is buggy and needs to be
   *                                 fixed
   */
  public FlightStick(int port) {
    super(port);
  }

  /**
   * @param values new values
   */
  public void setValues(List<Integer> values) {
    for (int i = 0; i < values.size(); i++) {
      if (i < 12) {
        ButtonEnum.values()[i].setValue(values.get(i));
      } else {
        AxisEnum.values()[i].setValue(values.get(i));
      }
    }
  }
}
