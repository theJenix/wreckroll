package org.gitmad.wreckroll.client;

public class DebugClient implements WreckClient {

    private long emergencyStopLastTap;

    public void forward() {
        System.out.println("FORWARD");
    }

    public void stop() {
        System.out.println("STAHP");
    }

    public void reverse() {
        System.out.println("REVERSE");
    }

    public void left() {
        System.out.println("LEFT");
    }

    public void right() {
        System.out.println("RIGHT");
    }

    public void toggleGun() {
        System.out.println("HES GOT A GUN");
    }

    public void toggleSmoke() {
        System.out.println("HES GOT A SMOKE");
    }

    public void toggleCanopy() {
        System.out.println("HES GETTING AWAY");
    }

    public void emergencyStop() {
        long currentTimeMillis = System.currentTimeMillis();
        if (emergencyStopLastTap != 0
                && currentTimeMillis - emergencyStopLastTap < 250) {
            System.out.println("EMERGENCY");
        }
        emergencyStopLastTap = currentTimeMillis;
    }
}
