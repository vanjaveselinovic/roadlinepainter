package com.vanjav.roadlinepainter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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
    private Paint paintBG, paintRoad, paintLine;
    private boolean touch;
    private Controller controller;
    private int currX, currY;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        controller = new Controller(width);

        paintBG = new Paint();
        paintBG.setColor(ContextCompat.getColor(getContext(), R.color.colorGrass1));
        paintRoad = new Paint();
        paintRoad.setColor(ContextCompat.getColor(getContext(), R.color.colorRoad1));
        paintRoad.setStrokeWidth(100);
        paintRoad.setStrokeCap(Paint.Cap.ROUND);
        paintLine = new Paint();
        paintLine.setColor(ContextCompat.getColor(getContext(), R.color.colorLine1));
        paintLine.setStrokeWidth(10);

        touch = false;

        surfaceHolder = getHolder();

        previousFrameNanos = System.nanoTime();

        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        Choreographer.getInstance().postFrameCallback(this);

        draw();
        update((frameTimeNanos - previousFrameNanos)/1000000);

        previousFrameNanos = frameTimeNanos;
    }

    private void update(long deltaTimeNanos) {
        if (touch) {
            controller.addLinePoint(currX, currY);
        }

        controller.update(deltaTimeNanos);
    }

    private void draw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null){
                synchronized (surfaceHolder) {
                    canvas.drawRect(0, 0, width, height, paintBG);

                    Point prevPoint, currPoint;

                    for (int i = 1; i < controller.getRoadPoints().size(); i++) {
                        prevPoint = controller.getRoadPoints().get(i-1);
                        currPoint = controller.getRoadPoints().get(i);
                        canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintRoad);
                    }

                    for (int i = 1; i < controller.getLinePoints().size(); i++) {
                        prevPoint = controller.getLinePoints().get(i-1);
                        currPoint = controller.getLinePoints().get(i);
                        canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintLine);
                    }
                    if (touch && controller.getLinePoints().size() > 0) {
                        float newX = controller.getLinePoints().get(controller.getLinePoints().size() - 1).x;
                        float newY = controller.getLinePoints().get(controller.getLinePoints().size() - 1).y;
                        canvas.drawLine(newX, newY, currX, currY, paintLine);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            touch = true;
        else if (event.getAction() == MotionEvent.ACTION_UP)
            touch = false;

        currX = (int) event.getX();
        currY = (int) event.getY();

        return true;
    }
}
