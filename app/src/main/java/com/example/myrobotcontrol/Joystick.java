package com.example.myrobotcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Joystick extends View {
    private final int maxCoordinate = 255;
    private int joystickBackgroundRadius = 50;
    private int joystickRadius = 100;
    private int joystickAlpha = 100;
    private int joystickColor = Color.GREEN;
    private final PointF joystickBackground = new PointF();
    private final PointF joystick = new PointF();
    private final Paint paint = new Paint();

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(joystickColor);
        paint.setAlpha(joystickAlpha);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        joystickBackground.set(newWidth / 2f, newHeight / 2f);
        joystick.set(joystickBackground);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(joystickBackground.x, joystickBackground.y, joystickBackgroundRadius, paint);
        canvas.drawCircle(joystick.x, joystick.y, joystickRadius, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float distance = getDistance(event.getX(), event.getY(), joystickBackground.x, joystickBackground.y);
                if (distance > joystickRadius) {
                    float angle = getAngle(event.getX(), event.getY(), joystickBackground.x, joystickBackground.y);
                    joystick.x = (float) (joystickBackground.x + joystickRadius * Math.cos(angle));
                    joystick.y = (float) (joystickBackground.y + joystickRadius * Math.sin(angle));
                } else {
                    joystick.x = event.getX();
                    joystick.y = event.getY();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                joystick.set(joystickBackground);
                invalidate();
                break;
        }
        return true;
    }

    public int getJoystickX() {
        return (int) ((joystick.x - joystickBackground.x) / joystickRadius * maxCoordinate);
    }

    public int getJoystickY() {
        return (int) ((joystickBackground.y - joystick.y) / joystickRadius * maxCoordinate);
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private float getAngle(float x1, float y1, float x2, float y2) {
        return (float) Math.atan2(y1 - y2, x1 - x2);
    }

    public void setJoystickColor(int joystickColor) {
        this.joystickColor = joystickColor;
    }

    public void setJoystickBackgroundRadius(int joystickBackgroundRadius) {
        this.joystickBackgroundRadius = joystickBackgroundRadius;
    }

    public void setJoystickRadius(int joystickRadius) {
        this.joystickRadius = joystickRadius;
    }

    public void setJoystickAlpha(int joystickAlpha) {
        this.joystickAlpha = joystickAlpha;
    }
}