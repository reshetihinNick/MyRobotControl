package com.example.myrobotcontrol;

import org.junit.Test;

import static org.junit.Assert.*;
public class RobotTest {
    @Test
    public void transformRange() throws Exception {
        Robot robot = new Robot("127.0.0.1");
        assertEquals(
                robot.transformRange(
                        0, 20, 100,
                        30, 50),
                30);
    }
}