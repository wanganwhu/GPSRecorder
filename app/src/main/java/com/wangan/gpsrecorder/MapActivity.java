package com.wangan.gpsrecorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MapViewRender;
import com.tianditu.android.maps.MyLocationOverlay;
import com.tianditu.android.maps.Overlay;
import com.tianditu.android.maps.renderoption.DrawableOption;

import javax.microedition.khronos.opengles.GL10;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MapActivity extends Activity implements View.OnClickListener{
    private DrawerLayout drawerLayout;
    private SystemBarTintManager tintManager;
    private NavigationView navigationView;
    ImageView menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);
        MapView mMapView =  findViewById(R.id.main_map_view);
        initWindow();
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_na);
        navigationView = (NavigationView) findViewById(R.id.nav);
        menu= (ImageView) findViewById(R.id.main_menu);
        View headerView = navigationView.getHeaderView(0);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭手势滑动，只通过点击按钮来滑动
        menu.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //item.setChecked(true);
                switch (item.getTitle().toString()){
                    case "设施点采集":
                        //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(navigationView);
                        Intent intent = new Intent(MapActivity.this,LocalFacilityManageActivity.class);
                        startActivity(intent);
                        return true;

                    case "    设施点管理":
                        //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(navigationView);
                        return true;

                    case "    学习资料":
                        //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(navigationView);
                        return true;

                }

                //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                //drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_menu:
                if (drawerLayout.isDrawerOpen(navigationView)){
                    drawerLayout.closeDrawer(navigationView);
                }else{
                    drawerLayout.openDrawer(navigationView);
                }
                break;
        }
    }

    private void initWindow() {//初始化窗口属性，让状态栏和导航栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            tintManager = new SystemBarTintManager(this);
            int statusColor = Color.parseColor("#1976d2");
            tintManager.setStatusBarTintColor(statusColor);
            tintManager.setStatusBarTintEnabled(true);
        }
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
