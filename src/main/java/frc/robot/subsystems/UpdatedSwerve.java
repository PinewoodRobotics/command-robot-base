package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.SwerveConstants;
import frc.robot.hardware.RobotWheelMover;
import frc.robot.util.Communicator;
import org.pwrup.SwerveDrive;
import org.pwrup.util.Config;
import org.pwrup.util.Vec2;
import org.pwrup.util.Wheel;

// The purpose of this class is to hide a bunch of wheel init code from the RobotContainer
public class UpdatedSwerve extends SubsystemBase {

  private final RobotWheelMover m_frontLeftSwerveModule;
  private final RobotWheelMover m_frontRightSwerveModule;
  private final RobotWheelMover m_rearLeftSwerveModule;
  private final RobotWheelMover m_rearRightSwerveModule;

  private final SwerveDrive swerve;

  public UpdatedSwerve() {
    this.m_frontLeftSwerveModule =
      new RobotWheelMover(
        SwerveConstants.kFrontLeftDriveMotorPort,
        SwerveConstants.kFrontLeftDriveMotorReversed,
        SwerveConstants.kFrontLeftTurningMotorPort,
        SwerveConstants.kFrontLeftTurningMotorReversed,
        SwerveConstants.kFrontLeftCANcoderPort,
        SwerveConstants.kFrontLeftCANcoderDirection,
        SwerveConstants.kFrontLeftCANcoderMagnetOffset,
        "FL"
      );
    this.m_frontRightSwerveModule =
      new RobotWheelMover(
        SwerveConstants.kFrontRightDriveMotorPort,
        SwerveConstants.kFrontRightDriveMotorReversed,
        SwerveConstants.kFrontRightTurningMotorPort,
        SwerveConstants.kFrontRightTurningMotorReversed,
        SwerveConstants.kFrontRightCANcoderPort,
        SwerveConstants.kFrontRightCANcoderDirection,
        SwerveConstants.kFrontRightCANcoderMagnetOffset,
        "FR"
      );
    this.m_rearLeftSwerveModule =
      new RobotWheelMover(
        SwerveConstants.kRearLeftDriveMotorPort,
        SwerveConstants.kRearLeftDriveMotorReversed,
        SwerveConstants.kRearLeftTurningMotorPort,
        SwerveConstants.kRearLeftTurningMotorReversed,
        SwerveConstants.kRearLeftCANcoderPort,
        SwerveConstants.kRearLeftCANcoderDirection,
        SwerveConstants.kRearLeftCANcoderMagnetOffset,
        "RL"
      );
    this.m_rearRightSwerveModule =
      new RobotWheelMover(
        SwerveConstants.kRearRightDriveMotorPort,
        SwerveConstants.kRearRightDriveMotorReversed,
        SwerveConstants.kRearRightTurningMotorPort,
        SwerveConstants.kRearRightTurningMotorReversed,
        SwerveConstants.kRearRightCANcoderPort,
        SwerveConstants.kRearRightCANcoderDirection,
        SwerveConstants.kRearRightCANcoderMagnetOffset,
        "RR"
      );

    this.swerve =
      new SwerveDrive(
        new Config(
          new Communicator(),
          new Wheel[] {
            new Wheel(
              new Vec2(0.238125, 0.238125),
              m_frontLeftSwerveModule,
              0.075
            ),
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
  }

  public void drive(Vec2 vector, double rotation, double speed) {
    swerve.drive(vector, rotation, speed);
  }
}
