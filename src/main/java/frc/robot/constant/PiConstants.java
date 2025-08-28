package frc.robot.constant;

import java.io.File;

import edu.wpi.first.wpilibj.Filesystem;
import lombok.AllArgsConstructor;
import pwrup.frc.core.online.raspberrypi.PiNetwork;
import pwrup.frc.core.online.raspberrypi.RaspberryPi;

public class PiConstants {
  public static File configFilePath = new File(
      Filesystem.getDeployDirectory().getAbsolutePath() + "/config");

  @AllArgsConstructor
  public static enum ProcessType {
    POSE_EXTRAPOLATOR("position-extrapolator"),
    APRIL_TAG_DETECTOR("april-server");

    private final String name;

    @Override
    public String toString() {
      return name;
    }
  }

  public static final PiNetwork<ProcessType> network;
  static {
    network = new PiNetwork<ProcessType>();
  }
}
