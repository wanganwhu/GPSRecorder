package com.wangan.gpsrecorder.model;

import java.util.ArrayList;

/**
 * Created by 10394 on 2018-02-05.
 */

public class UnUploadData {
    private ArrayList<PointDetails> UnUploadData;

    public ArrayList<PointDetails> getUnUploadData() {
        return UnUploadData;
    }

    @Override
    public String toString() {
        return "{\"UnUploadData\":{"
                + "\"UnUploadData\":" + UnUploadData
                + "}}";
    }

    public void setUnUploadData(ArrayList<PointDetails> UnUploadData) {
        this.UnUploadData = UnUploadData;
    }
}
