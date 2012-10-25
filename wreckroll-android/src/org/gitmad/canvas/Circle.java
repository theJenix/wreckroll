package org.gitmad.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Circle extends ATouchPoint {
    private float x, y;
    private float radius;
    private int color = Color.BLACK;

    public Circle(float x, float y, float radius, int color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(this.color);
        paint.setAlpha(50);
        canvas.drawCircle(this.x, this.y, this.radius, paint);
    }
    
    @Override
    public boolean isTouchPerformed(int action, float x, float y) {
        if (!this.getListener().isSupportedAction(action)) {
            return false;
        }
        //compute the distance between the center of the circle and the point
        float dist = (float) Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
        //if the distance is less than the circle's radius, then the point is inside the circle
        return dist <= this.radius;
    }
}