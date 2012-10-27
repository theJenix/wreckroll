package org.gitmad.wreckroll.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RelayClient implements WreckClient {

    private HttpURLConnection connection;
    private OutputStream out;
    private int retries;
    private URL url;

    private long emergencyStopLastTap = 0;
    
    public RelayClient(String ipAddress, int port) throws IOException {
        this.url = new URL("http://" + ipAddress + ":" + port);
        this.retries = 0;
    }

	private void sendCommand(String command) {
	    HttpURLConnection connection = null;
	    try {
	        String message = new String("{\"command\":\""+ command +"\"}");
	        connection = (HttpURLConnection)url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Length", Integer.toString(message.length()));
	        connection.getOutputStream().write(message.getBytes());
	        connection.connect();
	        connection.getResponseCode();
        	this.retries = 0;
	    } catch (IOException ex) {
	        this.retries++;
	        if (this.retries >= 5) {
	            throw new RuntimeException(ex);
	        }
	    } finally {
	        if (connection != null) {
	            connection.disconnect();
	        }
	    }
    }
        
    public void forward() {
    	sendCommand("forward");
    }
    
    public void stop() {
    	sendCommand("stop");
    }
    
    public void reverse() {
    	sendCommand("reverse");
    }
    
    public void left() {
    	sendCommand("left");
    }
    
    public void right() {
    	sendCommand("right");
    }
    
    public void toggleGun() {
    	sendCommand("gun");
    }
    
    public void toggleSmoke() {
    	sendCommand("smoke");
    }
   
    public void toggleCanopy() {
     	sendCommand("canopy");
    }

    public void emergencyStop() {
        long currentTimeMillis = System.currentTimeMillis();
        if (emergencyStopLastTap != 0 && currentTimeMillis - emergencyStopLastTap < 50) {
            sendCommand("emergency");
        }
        emergencyStopLastTap = currentTimeMillis;
    }
}
