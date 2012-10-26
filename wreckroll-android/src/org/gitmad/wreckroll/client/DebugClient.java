package org.gitmad.wreckroll.client;

public class DebugClient implements WreckClient {

    @Override
    public void forward() {
        System.out.println("FORWARD");
    }

    @Override
    public void stop() {
        System.out.println("STAHP");
    }

    @Override
    public void reverse() {
        System.out.println("REVERSE");
    }

    @Override
    public void left() {
        System.out.println("LEFT");
    }

    @Override
    public void right() {
        System.out.println("RIGHT");
    }

    @Override
    public void toggleGun() {
        System.out.println("HES GOT A GUN");
    }

    @Override
    public void toggleSmoke() {
        System.out.println("HES GOT A SMOKE");
    }

    @Override
    public void toggleCanopy() {
        System.out.println("HES GETTING AWAY");
    }

}
