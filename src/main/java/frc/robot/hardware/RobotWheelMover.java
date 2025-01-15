package frc.robot.hardware;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import frc.robot.Constants.SwerveConstants;
import frc.robot.util.CustomMath;
import org.pwrup.motor.WheelMover;
import org.pwrup.util.Vec2;

public class RobotWheelMover extends WheelMover {

  private CANSparkMax m_driveMotor;

  // the turning electronics
  private CANSparkMax m_turnMotor;
  private SparkPIDController m_turnPIDController;
  private RelativeEncoder m_turnRelativeEncoder;

  private CANcoder turnCANcoder;

  public double kDriveP = SwerveConstants.kDriveP, kDriveI =
    SwerveConstants.kDriveI, kDriveD = SwerveConstants.kDriveD, kDriveIZ =
    SwerveConstants.kDriveIZ, kDriveFF = SwerveConstants.kDriveFF;

  public double kTurnP = SwerveConstants.kTurnP, kTurnI =
    SwerveConstants.kTurnI, kTurnD = SwerveConstants.kTurnD, kTurnIZ =
    SwerveConstants.kTurnIZ, kTurnFF = SwerveConstants.kTurnFF;

  public RobotWheelMover(
    int driveMotorChannel,
    boolean driveMotorReversed,
    int turnMotorChannel,
    boolean turnMotorReversed,
    int CANCoderEncoderChannel,
    SensorDirectionValue CANCoderDirection,
    double CANCoderMagnetOffset,
    String abbreviation
  ) {
    // setting up the drive motor controller
    m_driveMotor = new CANSparkMax(driveMotorChannel, MotorType.kBrushless);

    // setting up the turning motor controller and encoders
    m_turnMotor = new CANSparkMax(turnMotorChannel, MotorType.kBrushless);
    m_turnPIDController = m_turnMotor.getPIDController();
    m_turnRelativeEncoder = m_turnMotor.getEncoder();

    // setting up the CANCoder
    turnCANcoder = new CANcoder(CANCoderEncoderChannel);
    CANcoderConfiguration config = new CANcoderConfiguration();
    config.MagnetSensor.MagnetOffset = -CANCoderMagnetOffset;
    config.MagnetSensor.AbsoluteSensorRange =
      AbsoluteSensorRangeValue.Signed_PlusMinusHalf;
    config.MagnetSensor.SensorDirection = CANCoderDirection;
    turnCANcoder.getConfigurator().apply(config);

    m_driveMotor.restoreFactoryDefaults();
    m_driveMotor.setSmartCurrentLimit(SwerveConstants.kDriveCurrentLimit);
    m_driveMotor.setInverted(driveMotorReversed);

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
      SwerveConstants.kTurnMaxOutput
    );
    m_turnRelativeEncoder.setPosition(
      turnCANcoder.getAbsolutePosition().getValueAsDouble() /
      SwerveConstants.kTurnConversionFactor
    );
    m_turnRelativeEncoder.setPositionConversionFactor(
      SwerveConstants.kTurnConversionFactor
    );
  }

  @Override
  public void drive(Vec2 vector, double finalSpeedMultiplier) {
    m_driveMotor.set(CustomMath.min(vector.getModulo(), 1.0, -1.0));
    m_turnPIDController.setReference(
      vector.getAngle() / Math.PI, // wrap this angle from -pi <-> pi to -1 <-> 1
      CANSparkMax.ControlType.kPosition
    );
  }

  @Override
  public void drive(double angle, double speed) {}

  @Override
  public double getCurrentAngle() {
    return 0.0;
  }
}
