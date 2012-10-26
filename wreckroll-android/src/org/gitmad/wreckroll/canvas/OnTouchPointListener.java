package org.gitmad.wreckroll.canvas;


public interface OnTouchPointListener {

    public boolean isSupportedAction(int action);

    public void touchPerformed(TouchPoint point, float x, float y);
}
