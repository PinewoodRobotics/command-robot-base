package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMMotorController;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSubsystem extends SubsystemBase {

    private PWMTalonSRX m_intakeMotor;
    private IntakeSensor m_sensor;

    public IntakeSubsystem(){
        // needs to change based on what the type of motor and channel
        m_intakeMotor = new PWMTalonSRX(9);

        // basically a collection of AnalogInputs, which give a value from like 1-5000 based on how much light they receive.
        m_sensor = new IntakeSensor();
    }

    public void setMotor(double speed) {
        m_intakeMotor.set(speed);
    }

    public boolean isDetected(){
        // basically, is the sensor covered? (is the amalgamated value of all 4 inputs < 2500?)
        return m_sensor.isDetected();
    }

    public class IntakeSensor { 

        private AnalogInput m_analog1, m_analog2, m_analog3, m_analog4;

        public IntakeSensor() {
            // I think the only AnalogInputs connected are these ones, so probably doesn't need to change.
            m_analog1 = new AnalogInput(0);
            m_analog2 = new AnalogInput(1);
            m_analog3 = new AnalogInput(2);
            m_analog4 = new AnalogInput(3);
        }

        /**
         * @return whether any of the four analog inputs detects something in the intake
         */
        public boolean isDetected(){ 
            int[] AverageValues = {m_analog1.getAverageValue(), m_analog2.getAverageValue(), m_analog3.getAverageValue(), m_analog4.getAverageValue()};

            for (int analogValue : AverageValues){
                if (analogValue < 2500) { return true; }
            }
            return false;
        }
    }
}

