package com.vanjav.roadlinepainter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by vveselin on 07/10/2016.
 */

public class RoadLinePainterView extends SurfaceView  implements Choreographer.FrameCallback{
    private int width, height;
    private SurfaceHolder surfaceHolder;
    private long previousFrameNanos;
    private Canvas canvas;
    private Paint paint;
    private boolean touch;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        touch = false;

        surfaceHolder = getHolder();

        previousFrameNanos = System.nanoTime();

        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        Choreographer.getInstance().postFrameCallback(this);

        update((frameTimeNanos - previousFrameNanos)/1000000);
        draw();

        previousFrameNanos = frameTimeNanos;
    }

    private void update(long deltaTimeNanos) {

    }

    private void draw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null){
                synchronized (surfaceHolder) {
                    if (touch) {
                        canvas.drawRect(0, 0, width, height, paint);
                    }
                }
            }
        }
        finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        touch = true;

        return true;
    }
}
