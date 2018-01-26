package com.wangan.gpsrecorder;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MapViewRender;
import com.tianditu.android.maps.MyLocationOverlay;
import com.tianditu.android.maps.Overlay;
import com.tianditu.android.maps.renderoption.DrawableOption;

import javax.microedition.khronos.opengles.GL10;


public class RecordDetailActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;

    private Button mButtonTake, mButtonChoose, geomButtonGetLocation;
    private TextView location_information;
    private ImageView mImageView;
    private Uri mImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        mImageView =  findViewById(R.id.image_1);
        mButtonTake =  findViewById(R.id.btn_take_photo);
        mButtonChoose =  findViewById(R.id.btn_choose_photo);

        MapView mMapView =  findViewById(R.id.detail_map_view);
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


        mButtonChoose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 打开相册
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });

        mButtonTake.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //新建一个File，传入文件夹目录
                File file = new File(Environment.getExternalStorageDirectory()+ "/aaa");
                //判断文件夹是否存在，如果不存在就创建，否则不创建
                if (!file.exists()) {
                    //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
                    file.mkdirs();
                }
                // 创建File对象，用于存储拍照后的图片，并存放在SD卡的根目录下
                File outputImg = new File(file, "my_img.jpg");
                if (outputImg.exists()) {
                    outputImg.delete();
                }
                try {
                    outputImg.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 将File对象转换为Uri对象
                //mImgUri = Uri.fromFile(outputImg);
                mImgUri = FileProvider.getUriForFile(RecordDetailActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        outputImg);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                // 指定图片的输出地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgUri);
                // 启动相机程序
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    /*Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mImgUri, "image*//*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgUri);
                    // 启动裁剪程序
                    startActivityForResult(intent, CROP_PHOTO);*/

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImgUri));
                        // 将裁剪后的照片显示出来
                        mImageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImgUri));
                        // 将裁剪后的照片显示出来
                        mImageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }

            default:
                break;
        }
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance( this ).removeLocationUpdatesListener();
    }

    /**
     * 4.4以上系统处理图片的方法(Android系统从4.4开始选取相册中的图片不再返回真实地Uri，而是一个封装过的Uri)
     *
     * @param data
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        // 如果是document类型的Uri，则通过document id处理
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // 解析出数字格式的Id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath);
    }

    /**
     * 4.4以下系统处理图片的方法
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    /**
     * 根据图片路径选择图片
     *
     * @param imagePath
     *            图片路径
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(RecordDetailActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过uri和selection来获取真实地图片路径
     *
     * @param uri
     *            资源索引
     * @param selection
     *            查询符合条件的过滤参数，类似于SQL语句中Where之后的条件判断
     * @return
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(Media.DATA));
            }
            cursor.close();
        }
        return path;
    }



    public  class MyOverlay extends Overlay {
        private Drawable mDrawable;
        private GeoPoint mGeoPoint;
        private DrawableOption mOption;

        public MyOverlay() {
            mDrawable = RecordDetailActivity.this.getResources().getDrawable(R.drawable.poiresult);
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
