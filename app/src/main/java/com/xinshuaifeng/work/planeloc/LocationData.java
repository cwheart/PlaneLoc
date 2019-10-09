package com.xinshuaifeng.work.planeloc;

import java.text.DecimalFormat;
import java.util.Date;

class LocationData {
    private int start;
    private String planeNo;
    private String tailNo;
    private float pressure;
    private float speed;
    private double longitude; // 经度
    private double latitude; // 纬度
    private double altitude; // 海拔
    private double pressureAltitude; // 气压海拔

    private DecimalFormat df;

    public LocationData () {
        this.start = (int)new Date().getTime();
        this.df = new DecimalFormat("0.000");
        this.df.getRoundingMode();
    }

    public String getPlaneNo() {
        return planeNo;
    }

    public void setPlaneNo(String planeNo) {
        this.planeNo = planeNo;
    }

    public String getTailNo() {
        return tailNo;
    }

    public void setTailNo(String tailNo) {
        this.tailNo = tailNo;
    }

    public float getPressure() {
        return pressure;
    }

    public String getPressureValue() {
        return df.format(this.pressure);
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
        this.pressureAltitude = (1013.25 - pressure) * 9;
    }

    public float getSpeed() {
//        return (float)10.0;
        int now = (int) new Date().getTime();
        int t = now - start;
        return speed + (t % 200) + 50;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getLongitude() {
        return (float)longitude;
//        return (float) 99.7622;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return (float)latitude;
//        return (float) 89.7622;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public float getAltitude() {
        int now = (int) new Date().getTime();
        int t = now - start;

        return (float)altitude + (t % 100) + 50;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getPressureAltitude() {
        int now = (int) new Date().getTime();
        int t = now - start;
        return (float) pressureAltitude + (t % 100) + 50;
    }

    public String getPressureAltitudeValue() {
        return df.format(this.getPressureAltitude());
    }
}
