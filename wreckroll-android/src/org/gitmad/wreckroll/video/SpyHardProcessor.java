package org.gitmad.wreckroll.video;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

public class SpyHardProcessor implements ImageProcessor {

    private int maxFaces;
    private Paint paint;

    public SpyHardProcessor(int maxFaces, int color, int alpha) {
        this.maxFaces = maxFaces;
        this.paint = new Paint();
        this.paint.setColor(color);
        this.paint.setAlpha(alpha);
    }

    public Bitmap process(Bitmap bitmap) {
        Bitmap bmp565 = bitmap.copy(Bitmap.Config.RGB_565, false);
        FaceDetector fd = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), this.maxFaces);
        
        Face [] faces = new Face[this.maxFaces];
        int found = fd.findFaces(bmp565, faces);
        System.out.println("Found " + found + " faces.");
        
        if (found > 0) {
            Bitmap annotated = annotateFaces(bmp565, faces, found);
            Bitmap old = bitmap;
            bitmap = annotated.copy(bitmap.getConfig(), bitmap.isMutable());
            old.recycle();
        }
        
        bmp565.recycle();
        return bitmap;
    }

    private Bitmap annotateFaces(Bitmap image, Face[] faces, int foundFaces) {
        PointF midPoint = new PointF();
        Bitmap overlay = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(image, new Matrix(), null);
        
        for (int ii = 0; ii < foundFaces; ii++) {
            Face face = faces[ii];
            face.getMidPoint(midPoint);
            //use the eye distance as the width/height
            int radius = (int)face.eyesDistance();

            Rect rect = new Rect((int)midPoint.x - radius, (int)midPoint.y - radius, (int)midPoint.x + radius, (int)midPoint.y + radius);
            canvas.drawRect(rect, this.paint);
        }
        
        return overlay;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }
}
