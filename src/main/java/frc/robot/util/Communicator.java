package frc.robot.util;

import com.google.gson.Gson;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.pwrup.util.IPublisher;

public class Communicator implements IPublisher {

  private final Gson gson = new Gson();

  @Override
  public void publish(String arg0, Object arg1, Class<?> arg2) {
    var networkTable = NetworkTableInstance.getDefault();
    networkTable.getEntry(arg0).setString(gson.toJson(arg1, arg2));
  }
}
