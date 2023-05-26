package com.example.myrobotcontrol;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;

import java.net.InetAddress;

public class Robot {
    private int rotationRelativeToNorth;
    private int firstTOFSensorValueInMM;
    private int secondTOFSensorValueInMM;
    private int angleTOFSensors;
    private int isGripperUp;
    private int isGripperClosed;
    private TCPMasterConnection modbusConnection;
    private final int slaveModbusAddress = 1;
    private boolean CONNECTED = false;
    private String IPAddress;

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
        this.angleTOFSensors = response.getRegisterValue(angleTOFSensorsRegister);
        this.isGripperUp = response.getRegisterValue(isGripperUpRegister);
        this.isGripperClosed = response.getRegisterValue(isGripperClosedRegister);
    }

    public void setMoveFromJoysticks(Register[] dataFromJoysticks) throws Exception {
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

    public int getAngleTOFSensors() {
        return angleTOFSensors;
    }

    public int getIsGripperClosed() {
        return isGripperClosed;
    }

    public int getIsGripperUp() {
        return isGripperUp;
    }

}
