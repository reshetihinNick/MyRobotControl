package com.example.myrobotcontrol;

import static java.lang.Math.abs;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

import java.net.InetAddress;

public class Robot {
    private int rotationRelativeToNorth;
    private int firstTOFSensorValueInMM;
    private int secondTOFSensorValueInMM;
    private int firstTOFSensorValue;
    private int secondTOFSensorValue;
    private int angleTOFSensors;
    private int isGripperUp;
    private int isGripperClosed;
    private final TCPMasterConnection modbusConnection;
    private final int slaveModbusAddress = 1;
    private boolean CONNECTED = false;
    private final String IPAddress;

    public Robot(String IP_address) throws Exception {
        this.IPAddress = IP_address;
        this.modbusConnection = new TCPMasterConnection(InetAddress.getByName(IPAddress));
        this.modbusConnection.setPort(33877);
        this.modbusConnection.setTimeout(1000);
        Thread startConnection = new Thread(() -> {
            try {
                if (!CONNECTED) {
                    modbusConnection.connect();
                    CONNECTED = true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        startConnection.start();
    }

    public void disconnect() {
        CONNECTED = false;
        modbusConnection.close();
    }

    public void readRobotStatus() throws Exception {
        int startAddress = 0;
        int registerCount = 8;
        int rotationRelativeToNorthRegister = 0;
        int firstTOFSensorValueInMMRegister = 1;
        int secondTOFSensorValueInMMRegister = 2;
        int angleTOFSensorsRegister = 3;
        int isGripperUpRegister = 4;
        int isGripperClosedRegister = 6;
        ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(startAddress, registerCount);
        ReadMultipleRegistersResponse response = new ReadMultipleRegistersResponse();
        request.setUnitID(slaveModbusAddress);
        response.setUnitID(slaveModbusAddress);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction(modbusConnection);
        transaction.setRequest(request);
        transaction.execute();
        response = (ReadMultipleRegistersResponse) transaction.getResponse();
        this.rotationRelativeToNorth = response.getRegisterValue(rotationRelativeToNorthRegister);

        this.firstTOFSensorValueInMM = response.getRegisterValue(firstTOFSensorValueInMMRegister);
        this.secondTOFSensorValueInMM = response.getRegisterValue(secondTOFSensorValueInMMRegister);
        this.firstTOFSensorValue = transformRange(
                firstTOFSensorValueInMM,0, 2000, 50, 150);
        this.secondTOFSensorValue = transformRange(
                secondTOFSensorValueInMM,0, 2000, 50, 150);
        this.angleTOFSensors = response.getRegisterValue(angleTOFSensorsRegister);
        this.isGripperUp = response.getRegisterValue(isGripperUpRegister);
        this.isGripperClosed = response.getRegisterValue(isGripperClosedRegister);
    }

    private int transformRange(
            int value,
            int oldMinVal, int oldMaxValue,
            int newMinVal, int newMaxVal
    ) {
        return newMinVal +
                ((newMaxVal - newMinVal) * ((value - oldMinVal) /
                        (oldMaxValue - oldMinVal)));
    }

    public void convertDataFromView(
            int x_leftStick, int y_leftStick,
            int x_rightStick, int y_rightStick,
            int moveOpenClose, int moveUpDown
    ) throws Exception {
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
                transformRange(
                        abs(leftDiagonalSpeedValue),
                        0, 1020,
                        0, 100
                )

        );
        Register rightDiagonalEnginesSpeed = new SimpleRegister(
                transformRange(
                        abs(rightDiagonalSpeedValue),
                        0, 1020,
                        0, 100
                )

        );
        Register[] data = new Register[]{
                leftDiagonalDirection,
                rightDiagonalDirection,
                leftDiagonalEnginesSpeed,
                rightDiagonalEnginesSpeed,
                new SimpleRegister(moveUpDown),
                new SimpleRegister(moveOpenClose)
        };
        writeToRobot(data);
    }

    private void writeToRobot(Register[] dataFromJoysticks) throws Exception {
        int startAddress = 8;
        WriteMultipleRegistersRequest writingRequest =
                new WriteMultipleRegistersRequest(startAddress, dataFromJoysticks);
        writingRequest.setUnitID(slaveModbusAddress);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction(modbusConnection);
        transaction.setRequest(writingRequest);
        transaction.execute();
    }

    public int getRotationRelativeToNorth() {
        return rotationRelativeToNorth;
    }

    public int getFirstTOFSensorValueInMM() {
        return firstTOFSensorValueInMM;
    }

    public int getSecondTOFSensorValueInMM() {
        return secondTOFSensorValueInMM;
    }

    public int getFirstTOFSensorValue() {
        return firstTOFSensorValue;
    }

    public int getSecondTOFSensorValue() {
        return secondTOFSensorValue;
    }

    public int getAngleTOFSensors() {
        return angleTOFSensors;
    }

    public int getIsGripperClosed() {
        return isGripperClosed;
    }

    public int getIsGripperUp() {
        return isGripperUp;
    }

    public String getIPAddress() {
        return IPAddress;
    }

}
