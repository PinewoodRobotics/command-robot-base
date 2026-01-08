package frc.robot.subystem;

public class ExampleSubsystem {
    private static ExampleSubsystem instance;

    // Important note: this is required. DO NOT just just only have a constructor.
    // TRUST ME this is much easier to read and manage throughout the codebase.
    public static ExampleSubsystem GetInstance() {
        if (instance == null) {
            instance = new ExampleSubsystem();
        }

        return instance;
    }
}
