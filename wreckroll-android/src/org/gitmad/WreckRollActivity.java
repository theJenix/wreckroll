package org.gitmad;

import java.io.IOException;
import java.net.UnknownHostException;

import org.gitmad.canvas.Circle;
import org.gitmad.canvas.ControllerBoard;
import org.gitmad.canvas.OnDrawListener;
import org.gitmad.canvas.OnTouchPointListener;
import org.gitmad.canvas.TouchPoint;
import org.gitmad.client.DirectArduinoClient;
import org.gitmad.client.WreckClient;
import org.gitmad.video.CameraCaptureAsyncTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;


public class WreckRollActivity extends Activity {
    
    static final String IMAGE_FILENAME = "background.jpg";
    
    private CameraCaptureAsyncTask cameraCaptureTask;
    private WreckClient client;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final ControllerBoard board = ((ControllerBoard)findViewById(R.id.panel));

//        Rect rectgle= new Rect();
//        Window window= getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//        int StatusBarHeight= rectgle.top;
//        int contentViewTop= 
//            window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        int TitleBarHeight= contentViewTop - StatusBarHeight;
//

        board.setOnDrawListener(new OnDrawListener() {

            @Override
            public void onPreDraw() {
                //set the latest bitmap captured from the camera
                board.setBackgroundImage(cameraCaptureTask.getCurrentBitmap());
            }

            @Override
            public void onPostDraw() {
                //nothing to do
            }
        });
        
        //draw the buttons as touchpoint controls
        int titleBarHeight = 50;
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int statusBarHeight = (int) Math.ceil(titleBarHeight * metrics.density);

        int usableHeight = metrics.heightPixels - statusBarHeight;
        int dPadRadius = (int)(usableHeight * 0.8/2);
        //NOTE: currently assumes landscape mode
        Circle circle = new Circle(usableHeight / 2, usableHeight / 2, dPadRadius, Color.LTGRAY);
        board.addTouchPoint(circle);
        
        circle.setOnTouchListener(new OnTouchPointListener() {
            @Override
            public boolean isSupportedAction(int action) {
                return action != MotionEvent.ACTION_UP;
            }

            @Override
            public void touchPerformed(TouchPoint point, float x, float y) {
                processMovement((Circle) point, x, y);
            }
        });
        
        int radius = usableHeight / 8;

        Circle smokeButton = new Circle(metrics.widthPixels - 200, 1 * usableHeight / 2 - radius / 2, radius, Color.RED);
        board.addTouchPoint(smokeButton);
        smokeButton.setOnTouchListener(new OnTouchPointListener() {
            @Override
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            @Override
            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleSmoke();
            }
        });
        
        Circle gunButton       = new Circle(metrics.widthPixels - 75, 1 * usableHeight / 4, radius, Color.RED);
        board.addTouchPoint(gunButton);
        gunButton.setOnTouchListener(new OnTouchPointListener() {
            @Override
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }
            
            @Override
            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleGun();
            }
        });

        Circle canopyButton    = new Circle(metrics.widthPixels - 200, 3 * usableHeight / 4, radius, Color.RED);
        board.addTouchPoint(canopyButton);
        canopyButton.setOnTouchListener(new OnTouchPointListener() {
            
            @Override
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            @Override
            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleCanopy();
            }
        });
        Circle startStopButton = new Circle(metrics.widthPixels - 75, 1 * usableHeight / 2 + radius / 2, radius, Color.RED);
        board.addTouchPoint(startStopButton);

    }
    
    protected void processMovement(Circle point, float touchX, float touchY) {
        boolean moving = false;
        boolean forward = false;
        boolean turning = false;
        boolean right = false;
        
        int stopBarPX = 30;
        if (Math.abs(touchY - point.getY()) > stopBarPX) {
            if (touchY < point.getY()) {
                this.client.forward();
            } else {
                this.client.reverse();
            }
        } else {
            this.client.stop();
        }
        
        if (Math.abs(touchX - point.getX()) > stopBarPX) {
            turning = true;
            if (touchX > point.getX()) {
                this.client.right();
            } else {
                this.client.left();
            }
        } else {
            System.out.println("NOT TURNING");
        }        
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        this.cameraCaptureTask = new CameraCaptureAsyncTask(this, new Handler());
        this.cameraCaptureTask.execute();
        
        try {
            this.client = new DirectArduinoClient();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        this.cameraCaptureTask.cancel(true);
    }

    void setPanelBackground(Bitmap bitmap) {
        ((ControllerBoard)findViewById(R.id.panel)).setBackgroundImage(bitmap);
    }
}