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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by vveselin on 07/10/2016.
 */

public class RoadLinePainterView extends SurfaceView  implements Choreographer.FrameCallback {
    private int width, height;
    private SurfaceHolder surfaceHolder;
    private long previousFrameNanos;
    private Canvas canvas;
    private Paint paintBG, paintRoad, paintOutline, paintLine, paintText, paintZone1Shrub;
    private boolean touch;
    private Controller controller;
    private float currX, currY;
    private boolean paused = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean pausedBeforeStarting = false;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(50f);
        paintText.setTextAlign(Paint.Align.CENTER);

        /* zone 1 */
        paintBG = new Paint();
        paintBG.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Grass1));

        paintRoad = new Paint();
        paintRoad.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Road));
        paintRoad.setStrokeWidth(200);
        paintRoad.setStrokeCap(Paint.Cap.ROUND);

        paintOutline = new Paint();
        paintOutline.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Grass2));
        paintOutline.setStrokeWidth(350);
        paintOutline.setStrokeCap(Paint.Cap.ROUND);

        paintLine = new Paint();
        paintLine.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Line));
        paintLine.setStrokeWidth(20);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        paintZone1Shrub = new Paint();
        paintZone1Shrub.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Bushes));

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
        paused = true;
        if (!gameStarted) pausedBeforeStarting = true;
        gameOver();
    }

    public void resume() {
        if (paused) {
            paused = false;
            previousFrameNanos = System.nanoTime();
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    public void gameOver() {
        Choreographer.getInstance().removeFrameCallback(this);
        if (gameStarted) {
            gameOver = true;
            View view = ((View) getParent()).findViewById(R.id.game_over_container);
            view.setVisibility(VISIBLE);
            view.setAlpha(0.0f);
            view.animate().setDuration(200).alpha(1.0f);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        Choreographer.getInstance().postFrameCallback(this);

        draw();
        update((frameTimeNanos - previousFrameNanos)/1000000);

        previousFrameNanos = frameTimeNanos;
    }

    private void update(long deltaTimeNanos) {
        if (!gameOver && !pausedBeforeStarting) {
            if (touch) {
                controller.addLinePoint(currX, currY);
            }

            if (!controller.update(deltaTimeNanos))
                gameOver();
        }
        pausedBeforeStarting = false;
    }

    private void draw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null){
                synchronized (surfaceHolder) {
                    canvas.drawRect(0, 0, width, height, paintBG);

                    PointF prevPoint, currPoint;

                    for (int i = 1; i < controller.getRoadPoints().size(); i++) {
                        prevPoint = controller.getRoadPoints().get(i-1);
                        currPoint = controller.getRoadPoints().get(i);
                        canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintOutline);
                    }

                    for (int i = 1; i < controller.getFlowerPoints().size(); i++) {
                        currPoint = controller.getFlowerPoints().get(i);
                        canvas.drawPoint(currPoint.x, currPoint.y, paintLine);
                    }

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

                    for (int i =1; i < controller.getSmallShrubPoints().size(); i++) {
                        currPoint = controller.getSmallShrubPoints().get(i);
                        canvas.drawCircle(currPoint.x, currPoint.y, 100, paintZone1Shrub);
                    }

                    for (int i =1; i < controller.getBigShrubPoints().size(); i++) {
                        currPoint = controller.getBigShrubPoints().get(i);
                        canvas.drawCircle(currPoint.x, currPoint.y, 250, paintZone1Shrub);
                    }

                    if (touch && controller.getLinePoints().size() > 0)
                        canvas.drawLine(controller.getLinePoints().get(controller.getLinePoints().size() - 1).x, controller.getLinePoints().get(controller.getLinePoints().size() - 1).y, currX, currY, paintLine);

                    canvas.drawText(""+Math.round(controller.getScore()*10.0)/10.0, width/2, 50, paintText);
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
        if (gameOver) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            gameStarted = true;
            touch = true;
            previousFrameNanos = System.nanoTime();
            Choreographer.getInstance().postFrameCallback(this);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            touch = false;
            gameOver();
        }

        currX = event.getX();
        currY = event.getY();

        return true;
    }
}
