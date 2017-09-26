package com.vanjav.roadlinepainter;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Vanja on 2017-09-16.
 */

public class Draw {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer colorBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    private final String vertexShaderCode =
            "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  vColor = aColor;" +
                    "  gl_Position = aPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final int program;

    private float width;
    private float height;

    private List<Float> loadedPoints = new LinkedList<Float>();
    private List<Float> loadedColors = new LinkedList<Float>();

    private int mPositionHandle;
    private int mColorHandle;

    public Draw(float width, float height) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);

        this.width = width;
        this.height = height;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private float convertX(float f) {
        return (f - (width / 2)) / (width / 2);
    };

    private float convertY(float f) {
        return -1 * (f - (height / 2)) / (height / 2);
    };

    public void drawLoaded() {
        float coords[] = new float[loadedPoints.size()];
        float colors[] = new float[loadedColors.size()];
        short drawOrder[] = new short[loadedPoints.size()/2];

        for (int i = 0; i < loadedPoints.size(); i++) {
            coords[i] = loadedPoints.get(i);
        }

        for (int i = 0; i < loadedColors.size(); i++) {
            colors[i] = loadedColors.get(i);
        }

        for (short i = 0; i < loadedPoints.size()/2; i++) {
            drawOrder[i] = i;
        }

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(coords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(program);

        mPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        mColorHandle = GLES20.glGetAttribLocation(program, "aColor");

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    };

    public void clearLoaded() {
        loadedPoints.clear();
        loadedColors.clear();
    }

    public void loadTriangle(PointF p1, PointF p2, PointF p3, int c) {
        loadedPoints.add(convertX(p1.x));
        loadedPoints.add(convertY(p1.y));
        loadedPoints.add(convertX(p2.x));
        loadedPoints.add(convertY(p2.y));
        loadedPoints.add(convertX(p3.x));
        loadedPoints.add(convertY(p3.y));

        for (int i = 0; i < 3; i++) {
            loadedColors.add((float) Color.red(c)/255);
            loadedColors.add((float) Color.green(c)/255);
            loadedColors.add((float) Color.blue(c)/255);
            loadedColors.add((float) Color.alpha(c)/255);
        }
    }

    public void loadRectangle(PointF p1, PointF p2, PointF p3, PointF p4, int c) {
        loadTriangle(p1, p2, p3, c);
        loadTriangle(p2, p3, p4, c);
    }

    public void loadLine(PointF p1, PointF p2, float width, int c) {
        float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;
        if (p2.y - p1.y == 0) {
            p1x = p1.x;
            p1y = p1.y - width / 2;
            p2x = p2.x;
            p2y = p2.y - width / 2;
            p3x = p1.x;
            p3y = p1.y + width / 2;
            p4x = p2.x;
            p4y = p2.y + width / 2;
        } else {
            float m = (float) -1 * ((p2.x - p1.x) / (p2.y - p1.y));
            float k = (float) (width / (2 * Math.sqrt(1 + Math.pow(m, 2))));
            p1x = p1.x + k;
            p1y = p1.y + k * m;
            p2x = p2.x + k;
            p2y = p2.y + k * m;
            p3x = p1.x - k;
            p3y = p1.y - k * m;
            p4x = p2.x - k;
            p4y = p2.y - k * m;
        }

        loadRectangle(
                new PointF(p1x, p1y),
                new PointF(p2x, p2y),
                new PointF(p3x, p3y),
                new PointF(p4x, p4y),
                c
        );
    }

    public void loadCircle(PointF p, float r, int c) {
        float count = 4;

        float percent, rad;

        PointF prevPoint, point;

        prevPoint = new PointF(p.x + r, p.y);

        for (int i = 1; i < count; i++) {
            percent = i / count;
            rad = percent * 2*(float)Math.PI;

            point = new PointF(p.x + r * (float)cos(rad), p.y + r * (float)sin(rad));

            loadTriangle(
                    p,
                    prevPoint,
                    point,
                    c
            );

            prevPoint = point;
        }
    }

    public void loadLineRounded(PointF p1, PointF p2, float width, int c) {
        loadLine(p1, p2, width, c);
        loadCircle(p1, width/2, c);
        loadCircle(p2, width/2, c);
    }

    public void loadMultiLine(List<PointF> points, float width, int c) {
        for (int i = 1; i < points.size(); i++) {
            loadLine(points.get(i-1), points.get(i), width, c);
        }
    }
}