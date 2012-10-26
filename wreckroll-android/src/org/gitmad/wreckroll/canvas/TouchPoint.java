package org.gitmad.wreckroll.canvas;

import android.graphics.Canvas;

public interface TouchPoint {

    public boolean isTouchPerformed  (int action, float x, float y);
    public void    fireTouchPerformed(float x, float y);

    public void draw(Canvas canvas);

}
