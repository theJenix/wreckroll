package org.gitmad.wreckroll.video;

import java.util.Map;

import android.graphics.Bitmap;

public class CapturedImage {

    private Bitmap bitmap;
    private Map<String, String> attributes;

    public CapturedImage(Bitmap bitmap, Map<String, String> attributes) {
        this.bitmap     = bitmap;
        this.attributes = attributes;
    }
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    public String getAttribute(String attribute) {
        return attributes.get(attribute);
    }
}
