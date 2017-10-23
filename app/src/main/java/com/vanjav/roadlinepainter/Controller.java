package com.vanjav.roadlinepainter;

import android.content.Context;
import android.graphics.PointF;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private LinkedList<PointF> linePoints, roadPoints;
    private float score;

    private int width, height;
    private int crossTime;
    private float lineWidth, roadWidth;

    private Random random;
    private int i;

    /* zone 1 */
    private LinkedList<PointF> flowerPoints, treePoints;
    private LinkedList<Integer> treeSizes;
    private int flowerPointToRemove;
    private LinkedList<Integer> treePointsToRemove;


    public Controller (int width, int height, Context context) {
        linePoints = new LinkedList<PointF>();
        roadPoints = new LinkedList<PointF>();
        linePointToRemove = 0;
        roadPointToRemove = 0;
        score = 0;

        this.width = width;
        this.height = height;
        crossTime = 1000; //1 second
        lineWidth = 20;
        roadWidth = 200;

        random = new Random();

        /* zone 1* */
        flowerPoints = new LinkedList<PointF>();
        treePoints = new LinkedList<PointF>();
        treeSizes = new LinkedList<Integer>();
        treePointsToRemove = new LinkedList<Integer>();

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

    public LinkedList<Integer> getTreeSizes() {
        return treeSizes;
    }

    public void addLinePoint(float x, float y) {
        linePoints.add(new PointF(x, y));
    }

    public float getScore() {
        return score;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public float getRoadWidth() {
        return roadWidth;
    }

    private void updateScore(float x) {
        score += x/1000.0;
    }

    private void initRoad() {
        roadPoints.add(new PointF(0, height/2));
        genItems(0, height/2);

        roadPoints.add(new PointF(width/5, height/2));
        genItems(width/5, height/2);

        roadPoints.add(new PointF(2*width/5, height/2));
        genItems(2*width/5, height/2);

        roadPoints.add(new PointF(3*width/5, height/2));
        genItems(3*width/5, height/2);

        roadPoints.add(new PointF(4*width/5, height/2));
        genItems(4*width/5, height/2);

        roadPoints.add(new PointF(width, height/2));
        genItems(width, height/2);

        roadPoints.add(new PointF(width+width/2, height/2));
        genItems(width+width/2, height/2);
    }

    private PointF lastPointInRoad;
    private float plusMinus, genX, genY;

    private void genRoad() {
        lastPointInRoad = roadPoints.getLast();
        plusMinus = 0;

        while(lastPointInRoad.x < width*2) {
            plusMinus = random.nextInt(2);

            if (plusMinus == 0)
                plusMinus = -1;

            genX = random.nextFloat() * (lastPointInRoad.x+500-lastPointInRoad.x) + lastPointInRoad.x;
            genY = plusMinus*random.nextFloat() * (lastPointInRoad.y+250-lastPointInRoad.y)+lastPointInRoad.y;

            if (genY < 100)
                genY = 100;
            if (genY > height - 100)
                genY = height - 100;

            roadPoints.add(new PointF(genX, genY));
            lastPointInRoad = roadPoints.getLast();

            genItems(genX, genY);
        }
    }

    private int left, right;

    private int binarySearchPointFList(LinkedList<PointF> list, float y) {
        if (list.isEmpty()) return 0;

        left = 0;
        right = list.size() - 1;

        while (right != left) {
            if (y > list.get(left + (right-left)/2).y)
                left = left + (right-left)/2 + 1;
            else
                right = left + (right-left)/2;
        }

        if (y > list.get(left).y) return left+1;
        else return left;
    }

    private float addX, addY;
    private int numItemsToAdd;
    private int positionToInsert;

    private void genItems(float x, float y) {
        numItemsToAdd = random.nextInt(7);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-250 + random.nextFloat()*500;
            addY = y+300 + random.nextFloat()*250;

            //if (!isPointOnRoad(addX, addY))
                flowerPoints.add(new PointF(addX, addY));
        }

        numItemsToAdd = random.nextInt(7);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-250 + random.nextFloat()*500;
            addY = y-300 - random.nextFloat()*250;

            //if (!isPointOnRoad(addX, addY))
                flowerPoints.add(new PointF(addX, addY));
        }

        numItemsToAdd = random.nextInt(3);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-width/2 + random.nextFloat()*width/2;
            addY = (float) (y+roadWidth/2 + random.nextFloat()*(1.1*height-y));

            if (!isPointOnRoad(addX, addY)) {
                positionToInsert = binarySearchPointFList(treePoints, addY);
                treePoints.add(positionToInsert, new PointF(addX, addY));
                treeSizes.add(positionToInsert, random.nextInt(5));
            }
        }

        numItemsToAdd = random.nextInt(3);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-width/2 + random.nextFloat()*width/2;
            addY = y-roadWidth/2 - random.nextFloat()*y;

            if (!isPointOnRoad(addX, addY)) {
                positionToInsert = binarySearchPointFList(treePoints, addY);
                treePoints.add(positionToInsert, new PointF(addX, addY));
                treeSizes.add(positionToInsert, random.nextInt(5));
            }
        }
    }

    private PointF prevPoint, currPoint;
    private float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;
    private float k, m;

    private boolean isPointOnRoad(float x, float y) {
        for (i = 1; i < roadPoints.size(); i++) {
            prevPoint = roadPoints.get(i - 1);
            currPoint = roadPoints.get(i);

            if (currPoint.y - prevPoint.y == 0) {
                p1x = prevPoint.x;
                p1y = prevPoint.y - roadWidth / 2;
                p2x = currPoint.x;
                p2y = currPoint.y - roadWidth / 2;
                p3x = currPoint.x;
                p3y = currPoint.y + roadWidth / 2;
                p4x = prevPoint.x;
                p4y = prevPoint.y + roadWidth / 2;
            } else {
                m = -1 * ((currPoint.x - prevPoint.x) / (currPoint.y - prevPoint.y));
                k = (float) (roadWidth / (2 * Math.sqrt(1 + Math.pow(m, 2))));
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
            d1 = a * x + b * y + c;

            a = -1 * (p3y - p2y);
            b = p3x - p2x;
            c = -1 * (a * p2x + b * p2y);
            d2 = a * x + b * y + c;

            a = -1 * (p4y - p3y);
            b = p4x - p3x;
            c = -1 * (a * p3x + b * p3y);
            d3 = a * x + b * y + c;

            a = -1 * (p1y - p4y);
            b = p1x - p4x;
            c = -1 * (a * p4x + b * p4y);
            d4 = a * x + b * y + c;

            if ((d1 >= 0 && d2 >= 0 && d3 >= 0 && d4 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0 && d4 <= 0))
                return true;

            if (Math.pow(x - prevPoint.x, 2) + Math.pow(y - prevPoint.y, 2) <= Math.pow(100, 2))
                return true;
        }

        return false;
    }

    private int linePointToRemove, roadPointToRemove;

    public boolean update(float deltaTimeMillis) {
        genRoad();

        updateScore((float) width*(deltaTimeMillis/crossTime));

        if (roadPoints.size() > 0 && roadPoints.get(0).x < -1*width)
            roadPoints.subList(0, roadPointToRemove).clear();

        for (i = 0; i < roadPoints.size(); i++) {
            roadPoints.get(i).offset((int) (-1*width*(deltaTimeMillis/crossTime)), 0);
            if (roadPoints.get(i).x < -1*width) roadPointToRemove = i;
        }

        if (linePoints.size() > 0 && linePoints.get(0).x < -1*width)
            linePoints.subList(0, linePointToRemove).clear();

        for (i = 0; i < linePoints.size(); i++) {
            linePoints.get(i).offset((int) (-1*width*(deltaTimeMillis/crossTime)), 0);
            if (linePoints.get(i).x < -1*width) linePointToRemove = i;
        }

        if (flowerPoints.size() > 0) {
            flowerPoints.subList(0, flowerPointToRemove).clear();
        }

        for (i = 0; i < flowerPoints.size(); i++) {
            flowerPoints.get(i).offset((int) (-1*width*(deltaTimeMillis/crossTime)), 0);
            if (flowerPoints.get(i).x < -1*width) flowerPointToRemove = i;
        }

        if (treePoints.size() > 0) {
            for (i = 0; i < treePointsToRemove.size(); i++) {
                treePoints.remove(treePointsToRemove.get(i) - i);
                treeSizes.remove(treePointsToRemove.get(i) - i);
            }
        }

        treePointsToRemove.clear();

        for (i = 0; i < treePoints.size(); i++) {
            treePoints.get(i).offset((int) (-1*width*(deltaTimeMillis/crossTime)), 0);
            if (treePoints.get(i).x < -1*width) treePointsToRemove.add(i);
        }

        if (linePoints.size() > 0)
            return isPointOnRoad(linePoints.getLast().x, linePoints.getLast().y);

        return false;
    }
}
