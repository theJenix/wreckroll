package org.gitmad.wreckroll.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DirectArduinoClient implements WreckClient {

    private Socket socket;
    private PrintWriter out;

    public DirectArduinoClient() throws UnknownHostException, IOException {
        this.socket = new Socket("192.168.1.2", 9000);
        this.out = new PrintWriter(socket.getOutputStream(), 
                true);   
    }

    
    public void forward() {
        this.out.println("F");
    }

    
    public void stop() {
        this.out.println("S");
    }

    
    public void reverse() {
        this.out.println("V");
    }

    
    public void left() {
        this.out.println("L");
    }

    
    public void right() {
        this.out.println("R");
    }

    
    public void toggleGun() {
        this.out.println("G");
    }

    
    public void toggleSmoke() {
        this.out.println("M");
    }

    
    public void toggleCanopy() {
        this.out.println("E");
    }

}
