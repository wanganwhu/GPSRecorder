package com.wangan.gpsrecorder.model;

import java.util.List;



/**
 * Created by 10394 on 2018-01-29.
 */

public class PointDetails {

    private String id;
    private String geometrytype;
    private List<Data> data;


    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }


    public void setGeometrytype(String geometrytype) {
        this.geometrytype = geometrytype;
    }
    public String getGeometrytype() {
        return geometrytype;
    }


    public void setData(List<Data> data) {
        this.data = data;
    }
    public List<Data> getData() {
        return data;
    }

}
//===========================



 class Coordinate {

    private String longitude;
    private String latitude;


    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public String getLongitude() {
        return longitude;
    }


    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLatitude() {
        return latitude;
    }

}




 class Data {

    private String scene1;
    private String scene2;

    private String facilitytpye;
    private String county;
    private String street;
    private String community;

    private String facilityaddress;
    private String quality;
    private List<String> image;
    private Coordinate coordinate;
    private String otherInformation;


    public void setScene1(String scene1) {
        this.scene1 = scene1;
    }
    public String getScene1() {
        return scene1;
    }


    public void setScene2(String scene2) {
        this.scene2 = scene2;
    }
    public String getScene2() {
        return scene2;
    }


    public void setFacilitytpye(String facilitytpye) {
        this.facilitytpye = facilitytpye;
    }
    public String getFacilitytpye() {
        return facilitytpye;
    }


    public void setCounty(String county) {
        this.county = county;
    }
    public String getCounty() {
        return county;
    }


    public void setStreet(String street) {
        this.street = street;
    }
    public String getStreet() {
        return street;
    }


    public void setCommunity(String community) {
        this.community = community;
    }
    public String getCommunity() {
        return community;
    }


    public void setFacilityaddress(String facilityaddress) {
        this.facilityaddress = facilityaddress;
    }
    public String getFacilityaddress() {
        return facilityaddress;
    }


    public void setQuality(String quality) {
        this.quality = quality;
    }
    public String getQuality() {
        return quality;
    }


    public void setImage(List<String> image) {
        this.image = image;
    }
    public List<String> getImage() {
        return image;
    }


    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
    public Coordinate getCoordinate() {
        return coordinate;
    }

     public void setOtherInformation(String otherInformation) {
         this.otherInformation = otherInformation;
     }

    public String getOtherInformation() {
         return otherInformation;
    }

}


