# Spark MAX + Absolute Encoder + PID (Java) — Code with explanations

This is a reference you can copy ideas from later: it shows the code you usually write for a **single Spark MAX motor subsystem** with an **absolute encoder**, a **gear ratio conversion**, **logging for AdvantageScope**, and **commands/bindings** to move it.

---

## What files you usually add

- `Constants.java`: CAN IDs, gear ratio, and PID gains live here.
- `MyMotorSubsystem.java`: the subsystem that owns the Spark MAX and encoder.
- `RobotContainer.java`: controller and button bindings live here.

---

## `Constants.java` (IDs, ratio, gains)

```java
public final class Constants {
  public static final class MyMotor {
    // The CAN ID is the number you set in REV Hardware Client.
    public static final int kCanId = 10;

    // Motor inversion makes "positive output" match the direction you want.
    public static final boolean kMotorInverted = false;

    // Gear ratio written as motorRotations / mechanismRotations.
    // Example: 12:1 reduction means motor spins 12 times per 1 mechanism rotation.
    public static final double kGearRatio = 12.0;

    // PID gains (start with only P, then tune).
    public static final double kP = 0.2;
    public static final double kI = 0.0;
    public static final double kD = 0.0;
    public static final double kFF = 0.0;

    // "Close enough" tolerance for atGoal() in mechanism rotations.
    public static final double kPosToleranceRot = 0.02;
  }
}
```

What each constant is for:
- `kCanId`: tells your code which Spark MAX on the CAN bus to talk to.
- `kMotorInverted`: fixes direction without rewiring.
- `kGearRatio`: lets you convert between motor space and mechanism space.
- `kP/kI/kD/kFF`: gains for closed-loop control.
- `kPosToleranceRot`: defines “at goal” so commands can finish cleanly.

---

## `MyMotorSubsystem.java` (Spark MAX + absolute encoder + logging + PID)

This example assumes a **NEO (brushless)** on a **Spark MAX**, and an **absolute duty-cycle encoder** plugged into the Spark MAX.

```java
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class MyMotorSubsystem extends SubsystemBase {
  // This object represents the physical Spark MAX on CAN.
  private final CANSparkMax motor =
      new CANSparkMax(Constants.MyMotor.kCanId, MotorType.kBrushless);

  // This reads the absolute encoder through the Spark MAX.
  private final SparkMaxAbsoluteEncoder absEncoder =
      motor.getAbsoluteEncoder(SparkMaxAbsoluteEncoder.Type.kDutyCycle);

  // This lets you run PID on the Spark MAX (closed-loop on the controller).
  private final SparkMaxPIDController pid = motor.getPIDController();

  // This is the "target position" your code wants, in mechanism rotations.
  private double goalMechanismRot = 0.0;

  public MyMotorSubsystem() {
    // Resets settings so last year's config doesn't surprise you.
    motor.restoreFactoryDefaults();

    // Makes positive output match your mechanism direction.
    motor.setInverted(Constants.MyMotor.kMotorInverted);

    // Brake holds position better; coast spins down freely.
    motor.setIdleMode(IdleMode.kBrake);

    // Encoder direction: make it so increasing position matches your "positive" direction.
    absEncoder.setInverted(false);

    // Convert encoder position to mechanism rotations.
    // If encoder reports motor rotations, divide by gear ratio.
    absEncoder.setPositionConversionFactor(1.0 / Constants.MyMotor.kGearRatio);

    // Tell the Spark MAX PID controller to use this encoder as its feedback sensor.
    pid.setFeedbackDevice(absEncoder);

    // PID gains for position control.
    pid.setP(Constants.MyMotor.kP);
    pid.setI(Constants.MyMotor.kI);
    pid.setD(Constants.MyMotor.kD);
    pid.setFF(Constants.MyMotor.kFF);

    // Saves config onto the Spark MAX so it persists across power cycles.
    motor.burnFlash();
  }

  @Override
  public void periodic() {
    // These show up in AdvantageScope, which is how you debug without guessing.
    Logger.recordOutput("MyMotor/GoalRot", goalMechanismRot);
    Logger.recordOutput("MyMotor/PosRot", getPositionRot());
    Logger.recordOutput("MyMotor/ErrorRot", goalMechanismRot - getPositionRot());

    // Motor status is useful to spot stalls/brownouts.
    Logger.recordOutput("MyMotor/AppliedOutput", motor.getAppliedOutput());
    Logger.recordOutput("MyMotor/BusVoltage", motor.getBusVoltage());
    Logger.recordOutput("MyMotor/Current", motor.getOutputCurrent());
  }

  // Reads the current mechanism position (rotations) from the absolute encoder.
  public double getPositionRot() {
    return absEncoder.getPosition();
  }

  // Stops the motor right now.
  public void stop() {
    motor.stopMotor();
  }

  // Open-loop control: directly set percent output (-1 to +1).
  public void setPercent(double percent) {
    motor.set(percent);
  }

  // Set the goal used by closed-loop control (in mechanism rotations).
  public void setGoalRot(double goalRot) {
    goalMechanismRot = goalRot;
  }

  // Runs closed-loop position control toward the stored goal.
  public void runClosedLoopToGoal() {
    pid.setReference(goalMechanismRot, ControlType.kPosition);
  }

  // Returns true if you're close enough to the goal to call it "done."
  public boolean atGoal() {
    return Math.abs(goalMechanismRot - getPositionRot()) <= Constants.MyMotor.kPosToleranceRot;
  }

  // Command: jog the motor while held, then stop on release.
  public Command jogCommand(double percent) {
    return runEnd(
        () -> setPercent(percent),
        this::stop
    );
  }

  // Command: drive to a setpoint using PID while scheduled.
  public Command holdPositionCommand(double goalRot) {
    return run(() -> {
      setGoalRot(goalRot);
      runClosedLoopToGoal();
    });
  }

  // Command: go to a setpoint and finish when you're close enough.
  public Command moveToPositionCommand(double goalRot) {
    return run(() -> {
      setGoalRot(goalRot);
      runClosedLoopToGoal();
    }).until(this::atGoal).finallyDo(this::stop);
  }
}
```

What each method is doing (short):
- `getPositionRot()`: reads the absolute encoder, already converted into mechanism rotations.
- `stop()`: stops the motor output immediately.
- `setPercent(percent)`: manual “just spin it” control for testing.
- `setGoalRot(goalRot)`: stores the setpoint so you can log it and reuse it.
- `runClosedLoopToGoal()`: asks the Spark MAX to run PID to the stored setpoint.
- `atGoal()`: checks if you’re within tolerance so “go to position” commands can end.
- `jogCommand(percent)`: makes a command you can bind to a held button.
- `holdPositionCommand(goalRot)`: runs PID every loop as long as the command is scheduled.
- `moveToPositionCommand(goalRot)`: runs PID and ends automatically once it reaches the target.

---

## PID (how it makes the motor move)

The entire “make it move to a target” flow is:
- `setGoalRot(target)`
- call `runClosedLoopToGoal()` repeatedly while you want it controlling
- log `Goal`, `Pos`, and `Error` so you can tune and verify direction

Tuning order:
- Start with **P only**, increase until it moves strongly but does not oscillate badly.
- Add a little **D** if it overshoots or oscillates.
- Add **I** only if it consistently stops short under load.

---

## `RobotContainer.java` (button/joystick makes it move)

This shows the pattern: create the subsystem, create the controller, bind buttons to commands.

```java
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public class RobotContainer {
  private final MyMotorSubsystem myMotor = new MyMotorSubsystem();
  private final CommandXboxController driver = new CommandXboxController(0);

  public RobotContainer() {
    // Hold A to jog forward at 20% power.
    driver.a().whileTrue(myMotor.jogCommand(0.2));

    // Hold B to hold a specific position using PID.
    driver.b().whileTrue(myMotor.holdPositionCommand(2.0));

    // Press Y to move to a position and finish automatically.
    driver.y().onTrue(myMotor.moveToPositionCommand(4.0));
  }
}
```

What each binding means:
- `whileTrue(...)`: the command runs only while the button is held.
- `onTrue(...)`: the command starts once when the button is pressed.

---

## Quick sanity checks (use logs)

- If `PosRot` never changes, the encoder wiring/config is wrong.
- If `ErrorRot` grows when you command a move, your sign convention is flipped.
- If `AppliedOutput` is high but position barely changes, you may be stalled or geared too tall.
# Spark MAX + Absolute Encoder Motor Subsystem (Java) – Notes for FRC

This doc explains the FRC-specific concepts behind a one-motor subsystem using a REV **Spark MAX**, an **absolute encoder**, **gear reduction**, **PID**, and **AdvantageScope** logging.

---

## The parts (what each thing is)

- **Spark MAX**: A REV motor controller that powers a motor and talks to the roboRIO over CAN.
- **Motor controller**: The “box” that converts your code’s request into power sent to the motor.
- **CAN (Controller Area Network)**: A shared two-wire network that lets robot devices exchange messages.
- **CAN ID**: A unique number for each CAN device so your code can address the correct one.
- **Absolute encoder**: A sensor that reports a real angle even after reboot, so you know position at startup.
- **Relative encoder**: A sensor that reports “change since boot,” so it starts at zero unless you re-zero it.
- **Gear ratio**: A scale factor between motor rotation and mechanism rotation.
- **PID**: A feedback method that uses sensor error to compute motor output toward a target.
- **Subsystem**: A class that owns hardware and exposes a small set of safe, named actions.
- **Command**: A reusable behavior that tells subsystems what to do while it is scheduled.
- **Scheduler**: The WPILib loop that runs commands and calls `periodic()` on subsystems.
- **AdvantageKit `Logger`**: A logger that records signals for later inspection.
- **AdvantageScope**: A viewer that plots your logs so you can debug without guessing.

---

## How command-based robot code is usually laid out

- **`RobotContainer`**: Create subsystems and bind controller buttons to commands.
- **Subsystem classes**: Put hardware objects and helper methods in a class that extends `SubsystemBase`.
- **Commands**: Put “actions over time” in commands instead of calling subsystem methods from random places.

---

## Hardware steps (what you do on the robot)

- **Wire motor → Spark MAX** and power the Spark MAX from the PDH/PDP with the correct breaker size.
- **Wire CANH/CANL** as a bus, keep it continuous, and avoid loose connectors.
- **Set the Spark MAX CAN ID** in REV Hardware Client and write it down.
- **Mount the absolute encoder** so it rotates with the shaft you care about.
- **Measure or confirm gear ratio** so you can convert motor/sensor units into mechanism units.

---

## Software steps (what you do in the project)

- **Install REVLib** so Java can communicate with Spark MAX devices.
- **Install AdvantageKit** so you can record and review subsystem signals.
- **Create constants** for CAN ID, inversion, gear ratio, current limits, and PID gains.

---

## CAN and CAN ID (what people mean in FRC)

- **CAN is the “network cable”** shared by many devices, so wiring and IDs matter.
- **A CAN ID is like an address**, and two devices with the same ID will cause problems.
- **If the ID is wrong in code**, your program will talk to the wrong device or to nothing.

---

## Absolute encoder (why you use it)

- **Absolute position lets you boot up already knowing the mechanism angle**, which avoids “find zero” moves.
- **You still need a defined zero reference**, which is either a mechanical mark or a calibration position.
- **If the position goes the wrong direction**, fix encoder inversion or swap your “positive direction” convention.

---

## Gear ratio (how to think about it)

- **Write ratios consistently**, for example \(motor rotations / mechanism rotations\).
- **If the encoder is on the motor**, you scale by gear ratio to get mechanism position.
- **If the encoder is on the mechanism**, you usually do not apply the motor gear ratio to that reading.

---

## What a good motor subsystem contains

### What the constructor is for
- **Create hardware objects** (Spark MAX, encoder) using IDs so code points at real devices.
- **Configure the controller** (inversion, idle mode, current limit) so behavior is predictable.
- **Configure sensor units** so your code uses mechanism units instead of raw numbers.
- **Configure PID gains** once so closed-loop control works the same every time you deploy.

### What `periodic()` is for
- **Log signals** and optionally run “background” control code that should happen every loop.

---

## Logging with AdvantageKit `Logger` (how it helps debugging)

- **Log goal, position, and error** so you can tell if the controller is aiming at the right value.
- **Log output and current** so you can tell if you are saturating, stalled, or brownouting.

Example logging calls:

```java
Logger.recordOutput("MySubsystem/Goal", goal);
Logger.recordOutput("MySubsystem/Pos", pos);
Logger.recordOutput("MySubsystem/Error", goal - pos);
Logger.recordOutput("MySubsystem/Current", motor.getOutputCurrent());
```

---

## PID (what it means when you use it on a robot)

- **P**: output based on current error, so bigger error usually means more output.
- **I**: output based on accumulated error, which helps overcome friction/gravity but can cause drift if abused.
- **D**: output based on error change rate, which can reduce overshoot and oscillation.
- **Setpoint**: the goal value you want the mechanism to reach.

### The basic PID loop you implement
- **Pick a goal** in mechanism units.
- **Run control repeatedly** so the motor keeps correcting toward that goal.

### Where PID can run
- **On the Spark MAX** using its internal PID controller (`setReference(...)`).
- **On the roboRIO** using WPILib’s `PIDController` and then sending the result to the motor.

---

## The “function menu” for most motor subsystems

The point is not the exact names, but having a small, consistent API you can reuse.

- **`double getPosition()`**: Return mechanism position in your chosen units.
- **`double getVelocity()`**: Return mechanism speed in your chosen units per second (if available).
- **`void stop()`**: Stop motor output immediately.
- **`void setPercent(double percent)`**: Open-loop control for testing and manual jogging.
- **`void setVoltage(double volts)`**: A more battery-consistent open-loop option.
- **`void setGoal(double position)`**: Store the target position for closed-loop control.
- **`void runClosedLoop()`**: Apply PID toward the stored goal.
- **`boolean atGoal()`**: Return true when error is within a tolerance.

---

## Commands and button bindings (how you make it move)

### The idea
- **A command keeps calling your subsystem methods** while it is scheduled.
- **Buttons schedule commands**, so your joystick becomes the “when” and the command is the “what.”

### Jog while held (open-loop)
- **Use a command that sets output every loop and stops when it ends**.

Example pattern:

```java
public Command jog(double percent) {
  return runEnd(
      () -> setPercent(percent),
      this::stop
  );
}
```

### Hold or move to a position while held (PID)
- **Use a command that sets the goal and runs closed-loop every loop**.

Example pattern:

```java
public Command holdPosition(double position) {
  return run(() -> {
    setGoal(position);
    runClosedLoop();
  });
}
```

### Bind in `RobotContainer`
- **Use `whileTrue(...)`** when you want it active only while the button is held.

Example bindings:

```java
driver.a().whileTrue(mySubsystem.jog(0.2));
driver.b().whileTrue(mySubsystem.holdPosition(2.0));
```

---

## Quick debug checklist

- **Motor does not move**: verify CAN wiring, CAN ID, breaker, and that REV Hardware Client sees the device.
- **Encoder does not change**: verify wiring, encoder type selection, and that you are reading the correct sensor.
- **Moves the wrong way**: fix motor inversion or encoder inversion so your sign convention matches.
- **PID oscillates**: lower P or add a small D.
- **Never reaches goal**: increase P slightly, then add a small I only if you are consistently short.
# Spark MAX + Absolute Encoder Motor Subsystem (Java) – Quick Recipe

This is a short, reusable checklist for building a **single-motor** subsystem using a **REV Spark MAX** with an **absolute encoder**, plus **PID**, **AdvantageKit/AdvantageScope logging**, and a **button/joystick command**.

### Hardware checklist (fast)
- **Motor + Spark MAX**: Connect motor to Spark MAX and power via PDP/PDH breakers.
- **CAN**: Give the Spark MAX a unique CAN ID and confirm it shows up in REV Hardware Client.
- **Absolute encoder wiring**: Wire the encoder to the Spark MAX’s absolute encoder port (duty-cycle) or the appropriate input for your encoder.
- **Mechanism**: Install gearing and write down the **gear ratio** \(motor rotations : mechanism rotations\).

### Project setup checklist (fast)
- **Vendor libs**: Install **REVLib** and **AdvantageKit (Littleton Robotics)** for logging.
- **Robot structure**: Put mechanism logic in a class that extends `SubsystemBase`.
- **Constants**: Keep CAN IDs, inversion, gear ratio, and PID gains in a `Constants` file.

---

## Step-by-step: make the subsystem (each step is 1 sentence)

### 1) Add constants you’ll reuse everywhere
- **Create** `Constants.MotorSubsystem` with CAN ID, inversion, gear ratio, and PID gains.

Example:

```java
public final class Constants {
  public static final class MotorSubsystem {
    public static final int kCanId = 10;
    public static final boolean kInverted = false;

    // Gear ratio = motorRotations / mechanismRotations (example: 12:1 means 12 motor rev per 1 mechanism rev).
    public static final double kGearRatio = 12.0;

    // PID gains (start small, tune later).
    public static final double kP = 0.2;
    public static final double kI = 0.0;
    public static final double kD = 0.0;
    public static final double kFF = 0.0;
  }
}
```

---

### 2) Create the subsystem class
- **Create** `MotorSubsystem extends SubsystemBase` and keep Spark MAX setup inside the constructor.

Template:

```java
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class MotorSubsystem extends SubsystemBase {
  private final CANSparkMax motor = new CANSparkMax(Constants.MotorSubsystem.kCanId, MotorType.kBrushless);
  private final SparkMaxAbsoluteEncoder absEncoder =
      motor.getAbsoluteEncoder(SparkMaxAbsoluteEncoder.Type.kDutyCycle);
  private final SparkMaxPIDController pid = motor.getPIDController();

  private double goalMechanismRotations = 0.0;

  public MotorSubsystem() {
    motor.restoreFactoryDefaults();
    motor.setInverted(Constants.MotorSubsystem.kInverted);
    motor.setIdleMode(IdleMode.kBrake);

    // Optional: reduce CAN spam.
    motor.setPeriodicFramePeriod(CANSparkMax.PeriodicFrame.kStatus0, 20);
    motor.setPeriodicFramePeriod(CANSparkMax.PeriodicFrame.kStatus1, 20);
    motor.setPeriodicFramePeriod(CANSparkMax.PeriodicFrame.kStatus2, 20);

    // Absolute encoder config (make sure this matches how your encoder is physically mounted).
    absEncoder.setInverted(false);

    // Convert encoder units to "mechanism rotations" using the gear ratio.
    // NOTE: Spark MAX absolute encoder position is typically in rotations (0..1 for one rotation) depending on mode/config.
    absEncoder.setPositionConversionFactor(1.0 / Constants.MotorSubsystem.kGearRatio);

    // PID uses the Spark MAX internal controller.
    pid.setFeedbackDevice(absEncoder);
    pid.setP(Constants.MotorSubsystem.kP);
    pid.setI(Constants.MotorSubsystem.kI);
    pid.setD(Constants.MotorSubsystem.kD);
    pid.setFF(Constants.MotorSubsystem.kFF);

    motor.burnFlash();
  }

  @Override
  public void periodic() {
    Logger.recordOutput("MotorSubsystem/GoalMechanismRotations", goalMechanismRotations);
    Logger.recordOutput("MotorSubsystem/AbsPosMechanismRotations", getMechanismRotations());
    Logger.recordOutput("MotorSubsystem/AppliedOutput", motor.getAppliedOutput());
    Logger.recordOutput("MotorSubsystem/BusVoltage", motor.getBusVoltage());
    Logger.recordOutput("MotorSubsystem/OutputCurrent", motor.getOutputCurrent());
  }

  public double getMechanismRotations() {
    return absEncoder.getPosition();
  }

  public void setPercentOutput(double percent) {
    motor.set(percent);
  }

  public void setGoalMechanismRotations(double rotations) {
    goalMechanismRotations = rotations;
  }

  public void runClosedLoopToGoal() {
    pid.setReference(goalMechanismRotations, ControlType.kPosition);
  }
}
```

---

## Gear ratio notes (keep it consistent)
- **Write down** your ratio as \(motor rotations / mechanism rotations\) and use it everywhere.
- **Pick one “unit”** for your subsystem API (mechanism rotations, degrees, meters) and stick to it.

---

## Logging + AdvantageScope (debug fast)

### Why use `Logger` here
- **You can see** sensor readings, goals, and motor output over time so you know what’s wrong quickly.

### What to log (minimum)
- **Absolute position** (in your chosen units).
- **Goal setpoint** (same units).
- **Applied output, voltage, current** (to catch brownouts/stalls).

### Quick “is it wired right?” checks
- **If position never changes**, your encoder wiring/config is wrong.
- **If position moves backwards**, flip encoder inversion or mechanically flip.
- **If motor fights itself**, check motor inversion vs encoder inversion.

---

## PID: make the motor move to a target (position control)

### Minimal PID flow
- **Set a goal** (setpoint) in mechanism units.
- **Call** `pid.setReference(goal, kPosition)` repeatedly (typically in `periodic()` or a command).

Example “move to position” method:

```java
public void moveToMechanismRotations(double rotations) {
  setGoalMechanismRotations(rotations);
  runClosedLoopToGoal();
}
```

### PID tuning mini-checklist (short)
- **Start with only P**, then add D if it overshoots, and add I only if it never reaches steady-state.
- **Lower P** if it oscillates, and raise P if it feels weak and never gets close.

---

## Commands: move when you press a button/joystick

### 1) Make a command that runs while held (easy)
- **Use** `runEnd` or `StartEndCommand` so it stops when released.

Example (percent output while held):

```java
import edu.wpi.first.wpilibj2.command.Command;

public Command jogForwardCommand() {
  return this.runEnd(
      () -> setPercentOutput(0.2),
      () -> setPercentOutput(0.0)
  );
}
```

### 2) Make a command that moves to a setpoint (PID)
- **Use** `run` and continuously call `setReference` while the command is scheduled.

Example (hold button to hold position at 2 rotations):

```java
public Command holdAtTwoRotations() {
  return this.run(() -> moveToMechanismRotations(2.0));
}
```

### 3) Bind it to a controller button in `RobotContainer`
- **Bind** with `whileTrue(...)` for “only while held” behavior.

Example:

```java
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public class RobotContainer {
  private final MotorSubsystem motorSubsystem = new MotorSubsystem();
  private final CommandXboxController driver = new CommandXboxController(0);

  public RobotContainer() {
    driver.a().whileTrue(motorSubsystem.jogForwardCommand());
    driver.b().whileTrue(motorSubsystem.holdAtTwoRotations());
  }
}
```

---

## Reuse this doc for other subsystems (copy/paste checklist)
- **Pick** your sensor (encoder, limit switch, gyro) and decide your “units”.
- **Define** constants (IDs, inversion, ratios, gains) in `Constants`.
- **Build** a `SubsystemBase` with clear public methods (`setGoal`, `getPosition`, `stop`).
- **Log** goal + measurement + output using `Logger.recordOutput(...)`.
- **Create** commands for “manual” (percent output) and “closed-loop” (PID to setpoint).
- **Bind** commands in `RobotContainer` using buttons/joysticks.
