package org.gitmad.wreckroll.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RelayClient implements WreckClient {

    private HttpURLConnection connection;
    private OutputStream out;

    public RelayClient(String ipAddress, int port) throws IOException {
        URL url = new URL("http://" + ipAddress + ":" + port);
        this.connection = (HttpURLConnection)url.openConnection();
        this.connection.setDoInput(true);
        this.connection.setDoOutput(true);
        this.out = this.connection.getOutputStream();
    }

	private void sendCommand(String command) throws IOException{
    	this.out.write(new String("{\"command\":\""+ command +"\"}").getBytes());
    	this.out.flush();
    }
    
    @Override
    public void forward() {
        
//        this.out.wr
        // TODO Auto-generated method stub
    	sendCommand("forward");
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    	sendCommand("stop");
    }

    @Override
    public void reverse() {
        // TODO Auto-generated method stub
    	sendCommand("referse");
    }

    @Override
    public void left() {
        // TODO Auto-generated method stub
    	sendCommand("left");
        
    }

    @Override
    public void right() {
        // TODO Auto-generated method stub
    	sendCommand("right");
    }

    @Override
    public void toggleGun() {
        // TODO Auto-generated method stub
    	sendCommand("gun");
    }

    @Override
    public void toggleSmoke() {
        // TODO Auto-generated method stub
    	sendCommand("smoke");
    }

    @Override
    public void toggleCanopy() {
        // TODO Auto-generated method stub
    	sendCommand("canopy");
    }
}
