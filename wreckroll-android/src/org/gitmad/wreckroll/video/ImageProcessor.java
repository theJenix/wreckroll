package org.gitmad.wreckroll.video;

import java.util.Map;

import android.graphics.Bitmap;

public interface ImageProcessor {

    public Bitmap process(Bitmap bitmap, Map<String, String> attributes);
}
