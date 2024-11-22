package frc.robot.hardware.sensor;

public interface IVisor {
    /**
     * @return the current voltage
     */
    double getVoltage();

    /**
     * @return true if the visor is detected
     */
    boolean isDetected();
}
