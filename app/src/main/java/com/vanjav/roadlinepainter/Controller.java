package com.vanjav.roadlinepainter;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private ArrayList<Point> linePoints, roadPoints;
    private int linePointToRemove, roadPointToRemove;
    private int width, height;
    private int crossTime;
    private Random random;

    public Controller (int width, int height) {
        linePoints = new ArrayList<Point>();
        roadPoints = new ArrayList<Point>();
        linePointToRemove = 0;
        roadPointToRemove = 0;
        this.width = width;
        this.height = height;
        crossTime = 5000;
        random = new Random();
        roadPoints.add(new Point(width/2, 500));
        initRoad();
    }

    public ArrayList<Point> getLinePoints() {
        return linePoints;
    }

    public ArrayList<Point> getRoadPoints() {
        return roadPoints;
    }

    public void addLinePoint(int x, int y) {
        linePoints.add(new Point(x, y));
    }

    public void addRoadPoint(int x, int y) {
        roadPoints.add(new Point(x, y));
    }

    private void initRoad() {
        Point lastPointInRoad = roadPoints.get(roadPoints.size()-1);
        int plusMinus = 0, x, y;

        while(lastPointInRoad.x < width*2) {
            plusMinus = random.nextInt(2);
            if (plusMinus == 0) plusMinus = -1;
            x = random.nextInt(lastPointInRoad.x+500-lastPointInRoad.x)+lastPointInRoad.x;
            y = plusMinus*random.nextInt(lastPointInRoad.y+250-lastPointInRoad.y)+lastPointInRoad.y;
            if (y < 100) y = 100;
            if (y > height - 100) y = height - 100;
            roadPoints.add(new Point(x, y));
            lastPointInRoad = roadPoints.get(roadPoints.size()-1);
        }
    }

    public void update(float deltaTimeNanos) {
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
    }
}
