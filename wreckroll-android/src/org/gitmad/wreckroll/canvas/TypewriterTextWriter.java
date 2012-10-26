package org.gitmad.wreckroll.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TypewriterTextWriter implements TextWriter {

    private static final int LINE_HEIGHT = 20; //TODO: dynamically figure this out?
    private String message;
    private boolean animating;
    private int messageIndex;
    private float x;
    private float y;
    private float width;
    private int maxLines;
    private int messageLines;

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
    }

    public boolean isWriting() {
        return this.animating;
    }

    public void writeNext(Canvas canvas) {
        if (this.animating) {
            
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
//            paint.setStyle(Style.STROKE);
            String part = this.message.substring(0, this.messageIndex + 1);
            String [] lines = part.split("\n");

            //compute a line offset, so we're always writing at the bottom of our block, and scrolling
            // the text up as time goes on
            int lineOffset = Math.min(this.maxLines, this.messageLines - lines.length);
            for (int ii = 0; ii < lines.length; ii++) {
                canvas.drawText(lines[ii], x, y + LINE_HEIGHT * (ii + lineOffset), paint);
            }

            //next index
            this.messageIndex++;
            //skip over newlines...they're handled above
            while (this.messageIndex < this.message.length() && this.message.charAt(this.messageIndex) == '\n') {
               this.messageIndex++; 
            }
            
            //if we're done, set writingMessage to false
            if (this.messageIndex >= this.message.length()) {
                this.animating = false;
            }
        }
    }
}
