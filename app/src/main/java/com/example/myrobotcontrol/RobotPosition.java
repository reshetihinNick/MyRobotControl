package com.example.myrobotcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RobotPosition extends View {
    private static final int POINT_RADIUS = 5; // Радиус точки
    private static final int CIRCLE_RADIUS = 200; // Радиус окружности
    private static final int POINT_COUNT = 24; // Количество точек

    private Bitmap robotBitmap;
    private Paint pointPaint;
    private int centerX;
    private int centerY;

    public RobotPosition(Context context) {
        super(context);
        init();
    }

    public RobotPosition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotPosition(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Инициализация кисти для рисования точек
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.myrobot);
//        robotBitmap.setHeight(219);
//        robotBitmap.setWidth(226);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Вычисление центра view
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(robotBitmap, centerX - robotBitmap.getWidth() / 2f, centerY - robotBitmap.getHeight() / 2f, null);
        // Рисование точек по окружности
        for (int i = 0; i < POINT_COUNT; i++) {
            double angle = Math.toRadians((360f / POINT_COUNT) * i);
            double distance = getCircularDistance(i); // Получение расстояния до центра окружности для точки i

            float x = (float) (centerX + distance * Math.cos(angle));
            float y = (float) (centerY + distance * Math.sin(angle));

            canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);
        }
    }

    private double getCircularDistance(int index) {
        // Возвращает расстояние до центра окружности для точки с заданным индексом
        // Здесь можно использовать формулу или алгоритм, который зависит от вашей конкретной логики
        // В данном примере используется простой линейный рост расстояния в зависимости от индекса точки
        return (index + 1) * (CIRCLE_RADIUS / POINT_COUNT);
    }
}

