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

    @Override
    public void forward() {
        this.out.println("F");
    }

    @Override
    public void stop() {
        this.out.println("S");
    }

    @Override
    public void reverse() {
        this.out.println("V");
    }

    @Override
    public void left() {
        this.out.println("L");
    }

    @Override
    public void right() {
        this.out.println("R");
    }

    @Override
    public void toggleGun() {
        this.out.println("G");
    }

    @Override
    public void toggleSmoke() {
        this.out.println("M");
    }

    @Override
    public void toggleCanopy() {
        this.out.println("E");
    }

}
