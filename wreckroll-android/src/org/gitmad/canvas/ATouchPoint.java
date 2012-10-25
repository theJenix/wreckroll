package org.gitmad.canvas;

public abstract class ATouchPoint implements TouchPoint {
    private class NoOpTouchPointListener implements OnTouchPointListener {

        @Override
        public boolean isSupportedAction(int action) {
            return false;
        }

        @Override
        public void touchPerformed(TouchPoint point, float x, float y) {
            //NO-OP
        }
    }
    private OnTouchPointListener listener = new NoOpTouchPointListener();

    @Override
    public void fireTouchPerformed(float x, float y) {
        this.listener.touchPerformed(this, x, y);
    }
    
    OnTouchPointListener getListener() {
        return listener;
    }
    
    public void setOnTouchListener(OnTouchPointListener listener) {
        this.listener = listener;
    }
}
