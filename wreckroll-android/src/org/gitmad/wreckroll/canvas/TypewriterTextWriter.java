package org.gitmad.wreckroll.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TypewriterTextWriter implements TextWriter {

    private static final int LINE_HEIGHT = 20; //TODO: dynamically figure this out?
    private static final int TIMER_WRITE_COUNT = 6;
    private String message;
    private boolean animating;
    private int messageIndex;
    private int timer;
    private float x;
    private float y;
    private float width;
    private int maxLines;
    private int messageLines;
    private int alpha;

    public TypewriterTextWriter(String message, float x, float y/*, float width*/) {
        this(message, x, y, Integer.MAX_VALUE);
    }

    public TypewriterTextWriter(String message, float x, float y/*, float width*/, int maxLines) {
        this.x        = x;
        this.y        = y;
//        this.width    = width;
        this.maxLines = maxLines;
        
        this.message      = message;
        this.animating    = true;
        this.messageIndex = 0;
        this.timer = 0;
        this.alpha = 255;
    }

    public boolean isWriting() {
        return this.animating;
    }

    public void writeNext(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);

        if (this.animating) {
            if (this.messageIndex < this.message.length()) {
                writeToIndex(canvas, this.messageIndex + 1, paint);
    
                if (this.timer == 0) {
                    //next index
                    this.messageIndex++;
                    //skip over newlines...they're handled above
                    while (this.messageIndex < this.message.length() && this.message.charAt(this.messageIndex) == '\n') {
                       this.messageIndex++; 
                    }
                    
                    this.timer = TIMER_WRITE_COUNT;
                } else {
                    this.timer--;
                }
            } else {
                paint.setAlpha(this.alpha);
                
                this.alpha = Math.max(this.alpha - 5, 0);
    
                writeToIndex(canvas, this.message.length(), paint);
            }
        }
    
        if (this.alpha == 0) {
            this.animating = false;
        }
            
    }

    private void writeToIndex(Canvas canvas, int index, Paint paint) {
        
        String part = this.message.substring(0, index);
        String [] lines = part.split("\n");

        //compute a line offset, so we're always writing at the bottom of our block, and scrolling
        // the text up as time goes on
        int lineOffset = Math.min(this.maxLines, this.messageLines - lines.length);
        for (int ii = 0; ii < lines.length; ii++) {
            canvas.drawText(lines[ii], x, y + LINE_HEIGHT * (ii + lineOffset), paint);
        }
    }
}
