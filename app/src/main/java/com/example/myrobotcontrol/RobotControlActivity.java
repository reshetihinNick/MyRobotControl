package com.example.myrobotcontrol;

import static java.lang.Math.abs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

import java.util.Timer;
import java.util.TimerTask;


public class RobotControlActivity extends AppCompatActivity {

    private Robot robot;
    private Joystick leftJoystick;
    private Joystick rightJoystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.robot_control_activity);

        Intent getIP = getIntent();
        String robotIP = getIP.getStringExtra("IP_ADDRESS");

        try {
            robot = new Robot(robotIP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String videoSource = "rtsp://" + robotIP + ":8554/unicast";
        VideoView robotCamera = findViewById(R.id.videoView);
        robotCamera.setVideoURI(Uri.parse(videoSource));
        robotCamera.requestFocus();
        robotCamera.start();

        leftJoystick = findViewById(R.id.leftJoystick);
        rightJoystick = findViewById(R.id.rightJoystick);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshData refresh = new refreshData();
        refresh.setDaemon(true);
        refresh.start();
    }

    public class refreshData extends Thread {
        @Override
        public void run() {
            Timer timer = new Timer();
            int delay = 100;
            int period = 100;
            TimerTask task = new TimerTask() {
                @SuppressLint("SetTextI18n")
                public void run() {
                    try {
                        robot.readRobotStatus();
                        robot.setMoveFromJoysticks(
                                convertDataFromJoysticks(
                                        leftJoystick.getJoystickX(),
                                        leftJoystick.getJoystickY(),
                                        rightJoystick.getJoystickX(),
                                        rightJoystick.getJoystickY()
                                )
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            timer.schedule(task, delay, period);
        }
    }

    private double map(
            double value,
            double firstMinVal, double firstMaxValue,
            double secondMinVal, double secondMaxVal
    ) {
        return secondMinVal +
                ((secondMaxVal - secondMinVal) *
                        ((value - firstMinVal) / (firstMaxValue - firstMinVal)));
    }

    private Register[] convertDataFromJoysticks(
            int x_leftStick, int y_leftStick,
            int x_rightStick, int y_rightStick
    ) {
        int leftDiagonalSpeedValue = y_leftStick + x_leftStick + y_rightStick + x_rightStick;
        int rightDiagonalSpeedValue = y_leftStick - x_leftStick + y_rightStick - x_rightStick;
        Register leftDiagonalDirection;
        if (leftDiagonalSpeedValue < 0) leftDiagonalDirection = new SimpleRegister(1);
        else if (leftDiagonalSpeedValue == 0) leftDiagonalDirection = new SimpleRegister(0);
        else leftDiagonalDirection = new SimpleRegister(2);
        Register rightDiagonalDirection;
        if (rightDiagonalSpeedValue < 0) rightDiagonalDirection = new SimpleRegister(1);
        else if (rightDiagonalSpeedValue == 0) rightDiagonalDirection = new SimpleRegister(0);
        else rightDiagonalDirection = new SimpleRegister(2);
        Register leftDiagonalEnginesSpeed = new SimpleRegister(
                (int) map(
                        abs(leftDiagonalSpeedValue),
                        0.0, 1020.0,
                        0.0, 100.0
                )

        );
        Register rightDiagonalEnginesSpeed = new SimpleRegister(
                (int) map(
                        abs(rightDiagonalSpeedValue),
                        0.0, 1020.0,
                        0.0, 100.0
                )

        );
        return new Register[]{
                leftDiagonalDirection,
                rightDiagonalDirection,
                leftDiagonalEnginesSpeed,
                rightDiagonalEnginesSpeed
        };
    }


    @Override
    protected void onPause() {
        super.onPause();
        robot.disconnect();
        Intent backToMainActivity = new Intent(this, MainActivity.class);
        startActivity(backToMainActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.disconnect();
    }
}