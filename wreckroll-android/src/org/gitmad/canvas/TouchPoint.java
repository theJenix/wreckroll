package org.gitmad.canvas;

import android.graphics.Canvas;

public interface TouchPoint {

    public boolean isTouchPerformed  (float x, float y);
    public void    fireTouchPerformed(float x, float y);

    public void draw(Canvas canvas);

}
