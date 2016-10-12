package com.vanjav.roadlinepainter;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private ArrayList<PointF> linePoints, roadPoints;
    private int linePointToRemove, roadPointToRemove;
    private int width, height;
    private int crossTime;
    private Random random;

    public Controller (int width, int height) {
        linePoints = new ArrayList<PointF>();
        roadPoints = new ArrayList<PointF>();
        linePointToRemove = 0;
        roadPointToRemove = 0;
        this.width = width;
        this.height = height;
        crossTime = 5000;
        random = new Random();
        roadPoints.add(new PointF(width/2, 500));
        initRoad();
    }

    public ArrayList<PointF> getLinePoints() {
        return linePoints;
    }

    public ArrayList<PointF> getRoadPoints() {
        return roadPoints;
    }

    public void addLinePoint(float x, float y) {
        linePoints.add(new PointF(x, y));
    }

    public void addRoadPoint(float x, float y) {
        roadPoints.add(new PointF(x, y));
    }

    private void initRoad() {
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
        }
    }

    public boolean update(float deltaTimeNanos) {
        initRoad();

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

        PointF prevPoint, currPoint;
        float w = 200;
        float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y, lx, ly;
        float k, m;
        float aRoad, aPoint;

        lx = linePoints.get(linePoints.size()-1).x;
        ly = linePoints.get(linePoints.size()-1).y;

        for (int i = 1; i < roadPoints.size(); i++) {
            prevPoint = roadPoints.get(i-1);
            currPoint = roadPoints.get(i);

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

//            aRoad = (float) Math.abs(0.5*((p1x*p2y + p2x*p3y + p3x*p4y + p4x*p1y)-(p1y*p2x + p2y*p3x + p3y*p4x + p4y*p1x)));
//
//            aPoint = (float) Math.abs(0.5*((lx*p1y + p1x*p2y + p2x*ly)-(ly*p1x + p1y*p2x + p2y*lx)));
//            aPoint+= (float) Math.abs(0.5*((lx*p2y + p2x*p3y + p3x*ly)-(ly*p2x + p2y*p3x + p3y*lx)));
//            aPoint+= (float) Math.abs(0.5*((lx*p3y + p3x*p4y + p4x*ly)-(ly*p3x + p3y*p4x + p4y*lx)));
//            aPoint+= (float) Math.abs(0.5*((lx*p4y + p4x*p1y + p1x*ly)-(ly*p4x + p4y*p1x + p1y*lx)));
//
//            if (aPoint <= aRoad) return true;

            float a, b, c, d1, d2, d3, d4;

            a = -1*(p2y-p1y);
            b = p2x - p1x;
            c = -1*(a*p1x + b*p1y);
            d1 = a*lx + b*ly + c;

            a = -1*(p3y-p2y);
            b = p3x - p2x;
            c = -1*(a*p2x + b*p2y);
            d2 = a*lx + b*ly + c;

            a = -1*(p4y-p3y);
            b = p4x - p3x;
            c = -1*(a*p3x + b*p3y);
            d3 = a*lx + b*ly + c;

            a = -1*(p1y-p4y);
            b = p1x - p4x;
            c = -1*(a*p4x + b*p4y);
            d4 = a*lx + b*ly + c;

            if ((d1 >= 0 && d2 >= 0 && d3 >= 0 && d4 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0 && d4 <= 0)) return true;

            if (Math.pow(lx-prevPoint.x, 2)+Math.pow(ly-prevPoint.y, 2) <= Math.pow(100, 2)) return true;
        }
        
        return false;
    }
}
