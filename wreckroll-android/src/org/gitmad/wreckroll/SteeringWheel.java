package org.gitmad.wreckroll;

import org.gitmad.wreckroll.client.WreckClient;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SteeringWheel implements SensorEventListener {

    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 1e-4f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private WreckClient client;

    public SteeringWheel(WreckClient client) {
        this.client = client;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    public void onSensorChanged(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current
        // rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
                    * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin
            // of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the
            // timestep
            // We will convert this axis-angle representation of the delta
            // rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix,
                deltaRotationVector);
//        float[] R={0,0,0,0,0,0,0,0,0};
//        
//        float[] tilt_data = {0, 0, 0};
//        // Calculate the rotation matrix, and use that to get the orientation:
//        if(SensorManager.getRotationMatrix(R, null, new float[] {0, 0, -1}, event.values))
//            SensorManager.getOrientation(R, tilt_data);
////        SensorManager.getOrientation(deltaRotationVector, tilt_data);
        
//        if (event.values[0] )
        System.out.println(deltaRotationVector[0] + ", " + deltaRotationVector[1] + ", " + deltaRotationVector[2] + ", " + deltaRotationVector[3]);
//        processTilt(tilt_data);
        // User code should concatenate the delta rotation we computed with the
        // current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }

    private void processTilt(float[] tilt_data) {
        if (tilt_data[0] > 45 * Math.PI/180) {
            client.right();
        } else if (tilt_data[0] < -45 * Math.PI/180) {
            client.left();
        }
    }

}
