package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSubsystem extends SubsystemBase {
  private PWMTalonSRX m_intakeMotor;

  public IntakeSubsystem() {
    m_intakeMotor = new PWMTalonSRX(9);
  }

  public void robotTalks(double speed) {
    int channel = m_intakeMotor.getChannel();
    System.out.println(channel);
  }
}
