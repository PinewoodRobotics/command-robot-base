package frc.robot.command.task;

import edu.wpi.first.wpilibj2.command.Command;

public class ExampleCommand extends Command {
    public ExampleCommand() {
        super();
    }

    @Override
    public void initialize() {
        // initialize the command. This function is called once when the command is
        // scheduled
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public void execute() {
        // usually you would put the logic of the command here. This function is called
        // every 20ms.
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public void end(boolean interrupted) {
        // end the command. execute stops running after this function is called.
        throw new UnsupportedOperationException("Unimplemented method 'end'");
    }

    @Override
    public boolean isFinished() {
        // this function is called to check if the command is finished.
        // if it returns true, the command will be removed from the command scheduler.
        // if it returns false, the command will continue to run.
        return false;
    }
}
