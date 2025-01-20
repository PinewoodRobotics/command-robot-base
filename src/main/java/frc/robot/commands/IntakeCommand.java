package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.IntakeSubsystem;


public class IntakeCommand extends Command {

  @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
  private final IntakeSubsystem m_intakeSubsystem;

  private double IntakeSpeed = 10;

  /**
   * @param intakeSubsystem The subsystem used by this command.
   * @param armSubsystem the other subsystem used by this command
   */
  public IntakeCommand(IntakeSubsystem intakeSubsystem) {
    this.m_intakeSubsystem = intakeSubsystem;
    
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(intakeSubsystem);
  }

  @Override
  public void initialize() {

  }

  @Override
  public void execute() {
    m_intakeSubsystem.setMotor(IntakeSpeed);
  }

  @Override
  public void end(boolean interrupted) {
    m_intakeSubsystem.setMotor(0);
  }

  @Override
  public boolean isFinished() {
    return m_intakeSubsystem.isDetected();
  }
}
