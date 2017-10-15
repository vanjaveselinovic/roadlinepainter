package com.vanjav.roadlinepainter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private LinkedList<PointF> linePoints, roadPoints;
    private int linePointToRemove, roadPointToRemove;
    private int width, height;
    private int crossTime;
    private Random random;
    private float score;
    private int length;

    /* zone 1 */
    private LinkedList<PointF> flowerPoints, treePoints;
    private int flowerPointToRemove, treePointToRemove;

    public Controller (int width, int height, Context context) {
        linePoints = new LinkedList<PointF>();
        roadPoints = new LinkedList<PointF>();
        linePointToRemove = 0;
        roadPointToRemove = 0;
        this.width = width;
        this.height = height;
        crossTime = 1000; //1 second
        random = new Random();
        score = 0;

        /* zone 1* */
        flowerPoints = new LinkedList<PointF>();
        treePoints = new LinkedList<PointF>();

        initRoad();
        genRoad();
    }

    public LinkedList<PointF> getLinePoints() {
        return linePoints;
    }

    public LinkedList<PointF> getRoadPoints() {
        return roadPoints;
    }

    public LinkedList<PointF> getFlowerPoints() {
        return flowerPoints;
    }

    public LinkedList<PointF> getTreePoints() {
        return treePoints;
    }

    public void addLinePoint(float x, float y) {
        linePoints.add(new PointF(x, y));
    }

    public void addRoadPoint(float x, float y) {
        roadPoints.add(new PointF(x, y));
    }

    public float getScore() {
        return score;
    }

    public void updateScore(float x) {
        score += x/1000.0;
    }

    public void initRoad() {
        roadPoints.add(new PointF(width/2, height/2));
        roadPoints.add(new PointF(width+width/2, height/2));
    }

    public void genRoad() {
        PointF lastPointInRoad = roadPoints.get(roadPoints.size()-1);
        float plusMinus = 0, x, y;

        while(lastPointInRoad.x < width*2) {
            plusMinus = random.nextInt(2);
            if (plusMinus == 0) plusMinus = -1;
            x = random.nextFloat() * (lastPointInRoad.x+500-lastPointInRoad.x) + lastPointInRoad.x;
            y = plusMinus*random.nextFloat() * (lastPointInRoad.y+250-lastPointInRoad.y)+lastPointInRoad.y;
            if (y < 100) y = 100;
            if (y > height - 100) y = height - 100;
            roadPoints.add(new PointF(x, y));
            lastPointInRoad = roadPoints.get(roadPoints.size()-1);
            genItems(x, y);
        }
    }

    public void genItems(float x, float y) {
        int numItemsToAdd = random.nextInt(7);

        for (int i = 0; i < numItemsToAdd; i++) {
            flowerPoints.add(new PointF(
                    x-250 + random.nextFloat()*500,
                    y+300 + random.nextFloat()*250
            ));
        }

        numItemsToAdd = random.nextInt(7);

        for (int i = 0; i < numItemsToAdd; i++) {
            flowerPoints.add(new PointF(
                    x-250 + random.nextFloat()*500,
                    y-300 - random.nextFloat()*250
            ));
        }

        numItemsToAdd = random.nextInt(3);

        for (int i = 0; i < numItemsToAdd; i++) {
            treePoints.add(new PointF(
                    x-500 + random.nextFloat()*1000,
                    y+600 + random.nextFloat()*250
            ));
        }

        numItemsToAdd = random.nextInt(3);

        for (int i = 0; i < numItemsToAdd; i++) {
            treePoints.add(new PointF(
                    x-500 + random.nextFloat()*1000,
                    y-600 - random.nextFloat()*250
            ));
        }
    }

    public boolean update(float deltaTimeNanos) {
        genRoad();

        updateScore((float) width*(deltaTimeNanos/crossTime));

        if (roadPoints.size() > 0 && roadPoints.get(0).x < -1*width)
            roadPoints.subList(0, roadPointToRemove).clear();

        for (int i = 0; i < roadPoints.size(); i++) {
            roadPoints.get(i).offset((int) (-1*width*(deltaTimeNanos/crossTime)), 0);
            if (roadPoints.get(i).x < -1*width) roadPointToRemove = i;
        }

        if (linePoints.size() > 0 && linePoints.get(0).x < -1*width)
            linePoints.subList(0, linePointToRemove).clear();

        for (int i = 0; i < linePoints.size(); i++) {
            linePoints.get(i).offset((int) (-1*width*(deltaTimeNanos/crossTime)), 0);
            if (linePoints.get(i).x < -1*width) linePointToRemove = i;
        }

        if (flowerPoints.size() > 0) {
            flowerPoints.subList(0, flowerPointToRemove).clear();
        }

        for (int i = 0; i < flowerPoints.size(); i++) {
            flowerPoints.get(i).offset((int) (-1*width*(deltaTimeNanos/crossTime)), 0);
            if (flowerPoints.get(i).x < -1*width) flowerPointToRemove = i;
        }

        if (treePoints.size() > 0) {
            treePoints.subList(0, treePointToRemove).clear();
        }

        for (int i = 0; i < treePoints.size(); i++) {
            treePoints.get(i).offset((int) (-1*width*(deltaTimeNanos/crossTime)), 0);
            if (treePoints.get(i).x < -1*width) treePointToRemove = i;
        }

        PointF prevPoint, currPoint;
        float w = 200;
        float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y, lx, ly;
        float k, m;
        float aRoad, aPoint;

        if (linePoints.size() > 0) {
            lx = linePoints.get(linePoints.size() - 1).x;
            ly = linePoints.get(linePoints.size() - 1).y;

            for (int i = 1; i < roadPoints.size(); i++) {
                prevPoint = roadPoints.get(i - 1);
                currPoint = roadPoints.get(i);

                if (currPoint.y - prevPoint.y == 0) {
                    p1x = prevPoint.x;
                    p1y = prevPoint.y - w / 2;
                    p2x = currPoint.x;
                    p2y = currPoint.y - w / 2;
                    p3x = currPoint.x;
                    p3y = currPoint.y + w / 2;
                    p4x = prevPoint.x;
                    p4y = prevPoint.y + w / 2;
                } else {
                    m = -1 * ((currPoint.x - prevPoint.x) / (currPoint.y - prevPoint.y));
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

                float a, b, c, d1, d2, d3, d4;

                a = -1 * (p2y - p1y);
                b = p2x - p1x;
                c = -1 * (a * p1x + b * p1y);
                d1 = a * lx + b * ly + c;

                a = -1 * (p3y - p2y);
                b = p3x - p2x;
                c = -1 * (a * p2x + b * p2y);
                d2 = a * lx + b * ly + c;

                a = -1 * (p4y - p3y);
                b = p4x - p3x;
                c = -1 * (a * p3x + b * p3y);
                d3 = a * lx + b * ly + c;

                a = -1 * (p1y - p4y);
                b = p1x - p4x;
                c = -1 * (a * p4x + b * p4y);
                d4 = a * lx + b * ly + c;

                if ((d1 >= 0 && d2 >= 0 && d3 >= 0 && d4 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0 && d4 <= 0))
                    return true;

                if (Math.pow(lx - prevPoint.x, 2) + Math.pow(ly - prevPoint.y, 2) <= Math.pow(100, 2))
                    return true;
            }
        }
        
        return false;
    }
}
