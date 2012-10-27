package org.gitmad.wreckroll.client;

public interface WreckClient {

    public void forward();
    
    public void stop();
    
    public void reverse();
    
    public void left();
    
    public void right();
    
    public void toggleGun();
    
    public void toggleSmoke();
    
    public void toggleCanopy();
    
    public void emergencyStop();
}
