package com.example.myrobotcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RobotPosition extends View {
    private final int pointsCount = 72;

    private List<Integer> distances = new ArrayList<>(Collections.nCopies(pointsCount, 150));
    private int rotationDegree = 0;

    private Bitmap robotBitmap;
    private Bitmap compassBitmap;
    private Matrix compassRotate;
    private Paint pointPaint;
    private int centerX;
    private int centerY;


    public RobotPosition(Context context, AttributeSet attrs) {
        super(context, attrs);
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.myrobot);
        compassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compas);
        Matrix resizeBitmaps = new Matrix();
        resizeBitmaps.postScale(
                0.5f,
                0.5f
        );
        robotBitmap = Bitmap.createBitmap(
                robotBitmap, 0, 0,
                robotBitmap.getWidth(), robotBitmap.getHeight(),
                resizeBitmaps, false);
        compassBitmap = Bitmap.createBitmap(
                compassBitmap, 0, 0,
                compassBitmap.getWidth(), compassBitmap.getHeight(),
                resizeBitmaps, false);
        compassRotate = new Matrix();
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        centerX = newWidth / 2;
        centerY = newHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap
                (robotBitmap,
             centerX - robotBitmap.getWidth() / 2f,
             centerY - robotBitmap.getHeight() / 2f,
            null);
        compassRotate.setRotate(rotationDegree,
                compassBitmap.getWidth() / 2f,
                compassBitmap.getHeight() / 2f);
        @SuppressLint("DrawAllocation")
        Bitmap rotatedCompass = Bitmap.createBitmap
                (
                        compassBitmap, 0, 0,
                        compassBitmap.getWidth(),
                        compassBitmap.getHeight(),
                        compassRotate, true
                );
        canvas.drawBitmap(rotatedCompass,
                centerX - rotatedCompass.getWidth() / 2f,
                centerY - rotatedCompass.getHeight() / 2f,
               null);
        for (int pointIndex = 0; pointIndex < pointsCount; pointIndex++) {
            double angle = Math.toRadians((double) (360 / pointsCount) * pointIndex);
            double distance = distances.get(pointIndex);

            float x = (float) (centerX + distance * Math.cos(angle));
            float y = (float) (centerY + distance * Math.sin(angle));

            int pointsRadius = 3;
            canvas.drawCircle(x, y, pointsRadius, pointPaint);
        }
    }

    public void setDistances(List<Integer> distances) {
        this.distances = distances;
        invalidate();
    }

    public void setRotationDegree(int rotationDegree) {
        this.rotationDegree = rotationDegree;
        invalidate();
    }

    public int getPointsCount() {
        return pointsCount;
    }
}

