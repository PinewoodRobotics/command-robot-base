package frc.robot.subsystems;

//example phoenix6 code here: https://github.com/CrossTheRoadElec/Phoenix6-Examples/blob/main/java/CANcoder/src/main/java/frc/robot/Robot.java
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.Constants.SwerveConstants;
import frc.robot.util.MathFunc;

public class SwerveModule {

  // the driving electronics
  private CANSparkMax m_driveMotor;
  private RelativeEncoder m_driveRelativeEncoder;

  // the turning electronics
  private CANSparkMax m_turnMotor;
  private SparkPIDController m_turnPIDController;
  private RelativeEncoder m_turnRelativeEncoder;

  private CANcoder turnCANcoder;

  // the Shuffleboard tab and entries
  private String sb_abbreviation;
  private ShuffleboardTab sb_tab;

  public double kDriveP = SwerveConstants.kDriveP,
      kDriveI = SwerveConstants.kDriveI,
      kDriveD = SwerveConstants.kDriveD,
      kDriveIZ = SwerveConstants.kDriveIZ,
      kDriveFF = SwerveConstants.kDriveFF;

  public double kTurnP = SwerveConstants.kTurnP,
      kTurnI = SwerveConstants.kTurnI,
      kTurnD = SwerveConstants.kTurnD,
      kTurnIZ = SwerveConstants.kTurnIZ,
      kTurnFF = SwerveConstants.kTurnFF;

  public GenericEntry sb_kDriveP, sb_kDriveI, sb_kDriveD, sb_kDriveIZ, sb_kDriveFF, sb_kTurnP, sb_kTurnI, sb_kTurnD,
      sb_kTurnIZ, sb_kTurnFF, sb_speed, sb_angle, sb_m_turnRelativeEncoderAngle, sb_turnCANcoderAngle;

  public SwerveModule(
      int driveMotorChannel,
      boolean driveMotorReversed,
      int turnMotorChannel,
      boolean turnMotorReversed,
      int CANCoderEncoderChannel,
      SensorDirectionValue CANCoderDirection,
      double CANCoderMagnetOffset,
      String abbreviation) {
    // setting up the drive motor controller
    m_driveMotor = new CANSparkMax(driveMotorChannel, MotorType.kBrushless);
    m_driveRelativeEncoder = m_driveMotor.getEncoder();

    // setting up the turning motor controller and encoders
    m_turnMotor = new CANSparkMax(turnMotorChannel, MotorType.kBrushless);
    m_turnPIDController = m_turnMotor.getPIDController();
    m_turnRelativeEncoder = m_turnMotor.getEncoder();

    // setting up the CANCoder
    turnCANcoder = new CANcoder(CANCoderEncoderChannel);
    CANcoderConfiguration config = new CANcoderConfiguration();
    config.MagnetSensor.MagnetOffset = -CANCoderMagnetOffset;
    config.MagnetSensor.AbsoluteSensorRange = AbsoluteSensorRangeValue.Signed_PlusMinusHalf;
    config.MagnetSensor.SensorDirection = CANCoderDirection;
    turnCANcoder.getConfigurator().apply(config);

    // for a full list of SparkMax commands, vist
    // https://robotpy.readthedocs.io/projects/rev/en/latest/rev/RelativeEncoder.html

    // setting up the drive motor
    m_driveMotor.restoreFactoryDefaults();
    m_driveMotor.setSmartCurrentLimit(SwerveConstants.kDriveCurrentLimit);
    m_driveMotor.setInverted(driveMotorReversed);

    // setting up the turn motor
    m_turnMotor.restoreFactoryDefaults();
    m_turnMotor.setInverted(turnMotorReversed);
    m_turnMotor.setSmartCurrentLimit(SwerveConstants.kTurnCurrentLimit);
    m_turnPIDController.setP(kTurnP);
    m_turnPIDController.setI(kTurnI);
    m_turnPIDController.setD(kTurnD);
    m_turnPIDController.setIZone(kTurnIZ);
    m_turnPIDController.setFF(kTurnFF);
    m_turnPIDController.setPositionPIDWrappingEnabled(true);
    m_turnPIDController.setPositionPIDWrappingMinInput(-0.5);
    m_turnPIDController.setPositionPIDWrappingMaxInput(0.5);
    m_turnPIDController.setOutputRange(
        SwerveConstants.kTurnMinOutput,
        SwerveConstants.kTurnMaxOutput);
    m_turnRelativeEncoder.setPosition(
        turnCANcoder.getAbsolutePosition().getValueAsDouble() /
            SwerveConstants.kTurnConversionFactor);
    m_turnRelativeEncoder.setPositionConversionFactor(
        SwerveConstants.kTurnConversionFactor);

    // custom function to set up the Shuffleboard tab
    createShuffleboardTab(abbreviation);
  }

  /**
   * Sends the speed and angle commands to the swerve module with customizable
   * speed.
   * 
   * @param speed The desired speed. Domain: [-1, 1]
   * @param angle The desired angle. Domain: (-0.5, 0.5]
   */
  public void drive(double speed, double angle) {
    drive(speed, angle, SwerveConstants.kDefaultSpeedMultiplier);
  }

  /**
   * Sends the speed and angle commands to the swerve module with customizable
   * speed.
   * 
   * @param speed The desired speed. Domain: [-1, 1]
   * @param angle The desired angle. Domain: (-0.5, 0.5]
   */
  public void drive(double speed, double angle, double tempSpeedMultiplier) {
    if (SwerveConstants.kOptimizeAngles) {
      // if the opposite direction is closer to the current angle, flip the angle and
      // the speed
      double[] optimizedState = optimize(
          speed,
          angle,
          m_turnRelativeEncoder.getPosition());
      speed = optimizedState[0];
      angle = optimizedState[1];
    }

    // sending the motor speed to the driving motor controller
    m_driveMotor.set(speed * tempSpeedMultiplier);

    // sending the motor angle to the turning motor controller
    m_turnPIDController.setReference(angle, CANSparkMax.ControlType.kPosition);

    // updates the Shuffleboard tab
    updateShuffleboardTab(speed, angle);
  }

  /**
   * @return The current position of the module and angle in meters and radians.
   */
  public SwerveModulePosition getPosition() {
    return new SwerveModulePosition(
        m_driveRelativeEncoder.getPosition() * SwerveConstants.kWheelDiameterMeters,
        new Rotation2d(m_turnRelativeEncoder.getPosition() * 2 * Math.PI));
  }

  public void reset() {
    m_turnRelativeEncoder.setPosition(
        turnCANcoder.getAbsolutePosition().getValueAsDouble());
  }

  /**
   * If the opposite angle is closer to the desired one, returns a reversed speed
   * and a flipped angle.
   * 
   * @param speed        the speed the drive motor should be running at
   * @param angle        the angle the turn motor should reach
   * @param encoderAngle the current turn encoder's angle
   * @return [The optimized speed, The optimized angle]
   */
  private double[] optimize(double speed, double angle, double encoderAngle) {
    encoderAngle = MathFunc.putWithinHalfToHalf(encoderAngle);
    if (Math.abs(angle - encoderAngle) < 0.25 ||
        Math.abs(angle - encoderAngle) > 0.75) {
      return new double[] { speed, angle };
    }

    return new double[] { -speed, MathFunc.putWithinHalfToHalf(angle + 0.5) };
  }

  private void createShuffleboardTab(String abbreviation) {
    sb_abbreviation = abbreviation;

    // learn how to use shuffleboard here:
    // https://docs.wpilib.org/en/stable/docs/software/dashboards/shuffleboard/layouts-with-code/index.html

    // creates the Shuffleboard tab
    sb_tab = Shuffleboard.getTab(sb_abbreviation);

    // creates the modifiable entries for the driving PID values
    sb_kDriveP = sb_tab.add("kDriveP", kDriveP).getEntry();
    sb_kDriveI = sb_tab.add("kDriveI", kDriveI).getEntry();
    sb_kDriveD = sb_tab.add("kDriveD", kDriveD).getEntry();
    sb_kDriveIZ = sb_tab.add("kDriveIZ", kDriveIZ).getEntry();
    sb_kDriveFF = sb_tab.add("kDriveFF", kDriveFF).getEntry();

    // creates the modifiable entries for the turning PID values
    sb_kTurnP = sb_tab.add("kTurnP", kTurnP).getEntry();
    sb_kTurnI = sb_tab.add("kTurnI", kTurnI).getEntry();
    sb_kTurnD = sb_tab.add("kTurnD", kTurnD).getEntry();
    sb_kTurnIZ = sb_tab.add("kTurnIZ", kTurnIZ).getEntry();
    sb_kTurnFF = sb_tab.add("kTurnFF", kTurnFF).getEntry();

    // for the calculated speed and angle of this swerve module on this iteration
    sb_speed = sb_tab.add("speed", 0).getEntry();
    sb_angle = sb_tab.add("angle", 0).getEntry();

    // for the reported angles from the encoders with the sparkMax
    sb_m_turnRelativeEncoderAngle = sb_tab.add("turnEncoderAngle", 0).getEntry();
    sb_turnCANcoderAngle = sb_tab.add("turnCANcoderAngle", 0).getEntry();
  }

  private void updateShuffleboardTab(double speed, double angle) {
    sb_speed.setDouble(speed);
    sb_angle.setDouble(angle);

    sb_m_turnRelativeEncoderAngle.setDouble(m_turnRelativeEncoder.getPosition());
    sb_turnCANcoderAngle.setDouble(
        turnCANcoder.getAbsolutePosition().getValueAsDouble());
  }
}
