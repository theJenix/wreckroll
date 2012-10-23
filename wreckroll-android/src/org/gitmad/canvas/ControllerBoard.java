package org.gitmad.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class ControllerBoard extends SurfaceView implements SurfaceHolder.Callback {
    private class NoOpDrawListener implements OnDrawListener {

        @Override
        public void onPreDraw() {
            //NO-OP
        }

        @Override
        public void onPostDraw() {
            //NO-OP
        }
    }
    
    private CanvasThread canvasthread;
    private Bitmap backgroundImage;
    private List<TouchPoint> touchPoints = new ArrayList<TouchPoint>();
    private OnDrawListener onDrawListener;
    
    public ControllerBoard(Context context) {
        this(context, null);
    }

    public ControllerBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        canvasthread = new CanvasThread(getHolder(), this);
        onDrawListener = new NoOpDrawListener();
        setFocusable(true);
        
        this.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return processTouch(view, event);
            }
        });
    }

    protected boolean processTouch(View view, MotionEvent event) {
        List<TouchPoint> reversedTouchPoints = new ArrayList<TouchPoint>(this.touchPoints);
        Collections.reverse(reversedTouchPoints);
        for (TouchPoint tp : reversedTouchPoints) {
            
            if (tp.isTouchPerformed(event.getX(), event.getY())) {
                tp.fireTouchPerformed(event.getX(), event.getY());  
            }
        }
        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        canvasthread.setRunning(true);
        canvasthread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        canvasthread.setRunning(false);
        while (retry) {
            try {
                canvasthread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.backgroundImage != null) {
            Paint paint = new Paint();
            canvas.drawBitmap(this.backgroundImage, 0, 0, paint);
        }
        
        for (TouchPoint tp : this.touchPoints) {
            tp.draw(canvas);
        }
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    
    public void addTouchPoint(TouchPoint touchPoint) {
        this.touchPoints.add(touchPoint);
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }    
}
