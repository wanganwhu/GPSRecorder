package com.wangan.gpsrecorder.model;

/**
 * Created by 10394 on 2018-01-30.
 * 数值*1'000'000的经纬度坐标
 */

public class Coordinate {

    private double longitude;
    private double latitude;

    @Override
    public String toString() {
        return "{\"longitude\":\"" + longitude + "\""
                + ", \"latitude\":\"" + latitude + "\"}";
    }

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
    public double getLatitude() {
        return latitude;
    }

}
