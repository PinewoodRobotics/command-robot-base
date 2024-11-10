package frc.robot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.brigero.KeybindConfigurator;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.initiate.InitiateAuto;
import frc.robot.initiate.InitiateTeleop;

public class Robot extends TimedRobot {
  private InitiateTeleop teleopInit;

  private InitiateAuto autoInit;

  @Override
  public void robotInit() {
    teleopInit = new InitiateTeleop();
    autoInit = new InitiateAuto();
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void autonomousInit() {
    // Please note that the usage of this is outlined in the docs of
    // "KeybindConfigurator"

    try {
      KeybindConfigurator.runFunctions(new File("config/auto-config.toml"), autoInit);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    // Please note that the usage of this is outlined in the docs of
    // "KeybindConfigurator"
    System.out.println("Teleop Init!");

    try {
      KeybindConfigurator.runFunctions(new File("config/teleop-config.toml"), teleopInit);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
    }
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
}
