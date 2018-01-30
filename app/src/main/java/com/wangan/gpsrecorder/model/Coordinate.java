package com.wangan.gpsrecorder.model;

/**
 * Created by 10394 on 2018-01-30.
 * 数值*1'000'000的经纬度坐标
 */

public class Coordinate {

    private int longitude;
    private int latitude;

    public Coordinate(int longitude, int latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
    public int getLongitude() {
        return longitude;
    }


    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
    public int getLatitude() {
        return latitude;
    }

}
