package com.wangan.gpsrecorder;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MapViewRender;
import com.tianditu.android.maps.MyLocationOverlay;
import com.tianditu.android.maps.Overlay;
import com.tianditu.android.maps.renderoption.DrawableOption;

import javax.microedition.khronos.opengles.GL10;

public class MapActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.map_menu,menu);
        return true;
    }

    //定义菜单响应事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.add_item:
                Intent intent = new Intent(MapActivity.this, RecordDetailActivity.class);
                startActivity(intent);
                break;
            case R.id.remove_item:
                Toast.makeText(this,"你点击了remove",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MapView mMapView =  findViewById(R.id.main_map_view);
        //设置启用内置的缩放控件
        mMapView.setBuiltInZoomControls(true);
        MyOverlay myOverlay = new MyOverlay();

        //得到mMapView的控制权,可以用它控制和驱动平移和缩放
        MapController mMapController = mMapView.getController();

        MyLocationOverlay myLocation = new MyLocationOverlay(this, mMapView);

        //myLocation.enableCompass();  //显示指南针
        myLocation.enableMyLocation(); //显示我的位置


        myOverlay.setGeoPoint(myLocation.getMyLocation());
        myOverlay.onTap(myLocation.getMyLocation(),mMapView);

        mMapView.addOverlay(myLocation);
        mMapView.addOverlay(myOverlay);
        mMapController.setCenter(myLocation.getMyLocation());
        //设置地图zoom级别
        mMapController.setZoom(15);



    }

    public  class MyOverlay extends Overlay {
        private Drawable mDrawable;
        private GeoPoint mGeoPoint;
        private DrawableOption mOption;

        public MyOverlay() {
            mDrawable = MapActivity.this.getResources().getDrawable(R.drawable.poiresult);
            mOption = new DrawableOption();
            mOption.setAnchor(0.5f, 1.0f);
        }

        public void setGeoPoint(GeoPoint point) {
            mGeoPoint = point;
        }

        @Override
        public boolean onTap(GeoPoint point, MapView mapView) {
            mGeoPoint = point;
            //mEditTextLon.setText("" + point.getLongitudeE6());
            //mEditTextLat.setText("" + point.getLatitudeE6());
           // mCbShowView.setChecked(true);

            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
            return super.onKeyUp(keyCode, event, mapView);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event, MapView mapView) {

            return super.onKeyDown(keyCode, event, mapView);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }
            return super.onTouchEvent(event, mapView);
        }

        @Override
        public boolean onLongPress(GeoPoint p, MapView mapView) {
            //mTvTips.setText("onLongPress:" + p.getLatitudeE6() + ","
            //        + p.getLongitudeE6());

            return super.onLongPress(p, mapView);
        }

        @Override
        public boolean isVisible() {
            return super.isVisible();
        }

        @Override
        public void setVisible(boolean b) {
            super.setVisible(b);
        }

        @Override
        public void draw(GL10 gl, MapView mapView, boolean shadow) {
            if (shadow)
                return;

            MapViewRender render = mapView.getMapViewRender();
            render.drawDrawable(gl, mOption, mDrawable, mGeoPoint);
        }
    }
}
