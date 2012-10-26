package org.gitmad.wreckroll.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Image extends ATouchPoint {

    private Bitmap bitmap;
    private Rect drawRect;

    public Image(Bitmap bitmap, int centerX, int centerY) {
        this.bitmap = bitmap;
        int halfWidth = bitmap.getWidth() / 2;
        int halfHeight = bitmap.getHeight() / 2;
        this.drawRect = new Rect(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
    }
    
    public boolean isTouchPerformed(int action, float x, float y) {
        return this.drawRect.contains((int)x, (int)y);
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAlpha(50);
        canvas.drawBitmap(this.bitmap, null, this.drawRect, paint);
    }

}
