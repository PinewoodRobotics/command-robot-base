package frc.robot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import autobahn.client.AutobahnClient;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.constant.BotConstants;
import frc.robot.constant.PiConstants;
import frc.robot.util.RPC;
import lombok.Getter;

public class Robot extends LoggedRobot {

  private RobotContainer m_robotContainer;

  @Getter
  private final AutobahnClient communicationClient;

  public Robot() {
    Logger.addDataReceiver(new NT4Publisher());

    switch (BotConstants.currentMode) {
      case REAL:
        Logger.addDataReceiver(new WPILOGWriter());
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case SIM:
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case REPLAY:
        setUseTiming(false);
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        break;
    }

    Logger.start();

    // AutobahnClient: WebSocket client for pub/sub and RPC over the Autobahn bus.
    // Uses the main Raspberry Pi address from PiNetwork (first Pi in the list). The
    // RaspberryPi type extends Autobahn Address, so it can be passed directly here.
    communicationClient = new AutobahnClient(PiConstants.network.getMainPi());

    // Kick off the async WebSocket connection. join() waits for the connect attempt
    // to start (reconnects are handled internally if the socket drops later).
    communicationClient.begin().join();

    // Wire the Autobahn client into the RPC framework. This must be called before
    // using RPC.Services(), so RPC calls use the same underlying connection.
    RPC.SetClient(communicationClient);
  }

  @Override
  public void robotInit() {
    m_robotContainer = new RobotContainer();
    // Push the deploy config file to every Pi via HTTP (/set/config) so the
    // processes on those Pis start with the latest configuration.
    PiConstants.network.setConfig(readFromFile(PiConstants.configFilePath));
    // Stop then start configured processes on all Pis (per ProcessType enum).
    boolean success = PiConstants.network.restartAllPis();
    if (!success) {
      System.out.println("ERROR: Failed to restart Pis");
    }
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    // Expose Autobahn connection status to AdvantageKit/NT for visibility.
    Logger.recordOutput("Autobahn/Connected", communicationClient.isConnected());
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }

  private String readFromFile(File path) {
    try {
      return Files.readString(Paths.get(path.getAbsolutePath()));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
