package frc.robot.hardware.motor;

public interface IMotor {
    /**
     * @param speed (-1.0 to 1.0) where 0 is stationary
     */
    void setSpeed(double speed);

    /**
     * @return speed (-1.0 to 1.0) where 0 is stationary
     */
    double getCurrentSpeed();

    /**
     * @return position in radians
     */
    double getPositionRadians();

    /**
     * 
     * @return position in degrees.
     * @note this is a bridge method. It calls the "getPositionRadians" method and
     *       then converts to degrees.
     */
    default double getPositionDegrees() {
        return getPositionRadians() * 180 / Math.PI;
    }
}
