package com.hxxr.rfterm;

public class SerialDevice {
    // SerialDevice is a class which stores info for one device which has a serial port.
    // Each instance of this class has the following properties:
    // name: Friendly name of the bluetooth device
    // address: MAC address of the bluetooth device
    // classId: A number representing the type of device (something like 0x400100)
    // type: Type of device (computer, smartphone, etc...), set automatically based on classId
    // icon: ID of icon to use for this file (d0, d2 ... d8), also set based on classId
    private String name, address, type;
    private int classId, icon;
    public SerialDevice(String name, String address, int classId) {
        this.name = name;
        this.address = address;
        this.classId = classId;
        this.type = getTypeFromClassId(classId);
        this.icon = getIconFromClassId(classId);
    }


    // Method for converting classId to type
    private String getTypeFromClassId(int classId) {
        switch (classId & 0x1F00) {
            case 0: return "Misc Device";
            case 0x100: return "Computer";
            case 0x200: return "Telephone";
            case 0x300: return "LAN/Network Access Point";
            case 0x400: return "Audio/Video Device";
            case 0x500: return "Controller";
            case 0x600: return "Imaging Device";
            case 0x700: return "Wearable Device";
            case 0x800: return "Toy";
            case 0x1F00: return "Unspecified Device";
            default: return "(Error Getting Device Type)";
        }
    }

    // Method for converting classId to icon
    private int getIconFromClassId(int classId) {
        switch (classId & 0x1F00) {
            case 0: return R.drawable.d0;
            case 0x100: return R.drawable.d1;
            case 0x200: return R.drawable.d2;
            case 0x300: return R.drawable.d3;
            case 0x400: return R.drawable.d4;
            case 0x500: return R.drawable.d5;
            case 0x600: return R.drawable.d6;
            case 0x700: return R.drawable.d7;
            case 0x800: return R.drawable.d8;
            case 0x1F00: return R.drawable.d0;
            default: return R.drawable.d0;
        }
    }


    // Here we have the get and set methods to change the properties of class instances:
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getType() {
        return type;
    }
    public int getClassId() {
        return classId;
    }
    public int getIcon() {
        return icon;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setClassId(int classId) {
        this.classId = classId;
        this.type = getTypeFromClassId(classId);
        this.icon = getIconFromClassId(classId);
    }
}
