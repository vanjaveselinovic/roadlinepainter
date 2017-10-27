package com.vanjav.roadlinepainter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by vveselin on 07/10/2016.
 */

public class RoadLinePainterView extends SurfaceView  implements Choreographer.FrameCallback {
    private SurfaceHolder surfaceHolder;
    private Controller controller;
    private Choreographer choreographer = Choreographer.getInstance();

    private int width, height;
    private long previousFrameNanos;
    private float currX, currY;
    private float roadWidth, lineWidth, outlineWidth;

    private boolean paused = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;

    private PointF prevPoint, currPoint;
    private int i;

    public RoadLinePainterView(Context context) {
        this(context, null);
    }

    public RoadLinePainterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        width = xNew;
        height = yNew;

        controller = new Controller(width, height, getContext());

        roadWidth = controller.getRoadWidth();
        lineWidth = controller.getLineWidth();
        outlineWidth = controller.getOutlineWidth();
        initPaint();

        previousFrameNanos = System.nanoTime();
        choreographer.postFrameCallback(this);
    }

    private Paint paintBG, paintRoad, paintOutline, paintLine, paintText, paintStroke, paintDebug;
    private Bitmap zone1tree;
    private LinkedList<Bitmap> zone1trees;

    private void initPaint() {
        paintText = new Paint();
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Line));
        paintText.setTextSize(150f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setFakeBoldText(true);

        paintStroke = new Paint();
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(20);
        paintStroke.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Road));
        paintStroke.setTextSize(150f);
        paintStroke.setTextAlign(Paint.Align.CENTER);
        paintStroke.setFakeBoldText(true);

        paintDebug = new Paint();
        paintDebug.setColor(Color.BLACK);
        paintDebug.setTextSize(50f);

        /* zone 1 */
        paintBG = new Paint();
        paintBG.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Grass1));

        paintRoad = new Paint();
        paintRoad.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Road));
        paintRoad.setStrokeWidth(roadWidth);
        paintRoad.setStrokeCap(Paint.Cap.ROUND);

        paintOutline = new Paint();
        paintOutline.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Grass2));
        paintOutline.setStrokeWidth(outlineWidth);
        paintOutline.setStrokeCap(Paint.Cap.ROUND);

        paintLine = new Paint();
        paintLine.setColor(ContextCompat.getColor(getContext(), R.color.colorZone1Line));
        paintLine.setStrokeWidth(lineWidth);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        zone1tree = BitmapFactory.decodeResource(getResources(), R.drawable.zone1tree, options);

        zone1trees = new LinkedList<Bitmap>();

        for (i = 0; i < 5; i++) {
            zone1trees.add(Bitmap.createScaledBitmap(
                    zone1tree,
                    100 + 50*i,
                    (int) ((100 + 50*i) * ((float) zone1tree.getHeight() / (float) zone1tree.getWidth())),
                    false
            ));
        }
    }

    public void pause() {
        paused = true;
        choreographer.removeFrameCallback(this);
        gameOver();
    }

    public void resume() {
        if (paused) {
            paused = false;
            previousFrameNanos = System.nanoTime();
            choreographer.postFrameCallback(this);
        }
    }

    public void gameOver() {
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
        choreographer.postFrameCallback(this);

        update((frameTimeNanos - previousFrameNanos)/1000000);
        draw();

        previousFrameNanos = frameTimeNanos;
    }

    private void update(long deltaTimeMillis) {
        if (gameStarted && !gameOver) {
            controller.addLinePoint(currX, currY);

            if (!controller.update(deltaTimeMillis)) {
                gameOver();
            }
        }
    }

    private Canvas canvas;
    private Bitmap currTree;
    private String score;

    private void draw() {
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null){
                synchronized (surfaceHolder) {
                    canvas.drawRect(0, 0, width, height, paintBG);

                    for (i = 1; i < controller.getRoadPoints().size(); i++) {
                        prevPoint = controller.getRoadPoints().get(i-1);
                        currPoint = controller.getRoadPoints().get(i);
                        if (prevPoint.x < width + outlineWidth)
                            canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintOutline);
                    }

                    for (i = 1; i < controller.getFlowerPoints().size(); i++) {
                        currPoint = controller.getFlowerPoints().get(i);
                        if (currPoint.x < width + lineWidth)
                            canvas.drawPoint(currPoint.x, currPoint.y, paintLine);
                    }

                    for (i = 1; i < controller.getRoadPoints().size(); i++) {
                        prevPoint = controller.getRoadPoints().get(i-1);
                        currPoint = controller.getRoadPoints().get(i);
                        if (prevPoint.x < width + roadWidth)
                            canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintRoad);
                    }

                    for (i = 1; i < controller.getLinePoints().size(); i++) {
                        prevPoint = controller.getLinePoints().get(i-1);
                        currPoint = controller.getLinePoints().get(i);
                        if (prevPoint.x < width + lineWidth)
                            canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paintLine);
                    }

                    for (i = 1; i < controller.getTreePoints().size(); i++) {
                        currPoint = controller.getTreePoints().get(i);
                        currTree = zone1trees.get(((TreePointF) currPoint).size);

                        if (currPoint.x < width + currTree.getWidth())
                            canvas.drawBitmap(currTree, currPoint.x - currTree.getWidth()/2, currPoint.y - currTree.getHeight(), null);
                    }

                    score = ""+Math.round(controller.getScore()*10.0)/10.0;

                    canvas.drawText(score, width/2 + 2, 200 + 2, paintStroke);
                    canvas.drawText(score, width/2, 200, paintText);

                    /* debug
                    canvas.drawText("line points: "+controller.getLinePoints().size(), 50, 50, paintDebug);
                    canvas.drawText("road points: "+controller.getRoadPoints().size(), 50, 100, paintDebug);
                    canvas.drawText("flwr points: "+controller.getFlowerPoints().size(), 50, 150, paintDebug);
                    canvas.drawText("tree points: "+controller.getTreePoints().size(), 50, 200, paintDebug);
                    */
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
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            gameOver();
        }

        currX = event.getX();
        currY = event.getY();

        return true;
    }
}
