package com.wangan.gpsrecorder.model;

import java.util.List;



/**
 * Created by 10394 on 2018-01-29.
 */

public class PointDetails {

    private int id;
    private String geometrytype;
    private List<PointData> data;


    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }


    public void setGeometrytype(String geometrytype) {
        this.geometrytype = geometrytype;
    }
    public String getGeometrytype() {
        return geometrytype;
    }


    public void setData(List<PointData> data) {
        this.data = data;
    }
    public List<PointData> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "{"
                + "\"id\":\"" + id + "\""
                + ", \"geometrytype\":\"" + geometrytype + "\""
                + ", \"data\":" + data
                + "}";
    }
}








