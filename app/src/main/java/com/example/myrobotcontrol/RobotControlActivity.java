package com.example.myrobotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.Button;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.media.VideoView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class RobotControlActivity extends AppCompatActivity {

    private Robot robot;

    private Joystick leftJoystick;
    private Joystick rightJoystick;
    private Button gripperOpenButton;
    private Button gripperCloseButton;
    private Button gripperUpButton;
    private Button gripperDownButton;
    private Button stopButton;

    private LibVLC libVLC;
    private RobotPosition robotPosition;

    private boolean isStop = false;
    private int moveUpDown;
    private int moveOpenClose;

    @SuppressLint("ClickableViewAccessibility")
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

        leftJoystick = findViewById(R.id.leftJoystick);
        rightJoystick = findViewById(R.id.rightJoystick);

        gripperOpenButton = findViewById(R.id.gripperOpenButton);
        gripperCloseButton = findViewById(R.id.gripperCloseButton);
        gripperUpButton = findViewById(R.id.gripperUpButton);
        gripperDownButton = findViewById(R.id.gripperDownButton);
        stopButton = findViewById(R.id.stopButton);

        robotPosition = findViewById(R.id.robotPosition);

        gripperOpenButton.setOnClickListener(view -> {
            if (!isStop) moveOpenClose = 1;
            else moveOpenClose = 0;
        });

        gripperOpenButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                moveOpenClose = 0;
                return true;
            }
            return false;
        });

        gripperCloseButton.setOnClickListener(view -> {
            if (!isStop) moveOpenClose = 2;
            else moveOpenClose = 0;
        });

        gripperCloseButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                moveOpenClose = 0;
                return true;
            }
            return false;
        });

        gripperUpButton.setOnClickListener(view -> {
            if (!isStop) moveUpDown = 1;
            else moveUpDown = 0;
        });

        gripperUpButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                moveUpDown = 0;
                return true;
            }
            return false;
        });

        gripperDownButton.setOnClickListener(view -> {
            if (!isStop) moveUpDown = 2;
            else moveUpDown = 0;
        });

        gripperDownButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                moveUpDown = 0;
                return true;
            }
            return false;
        });

        stopButton.setOnClickListener(view -> isStop = true);

        stopButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                isStop = false;
                return true;
            }
            return false;
        });

        VideoView videoView = findViewById(R.id.videoView);
        String videoSource = "rtsp://" + robotIP + ":8554/unicast";
        libVLC = new LibVLC(this);
        Media media = new Media(libVLC, Uri.parse(videoSource));
        media.addOption(":fullscreen");
        MediaPlayer mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setMedia(media);
        mediaPlayer.getVLCVout()
                .setVideoSurface(videoView.getHolder().getSurface(), videoView.getHolder());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowHeight = displayMetrics.heightPixels;
        int windowWidth = displayMetrics.widthPixels;
        mediaPlayer.getVLCVout().setWindowSize(windowWidth, windowHeight);
        mediaPlayer.getVLCVout().attachViews();
        mediaPlayer.play();
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

            List<Integer> distances =
                    new ArrayList<>(Collections.nCopies(robotPosition.getPointsCount(), 150));
            int rotationDegree = robot.getRotationRelativeToNorth();

            TimerTask task = new TimerTask() {
                public void run() {
                    try {
                        robot.readRobotStatus();
                        robot.convertDataFromView(
                                leftJoystick.getJoystickX(),
                                leftJoystick.getJoystickY(),
                                rightJoystick.getJoystickX(),
                                rightJoystick.getJoystickY(),
                                moveOpenClose,
                                moveUpDown
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    int angleToFirstListIndex =
                            robot.getAngleTOFSensors() * robotPosition.getPointsCount() / 360;
                    int angleToSecondListIndex =
                            (180 - robot.getAngleTOFSensors()) * robotPosition.getPointsCount() / 360;
                    int distanceFromFirstSensor = robot.getFirstTOFSensorValue();
                    int distanceFromSecondSensor = robot.getSecondTOFSensorValue();
                    distances.remove(angleToFirstListIndex);
                    distances.add(angleToFirstListIndex, distanceFromFirstSensor);
                    distances.remove(angleToSecondListIndex);
                    distances.add(angleToFirstListIndex, distanceFromSecondSensor);
                    robotPosition.setDistances(distances);
                    robotPosition.setRotationDegree(rotationDegree);
                }
            };
            timer.schedule(task, delay, period);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        libVLC.release();
        robot.disconnect();
        Intent backToMainActivity = new Intent(this, ConnectionActivity.class);
        startActivity(backToMainActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        libVLC.release();
        robot.disconnect();
    }
}