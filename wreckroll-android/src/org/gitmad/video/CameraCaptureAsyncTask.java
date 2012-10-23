package org.gitmad.video;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gitmad.Profiler;
import org.gitmad.WreckRollActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;

public class CameraCaptureAsyncTask extends AsyncTask {

    /**
     * 
     */
    private final WreckRollActivity wreckRollActivity;
    private Handler handler;
    private DisplayMetrics displayMetrics;

    public CameraCaptureAsyncTask(WreckRollActivity wreckRollActivity, Handler handler) {
        this.wreckRollActivity = wreckRollActivity;
        this.handler = handler;
        this.displayMetrics = new DisplayMetrics();
        this.wreckRollActivity.getWindowManager().getDefaultDisplay().getMetrics(this.displayMetrics);

    }

    Profiler resetProf = new Profiler("resetAfterExtracting", 10);
    private Bitmap currentBitmap;
    
    private class ImageDescriptor {
        public ImageDescriptor(int start, int length) {
            this.start  = start;
            this.length = length;
            this.end    = start + length;
        }
        private int start;
        private int length;        
        private int end;
    }

    @Override
    protected Object doInBackground(Object... params) {
        URL url;
        try {
            
            byte[] totalBuf = new byte[1024000];
            byte[] sideBuf  = new byte[64000];

            url = new URL("http://192.168.0.20/video.cgi");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            byte[] buf = new byte[8192];
            InputStream is = connection.getInputStream();
            int read = -1;
            int offset = 0;
            
            int counter = 0;
            long startTime = System.currentTimeMillis();
            float fps = 5.0f;
            float fpsMillis = 1000/fps;
            while (!this.isCancelled() && (read = is.read(buf)) != -1) {
                System.arraycopy(buf, 0, totalBuf, offset, read);
                offset += read;
                
                ImageDescriptor id = extractImage(totalBuf, offset);
                if (id != null) {
                    long drawTime = System.currentTimeMillis();
                    resetProf.enter();
                    //dont draw the frame if we're 
                    if ((drawTime - startTime) > fpsMillis) {
                        updateBitmap(totalBuf, id);
                        startTime = System.currentTimeMillis();
                    }
                    int left = offset - id.end;
                    System.arraycopy(totalBuf, id.end, sideBuf, 0, left);
                    System.arraycopy(sideBuf,  0, totalBuf, 0, left);
                    offset = left;
                    resetProf.exit();
                }
            }
            Thread.sleep(10);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    private void updateBitmap(byte[] buf, ImageDescriptor id) {
        //TODO: may have to double buffer the bitmaps
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inBitmap = this.currentBitmap;
        Bitmap   b = BitmapFactory.decodeByteArray(buf, id.start, id.length, opts);
        this.currentBitmap = b.createScaledBitmap(b, this.displayMetrics.widthPixels, this.displayMetrics.heightPixels, false);
        b.recycle();
    }

    //NOTE: BEWARE: thar be dragons ahead (hackathon quality code, watch your step)
    Profiler extractProf = new Profiler("extractImage", 10);
    private ImageDescriptor extractImage(byte [] buf, int length) throws IOException {
        int vidStart = -1;
        int ii = 0;
        try {
            extractProf.enter();
            while(ii < (length - 2)) {
                if (buf[ii] == '-' && buf[ii + 1] == '-') {
                    vidStart = ii;
                    break;
                }
                ii++;
            }
            
            if (vidStart < 0) {
//                System.out.println("Start tag not found");
                return null;
            }
            ii = vidStart;
            int dataStart = -1;
            while(ii < (length - 4)) {
                if (buf[ii] == '\r' && buf[ii + 1] == '\n' && buf[ii + 2] == '\r' && buf[ii + 3] == '\n') {
                    dataStart = ii;
                    break;
                }
                ii++;
            }
    
            if (dataStart < 0) {
//                System.out.println("Data start not found");
                return null;
            }
            byte[] headerBuf = new byte[dataStart];
            System.arraycopy(buf, 0, headerBuf, 0, dataStart);
            String header = new String(headerBuf); 
            int consumed = 0;
            dataStart += 4;
            consumed  += dataStart;
            Pattern p = Pattern.compile("Content-length: (\\d+)");
            Matcher m = p.matcher(header);
            if (!m.find()) {
//                System.out.println("Content length not found");
                return null;
            }
            
            int len = Integer.valueOf(m.group(1));
            
            if (consumed + len > length) {
//                System.out.println("All data not found");
                return null; //dont have all the data
            }
//            System.out.println("IMAGE FOUND");
            return new ImageDescriptor(dataStart, len);
        } finally {
            extractProf.exit();
        }
    }
}