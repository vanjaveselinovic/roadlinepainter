package com.vanjav.roadlinepainter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by vveselin on 07/10/2016.
 */

public class RoadLinePainterView extends GLSurfaceView implements GLSurfaceView.Renderer, Choreographer.FrameCallback {
    private int width, height;
    private SurfaceHolder surfaceHolder;
    private long previousFrameNanos;
    private Canvas canvas;
    private Paint paintBG, paintRoad, paintOutline, paintLine, paintText, paintZone1Shrub;
    private int colorBG, colorRoad, colorOutline, colorLine;
    private Bitmap zone1SmallShrub1, zone1BigShrub1, zone1Base;
    private boolean touch;
    private Controller controller;
    private float currX, currY;
    private boolean paused = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean pausedBeforeStarting = false;

    private Bitmap bitmapL, bitmapR;
    private Canvas canvasL, canvasR;

    private Draw draw;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(50f);
        paintText.setTextAlign(Paint.Align.CENTER);

        /* zone 1 */
        paintBG = new Paint();
        paintBG.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Grass2));

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

        colorBG = ContextCompat.getColor(getContext(), R.color.colorZone1Grass1);
        colorRoad = ContextCompat.getColor(getContext(), R.color.colorZone1Road);
        colorOutline = ContextCompat.getColor(getContext(), R.color.colorZone1Grass2);
        colorLine = ContextCompat.getColor(getContext(), R.color.colorZone1Line);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        zone1SmallShrub1 = BitmapFactory.decodeResource(getResources(), R.drawable.zone1smallshrub1, options);
        zone1BigShrub1 = BitmapFactory.decodeResource(getResources(), R.drawable.zone1bigshrub1, options);
        zone1Base = BitmapFactory.decodeResource(getResources(), R.drawable.zone1base, options);

        touch = false;
        //surfaceHolder = getHolder();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        width = xNew;
        height = yNew;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bitmapL = Bitmap.createBitmap(width, height, conf);
        bitmapR = Bitmap.createBitmap(width, height, conf);
        canvasL = new Canvas(bitmapL);
        canvasR = new Canvas(bitmapR);

        controller = new Controller(width, height, getContext());

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

        requestRender();
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

    /*
    public void drawStuff(Canvas canvas) {
        if (controller.getCanvasLpos() <= -1*width) {
            controller.setCanvasLpos(0);
            controller.setCanvasRpos(width);

            drawOnCanvas(canvasL, 0);
            drawOnCanvas(canvasR, width);
        }
        canvas.drawBitmap(bitmapL, controller.getCanvasLpos(), 0, null);
        canvas.drawBitmap(bitmapR, controller.getCanvasRpos(), 0, null);

        PointF prevPoint, currPoint;

        for (int i = 1; i < controller.getLinePoints().size(); i++) {
            prevPoint = controller.getLinePoints().get(i-1);
            currPoint = controller.getLinePoints().get(i);
            canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintLine);
        }

        canvas.drawText(""+Math.round(controller.getScore()*10.0)/10.0, width/2, 50, paintText);
    }

    public void drawOnCanvas(Canvas canvas, float offset) {
        canvas.drawRect(0, 0, width, height, paintBG);

        PointF prevPoint, currPoint;

        for (int i = 1; i < controller.getRoadPoints().size(); i++) {
            prevPoint = controller.getRoadPoints().get(i-1);
            currPoint = controller.getRoadPoints().get(i);
            canvas.drawBitmap(zone1Base, currPoint.x - zone1Base.getWidth()/2 - offset, currPoint.y - zone1Base.getHeight()/2, null);
        }

        for (int i = 1; i < controller.getRoadPoints().size(); i++) {
            prevPoint = controller.getRoadPoints().get(i-1);
            currPoint = controller.getRoadPoints().get(i);
            canvas.drawLine(prevPoint.x - offset, prevPoint.y, currPoint.x - offset, currPoint.y, paintOutline);
        }

        for (int i = 1; i < controller.getFlowerPoints().size(); i++) {
            currPoint = controller.getFlowerPoints().get(i);
            canvas.drawPoint(currPoint.x - offset, currPoint.y, paintLine);
        }

        for (int i = 1; i < controller.getRoadPoints().size(); i++) {
            prevPoint = controller.getRoadPoints().get(i-1);
            currPoint = controller.getRoadPoints().get(i);
            canvas.drawLine(prevPoint.x - offset, prevPoint.y, currPoint.x - offset, currPoint.y, paintRoad);
        }

        for (int i =1; i < controller.getSmallShrubPoints().size(); i++) {
            currPoint = controller.getSmallShrubPoints().get(i);
            canvas.drawBitmap(zone1SmallShrub1, currPoint.x - zone1SmallShrub1.getWidth()/2 - offset, currPoint.y - zone1SmallShrub1.getHeight()/2, null);
        }

        for (int i =1; i < controller.getBigShrubPoints().size(); i++) {
            currPoint = controller.getBigShrubPoints().get(i);
            canvas.drawBitmap(zone1BigShrub1, currPoint.x - zone1BigShrub1.getWidth()/2 - offset, currPoint.y - zone1BigShrub1.getHeight()/2, null);
        }
    }
    */

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

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        draw = new Draw(width, height);
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        draw.clearLoaded();
        draw.loadRectangle(new PointF(0, 0), new PointF(width, 0), new PointF(0, height), new PointF(width, height), colorBG);
        draw.loadMultiLine(controller.getRoadPoints(), 350, colorOutline);
        draw.loadMultiLine(controller.getRoadPoints(), 200, colorRoad);
        draw.loadMultiLine(controller.getLinePoints(), 20, colorLine);
        draw.drawLoaded();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
}
