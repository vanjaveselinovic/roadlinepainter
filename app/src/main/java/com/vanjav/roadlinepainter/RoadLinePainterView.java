package com.vanjav.roadlinepainter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by vveselin on 07/10/2016.
 */

public class RoadLinePainterView extends SurfaceView  implements Choreographer.FrameCallback {
    private int width, height;
    private SurfaceHolder surfaceHolder;
    private long previousFrameNanos;
    private Canvas canvas;
    private Paint paintBG, paintRoad, paintLine, paintTest;
    private boolean touch;
    private Controller controller;
    private float currX, currY;
    private boolean ready = false;
    private boolean paused = false;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintBG = new Paint();
        paintBG.setColor(ContextCompat.getColor(getContext(), R.color.colorGrass1));
        paintRoad = new Paint();
        paintRoad.setColor(ContextCompat.getColor(getContext(), R.color.colorRoad1));
        paintRoad.setStrokeWidth(200);
        paintRoad.setStrokeCap(Paint.Cap.ROUND);
        paintLine = new Paint();
        paintLine.setColor(ContextCompat.getColor(getContext(), R.color.colorLine1));
        paintLine.setStrokeWidth(20);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintTest = new Paint();
        paintTest.setColor(Color.RED);
        paintTest.setStrokeWidth(10);
        paintTest.setStyle(Paint.Style.STROKE);

        touch = false;
        surfaceHolder = getHolder();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        width = xNew;
        height = yNew;

        controller = new Controller(width, height);

        previousFrameNanos = System.nanoTime();
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void pause() {
        Choreographer.getInstance().removeFrameCallback(this);
        paused = true;
    }

    public void resume() {
        if (paused) {
            previousFrameNanos = System.nanoTime();
            Choreographer.getInstance().postFrameCallback(this);
            paused = false;
        }
    }

    public void gameOver() {
        Choreographer.getInstance().removeFrameCallback(this);
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

        if (!controller.update(deltaTimeNanos)) gameOver();
    }

    private void draw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null){
                synchronized (surfaceHolder) {
                    canvas.drawRect(0, 0, width, height, paintBG);

                    PointF prevPoint, currPoint;
                    /*
                    float w = 200;
                    float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;
                    float k, m;
                    */

                    for (int i = 1; i < controller.getRoadPoints().size(); i++) {
                        prevPoint = controller.getRoadPoints().get(i-1);
                        currPoint = controller.getRoadPoints().get(i);
                        canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintRoad);

                        /*
                        if (currPoint.y-prevPoint.y == 0) {
                            p1x = prevPoint.x;
                            p1y = prevPoint.y - w/2;
                            p2x = currPoint.x;
                            p2y = currPoint.y - w/2;
                            p3x = currPoint.x;
                            p3y = currPoint.y + w/2;
                            p4x = prevPoint.x;
                            p4y = prevPoint.y + w/2;
                        }
                        else {
                            m = (float) -1 * ((currPoint.x - prevPoint.x) / (currPoint.y - prevPoint.y));
                            k = (float) (w / (2 * Math.sqrt(1 + Math.pow(m, 2))));
                            p1x = prevPoint.x + k;
                            p1y = prevPoint.y + k * m;
                            p2x = currPoint.x + k;
                            p2y = currPoint.y + k * m;
                            p3x = currPoint.x - k;
                            p3y = currPoint.y - k * m;
                            p4x = prevPoint.x - k;
                            p4y = prevPoint.y - k * m;
                        }
                        canvas.drawLine(p1x, p1y, p2x, p2y, paintTest);
                        canvas.drawLine(p2x, p2y, p3x, p3y, paintTest);
                        canvas.drawLine(p3x, p3y, p4x, p4y, paintTest);
                        canvas.drawLine(p4x, p4y, p1x, p1y, paintTest);
                        canvas.drawCircle(currPoint.x, currPoint.y, w/2, paintTest);
                        */
                    }

                    for (int i = 1; i < controller.getLinePoints().size(); i++) {
                        prevPoint = controller.getLinePoints().get(i-1);
                        currPoint = controller.getLinePoints().get(i);
                        canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintLine);
                    }

                    if (touch && controller.getLinePoints().size() > 0)
                        canvas.drawLine(controller.getLinePoints().get(controller.getLinePoints().size() - 1).x, controller.getLinePoints().get(controller.getLinePoints().size() - 1).y, currX, currY, paintLine);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touch = true;
            previousFrameNanos = System.nanoTime();
            Choreographer.getInstance().postFrameCallback(this);
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            touch = false;
            gameOver();
        }

        currX = event.getX();
        currY = event.getY();

        return true;
    }
}
