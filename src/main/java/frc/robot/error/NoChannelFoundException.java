package frc.robot.error;

/**
 * @author godbrigero
 */
public class NoChannelFoundException extends Exception {

  public final int channelTried;

  public NoChannelFoundException() {
    super("No channel found!");
    this.channelTried = -101;
  }

  public NoChannelFoundException(int channelTried) {
    super("No channel found! Tried: " + channelTried);
    this.channelTried = channelTried;
  }
}
