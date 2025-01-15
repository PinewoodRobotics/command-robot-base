// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.hardware.RobotWheelMover;
import frc.robot.subsystems.UpdatedSwerve;
import frc.robot.util.Communicator;
import frc.robot.util.controller.FlightStick;
import org.pwrup.SwerveDrive;
import org.pwrup.util.Config;
import org.pwrup.util.Vec2;
import org.pwrup.util.Wheel;

public class RobotContainer {

  // private final SwerveSubsystem m_swerveSubsystem = new SwerveSubsystem();

  final FlightStick m_leftFlightStick = new FlightStick(
    OperatorConstants.kFlightPortLeft
  );

  final FlightStick m_rightFlightStick = new FlightStick(
    OperatorConstants.kFlightPortRight
  );

  final UpdatedSwerve m_swerveSubsystem = new UpdatedSwerve();

  public RobotContainer() {
    m_swerveSubsystem.setDefaultCommand(
      new RunCommand(
        () ->
          m_swerveSubsystem.drive(
            new Vec2(
              m_rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKX.value
              ),
              m_rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKY.value
              ) *
              -1
            ),
            m_rightFlightStick.getRawAxis(
              FlightStick.AxisEnum.JOYSTICKROTATION.value
            ),
            1
          ),
        m_swerveSubsystem
      )
    );
  }
}
