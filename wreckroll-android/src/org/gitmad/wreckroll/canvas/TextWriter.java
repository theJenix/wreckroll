package org.gitmad.wreckroll.canvas;

import android.graphics.Canvas;

public interface TextWriter {

    public boolean isWriting();
    
    public void writeNext(Canvas canvas);
}
