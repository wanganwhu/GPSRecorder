package com.wangan.gpsrecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MapView mMapView = (MapView) findViewById(R.id.main_mapview);
        //设置启用内置的缩放控件
        mMapView.setBuiltInZoomControls(true);
        //得到mMapView的控制权,可以用它控制和驱动平移和缩放
        MapController mMapController = mMapView.getController();
        //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        GeoPoint point = new GeoPoint((int) (39.915 * 1E6), (int) (116.404 * 1E6));
        //设置地图中心点
        mMapController.setCenter(point);
        //设置地图zoom级别
        mMapController.setZoom(12);
    }
}
