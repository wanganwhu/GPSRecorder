package com.wangan.gpsrecorder.model;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 10394 on 2018-01-30.
 */

public class PointData {

    private String scene1 = "";
    private String scene2 = "";
    private String facilitytpye = "";
    private String county = "";
    private String street = "";
    private String community = "";

    private String facilityaddress = "";
    private int quality = 1;//该设施是否可以，1为可用，-1为不可用，0为改建中
    private String[] imagePaths = {"","",""};
    private Coordinate coordinate = new Coordinate(0,0);
    private String otherInformation = "";

    public PointData(String scene1, String scene2, String facilitytpye, String county,
                     String street, String community, String facilityaddress, int quality,
                     String[] imagePaths, Coordinate coordinate, String otherInformation) {
        this.scene1 = scene1;
        this.scene2 = scene2;
        this.facilitytpye = facilitytpye;
        this.county = county;
        this.street = street;
        this.community = community;
        this.facilityaddress = facilityaddress;
        this.quality = quality;
        this.imagePaths = imagePaths;
        this.coordinate = coordinate;
        this.otherInformation = otherInformation;
    }

    public PointData(){

    }


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


    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getQuality() {
        return quality;
    }


    public void setImage(String[] imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String[] getImage() {
        return imagePaths;
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

    @Override
    public String toString() {
        return "{"
                + "\"scene1\":\"" + scene1 + "\""
                + ", \"scene2\":\"" + scene2 + "\""
                + ", \"facilitytpye\":\"" + facilitytpye + "\""
                + ", \"county\":\"" + county + "\""
                + ", \"street\":\"" + street + "\""
                + ", \"community\":\"" + community + "\""
                + ", \"facilityaddress\":\"" + facilityaddress + "\""
                + ", \"quality\":\"" + quality + "\""
                + ", \"imagePaths\":" + arrayToString(imagePaths)
                + ", \"coordinate\":" + coordinate
                + ", \"otherInformation\":\"" + otherInformation + "\""
                + "}";
    }

    public static String arrayToString(Object[] a) {
        if (a == null)
            return "\"\"";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append("\"");
            b.append(String.valueOf(a[i])=="null"?"":String.valueOf(a[i]));
            b.append("\"");
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
}