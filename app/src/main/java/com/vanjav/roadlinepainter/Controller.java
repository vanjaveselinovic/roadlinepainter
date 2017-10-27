package com.vanjav.roadlinepainter;

import android.content.Context;
import android.graphics.PointF;

import java.util.Iterator;
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
    private float lineWidth, roadWidth, outlineWidth;

    private Random random;
    private int i;

    /* zone 1 */
    private LinkedList<PointF> flowerPoints;
    private LinkedList<TreePointF> treePoints;
    private LinkedList<PointF> treePointsToRemove, flowerPointsToRemove;
    private float biggestTreeWidth;

    public Controller (int width, int height, Context context) {
        linePoints = new LinkedList<PointF>();
        roadPoints = new LinkedList<PointF>();
        firstLinePointToKeep = 0;
        firstRoadPointToKeep = 0;
        score = 0;

        this.width = width;
        this.height = height;
        crossTime = 1000; //1 second
        lineWidth = 20;
        roadWidth = 200;
        outlineWidth = (float) (roadWidth * 1.75);
        biggestTreeWidth = 300;

        random = new Random();

        /* zone 1* */
        flowerPoints = new LinkedList<PointF>();
        treePoints = new LinkedList<TreePointF>();
        treePointsToRemove = new LinkedList<PointF>();
        flowerPointsToRemove = new LinkedList<PointF>();

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

    public LinkedList<TreePointF> getTreePoints() {
        return treePoints;
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

    public float getOutlineWidth() { return outlineWidth; }

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

    private int left, right, mid;

    private int binarySearchTreePointListByY(LinkedList<TreePointF> list, float y) {
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
        //flowers above road
        numItemsToAdd = random.nextInt(7);

        for (i = 0; i < numItemsToAdd; i++) {
            flowerPoints.add(new PointF(
                    x-250 + random.nextFloat()*500,
                    y+300 + random.nextFloat()*250));
        }

        //flowers below road
        numItemsToAdd = random.nextInt(7);

        for (i = 0; i < numItemsToAdd; i++) {
            flowerPoints.add(new PointF(
                    x-250 + random.nextFloat()*500,
                    y-300 - random.nextFloat()*250));
        }

        //trees above road
        numItemsToAdd = random.nextInt(3);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-width/2 + random.nextFloat()*width/2;
            addY = (float) (y+roadWidth/2 + random.nextFloat()*(1.1*height-y));

            if (!isPointOnRoad(addX, addY)) {
                positionToInsert = binarySearchTreePointListByY(treePoints, addY);
                treePoints.add(positionToInsert, new TreePointF(addX, addY, random.nextInt(5)));
            }
        }

        //trees below road
        numItemsToAdd = random.nextInt(3);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-width/2 + random.nextFloat()*width/2;
            addY = y-roadWidth/2 - random.nextFloat()*y;

            if (!isPointOnRoad(addX, addY)) {
                positionToInsert = binarySearchTreePointListByY(treePoints, addY);
                treePoints.add(positionToInsert, new TreePointF(addX, addY, random.nextInt(5)));
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

            if (x >= prevPoint.x - roadWidth && x <= currPoint.x + roadWidth) {
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
        }

        return false;
    }

    private int firstLinePointToKeep, firstRoadPointToKeep, firstFlowerPointToKeep;
    //private LinkedList<PointF> treePointsToRemove, flowerPointsToRemove;
    private float offsetX;
    private boolean pointToKeepFound = false;

    Iterator<PointF> flowerPointsIterator;
    Iterator<TreePointF> treePointsIterator;

    public boolean update(float deltaTimeMillis) {
        genRoad();

        updateScore((float) width*(deltaTimeMillis/crossTime));

        offsetX = -1*width*(deltaTimeMillis/crossTime);

        if (roadPoints.size() > 0) {
            roadPoints.getFirst().offset(offsetX, 0);
            firstRoadPointToKeep = 0;

            for (i = 1; i < roadPoints.size(); i++) {
                currPoint = roadPoints.get(i);
                currPoint.offset(offsetX, 0);

                if (currPoint.x < 0 - outlineWidth)
                    firstRoadPointToKeep = i - 1;
            }

            roadPoints.subList(0, firstRoadPointToKeep).clear();
        }

        if (flowerPoints.size() > 0) {
            /*
            for (flowerPointsIterator = flowerPoints.iterator(); flowerPointsIterator.hasNext();) {
                currPoint = flowerPointsIterator.next();
                currPoint.offset(offsetX, 0);

                if (currPoint.x < 0 - lineWidth)
                    flowerPointsIterator.remove();
            }
            */

            flowerPointsToRemove.clear();

            for (i = 0; i < flowerPoints.size(); i++) {
                currPoint = flowerPoints.get(i);
                currPoint.offset(offsetX, 0);

                if (currPoint.x < 0 - width)
                    flowerPointsToRemove.add(currPoint);
            }

            for (i = 0; i < flowerPointsToRemove.size(); i++) {
                flowerPoints.remove(flowerPointsToRemove.get(i));
            }
        }

        if (treePoints.size() > 0) {
            /*
            for (treePointsIterator = treePoints.iterator(); treePointsIterator.hasNext();) {
                currPoint = treePointsIterator.next();
                currPoint.offset(offsetX, 0);

                if (currPoint.x < 0 - biggestTreeWidth)
                    treePointsIterator.remove();
            }
            */
            
            treePointsToRemove.clear();

            for (i = 0; i < treePoints.size(); i++) {
                currPoint = treePoints.get(i);
                currPoint.offset(offsetX, 0);

                if (currPoint.x < 0 - biggestTreeWidth)
                    treePointsToRemove.add(currPoint);
            }

            for (i = 0; i < treePointsToRemove.size(); i++) {
                treePoints.remove(treePointsToRemove.get(i));
            }
        }

        if (linePoints.size() > 0) {
            pointToKeepFound = false;

            linePoints.getFirst().offset(offsetX, 0);
            firstLinePointToKeep = 0;

            for (i = 1; i < linePoints.size(); i++) {
                currPoint = linePoints.get(i);
                currPoint.offset(offsetX, 0);

                if (!pointToKeepFound) {
                    if (currPoint.x < 0 - lineWidth) {
                        firstLinePointToKeep = i - 1;
                    } else {
                        pointToKeepFound = true;
                    }
                }
            }

            linePoints.subList(0, firstLinePointToKeep).clear();

            return isPointOnRoad(linePoints.getLast().x, linePoints.getLast().y);
        }

        return false;
    }
}
