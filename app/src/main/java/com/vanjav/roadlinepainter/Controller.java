package com.vanjav.roadlinepainter;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private ArrayList<Point> points;
    private int pointToRemove;
    private int width;
    private int crossTime;

    public Controller (int width) {
        points = new ArrayList<Point>();
        pointToRemove = 0;
        this.width = width;
        crossTime = 5000;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void addPoint(int x, int y) {
        points.add(new Point(x, y));
    }

    public void update(float deltaTimeNanos) {
        if (points.size() > 0 && points.get(0).x < -1*width)
            points.subList(0, pointToRemove).clear();

        for (int i = 0; i < points.size(); i++) {
            points.get(i).offset((int) (-1*width*(deltaTimeNanos/crossTime)), 0);
            if (points.get(i).x < -1*width) pointToRemove = i;
        }
    }
}
