package org.gitmad;

import org.gitmad.canvas.Circle;
import org.gitmad.canvas.ControllerBoard;
import org.gitmad.canvas.OnDrawListener;
import org.gitmad.canvas.OnTouchPointListener;
import org.gitmad.canvas.TouchPoint;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;

import com.gitmad.video.CameraCaptureAsyncTask;

public class WreckRollActivity extends Activity {
    
    static final String IMAGE_FILENAME = "background.jpg";
    
    private CameraCaptureAsyncTask cameraCaptureTask;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
//        Rect rectgle= new Rect();
//        Window window= getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//        int StatusBarHeight= rectgle.top;
//        int contentViewTop= 
//            window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        int TitleBarHeight= contentViewTop - StatusBarHeight;
//

        int titleBarHeight = 35;
        final ControllerBoard board = ((ControllerBoard)findViewById(R.id.panel));
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int statusBarHeight = (int) Math.ceil(titleBarHeight * metrics.density);

        int usableHeight = metrics.heightPixels - statusBarHeight;
//        Bitmap   b = BitmapFactory.decodeByteArray(buf, id.start, id.length);
        int dPadRadius = (int)(usableHeight * 0.8/2);
        //NOTE: currently assumes landscape mode
        Circle circle = new Circle(usableHeight / 2, usableHeight / 2, dPadRadius, Color.GRAY);
        board.addTouchPoint(circle);
        
        circle.setOnTouchListener(new OnTouchPointListener() {

            @Override
            public void touchPerformed(TouchPoint point, float x, float y) {
                processMovement((Circle) point, x, y);
            }
        });
        
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
    }
    
    protected void processMovement(Circle point, float touchX, float touchY) {
        boolean moving = false;
        boolean forward = false;
        boolean turning = false;
        boolean right = false;
        
        int stopBarPX = 30;
        if (Math.abs(touchY - point.getY()) > stopBarPX) {
            moving  = true;
            forward = touchY < point.getY();
        } else {
            moving = false;
        }
        
        if (Math.abs(touchX - point.getX()) > stopBarPX) {
            turning = true;
            right   = (touchX > point.getX());
        } else {
            turning = false;
        }
        
        if (moving) {
            System.out.println("MOVING " + (forward ? "FORWARD" : "BACKWARD"));
        } else {
            System.out.println("STOPPING");
        }
        
        if (turning) {
            System.out.println("TURNING " + (right ? "RIGHT" : "LEFT"));
        } else {
            System.out.println("NOT TURNING");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        this.cameraCaptureTask = new CameraCaptureAsyncTask(this, new Handler());
        this.cameraCaptureTask.execute();
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