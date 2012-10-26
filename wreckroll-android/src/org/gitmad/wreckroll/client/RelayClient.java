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
        this.out = this.connection.getOutputStream();
    }

    @Override
    public void forward() {
        
//        this.out.wr
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reverse() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void left() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void right() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void toggleGun() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void toggleSmoke() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void toggleCanopy() {
        // TODO Auto-generated method stub
        
    }
}
