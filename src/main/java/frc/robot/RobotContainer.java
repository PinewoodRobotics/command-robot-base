// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.hardware.RobotWheelMover;
import frc.robot.util.Communicator;
import frc.robot.util.controller.FlightModule;
import frc.robot.util.controller.FlightStick;
import frc.robot.util.controller.OperatorPanel;
import org.pwrup.SwerveDrive;
import org.pwrup.util.Config;
import org.pwrup.util.Vec2;
import org.pwrup.util.Wheel;

public class RobotContainer {

  private final RobotWheelMover m_frontLeftSwerveModule = new RobotWheelMover(
    SwerveConstants.kFrontLeftDriveMotorPort,
    SwerveConstants.kFrontLeftDriveMotorReversed,
    SwerveConstants.kFrontLeftTurningMotorPort,
    SwerveConstants.kFrontLeftTurningMotorReversed,
    SwerveConstants.kFrontLeftCANcoderPort,
    SwerveConstants.kFrontLeftCANcoderDirection,
    SwerveConstants.kFrontLeftCANcoderMagnetOffset,
    "FL"
  );
  private final RobotWheelMover m_frontRightSwerveModule = new RobotWheelMover(
    SwerveConstants.kFrontRightDriveMotorPort,
    SwerveConstants.kFrontRightDriveMotorReversed,
    SwerveConstants.kFrontRightTurningMotorPort,
    SwerveConstants.kFrontRightTurningMotorReversed,
    SwerveConstants.kFrontRightCANcoderPort,
    SwerveConstants.kFrontRightCANcoderDirection,
    SwerveConstants.kFrontRightCANcoderMagnetOffset,
    "FR"
  );
  private final RobotWheelMover m_rearLeftSwerveModule = new RobotWheelMover(
    SwerveConstants.kRearLeftDriveMotorPort,
    SwerveConstants.kRearLeftDriveMotorReversed,
    SwerveConstants.kRearLeftTurningMotorPort,
    SwerveConstants.kRearLeftTurningMotorReversed,
    SwerveConstants.kRearLeftCANcoderPort,
    SwerveConstants.kRearLeftCANcoderDirection,
    SwerveConstants.kRearLeftCANcoderMagnetOffset,
    "RL"
  );
  private final RobotWheelMover m_rearRightSwerveModule = new RobotWheelMover(
    SwerveConstants.kRearRightDriveMotorPort,
    SwerveConstants.kRearRightDriveMotorReversed,
    SwerveConstants.kRearRightTurningMotorPort,
    SwerveConstants.kRearRightTurningMotorReversed,
    SwerveConstants.kRearRightCANcoderPort,
    SwerveConstants.kRearRightCANcoderDirection,
    SwerveConstants.kRearRightCANcoderMagnetOffset,
    "RR"
  );

  // private final SwerveSubsystem m_swerveSubsystem = new SwerveSubsystem();

  final OperatorPanel m_operatorPanel = new OperatorPanel(
    OperatorConstants.kOperatorPanelPort
  );

  final FlightModule m_flightModule = new FlightModule(
    OperatorConstants.kFlightPortLeft,
    OperatorConstants.kFlightPortRight
  );

  final SwerveDrive swerve = new SwerveDrive(
    new Config(
      new Communicator(),
      new Wheel[] {
        new Wheel(new Vec2(0.238125, 0.238125), m_frontLeftSwerveModule, 0.075),
        new Wheel(
          new Vec2(0.238125, -0.238125),
          m_frontRightSwerveModule,
          0.075
        ),
        new Wheel(
          new Vec2(-0.238125, -0.238125),
          m_rearLeftSwerveModule,
          0.075
        ),
        new Wheel(
          new Vec2(-0.238125, 0.238125),
          m_rearRightSwerveModule,
          0.075
        ),
      }
    )
  );

  public RobotContainer() {
    new SubsystemBase() {}
      .setDefaultCommand(
        new RunCommand(() ->
          swerve.drive(
            new Vec2(
              m_flightModule.rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKX.value
              ),
              m_flightModule.rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKY.value
              ) *
              -1
            ),
            m_flightModule.leftFlightStick.getRawAxis(
              FlightStick.AxisEnum.JOYSTICKROTATION.value
            ),
            1
          )
        )
      );
  }
}
