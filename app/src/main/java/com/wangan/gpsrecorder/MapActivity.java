package com.wangan.gpsrecorder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import javax.microedition.khronos.opengles.GL10;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MapActivity extends Activity implements View.OnClickListener,SensorEventListener {

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();

    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    private boolean isLoacte = false;

    /**
     * 当前地点击点
     */
    private LatLng currentPt;
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);

    MapView mMapView;
    BaiduMap mBaiduMap;

    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;


    private DrawerLayout drawerLayout;
    private SystemBarTintManager tintManager;
    private NavigationView navigationView;
    ImageView menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 地图初始化
        mMapView = findViewById(R.id.main_map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currentPt = latLng;
                updateMapState();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                currentPt = mapPoi.getPosition();
                updateMapState();
                return false;
            }
        });
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);


        initWindow();
        drawerLayout = findViewById(R.id.activity_na);
        navigationView = findViewById(R.id.nav);
        menu= findViewById(R.id.main_menu);
        View headerView = navigationView.inflateHeaderView(R.layout.map_head);
        TextView userNameView = headerView.findViewById(R.id.user_name);

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
                        Intent intent = new Intent(MapActivity.this,
                                LocalFacilityManageActivity.class);
                        startActivity(intent);
                        return true;

                    case "设施点管理":
                        //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(navigationView);
                        return true;

                }

                //Toast.makeText(MapActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                //drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });

        String userName = getIntent().getStringExtra("username");
        userNameView.setText("当前登录用户：" + userName);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected  void onResume(){
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
        }else{
            checkPermissionREAD_EXTERNAL_STORAGE(this);
        }

        if (checkPermissionACCESS_FINE_LOCATION(this)) {
        }else{
            checkPermissionACCESS_FINE_LOCATION(this);
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE,MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123;

    public boolean checkPermissionACCESS_FINE_LOCATION(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showDialog("Get Location", context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission, final int permissionCode) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                permissionCode);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MapActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
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

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || isLoacte) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(15.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            currentPt =new LatLng(mCurrentLat,mCurrentLon);
            isLoacte = true;
        }
    }



    /**
     * 更新地图状态显示面板
     */
    private void updateMapState() {
        String state = "";
        if (currentPt == null) {
        } else {
            state = String.format(",当前经度： %f 当前纬度：%f",
                    currentPt.longitude, currentPt.latitude);
            MarkerOptions ooA = new MarkerOptions().position(currentPt).icon(bdA);
            mBaiduMap.clear();
            mBaiduMap.addOverlay(ooA);
        }
        state += "\n";
        MapStatus ms = mBaiduMap.getMapStatus();
        state += String.format(
                "zoom=%.1f rotate=%d overlook=%d",
                ms.zoom, (int) ms.rotate, (int) ms.overlook);
        Toast.makeText(MapActivity.this,state,Toast.LENGTH_LONG).show();
    }
}
