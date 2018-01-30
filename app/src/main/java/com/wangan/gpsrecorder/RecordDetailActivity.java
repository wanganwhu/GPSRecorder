package com.wangan.gpsrecorder;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.wangan.gpsrecorder.model.Coordinate;
import com.wangan.gpsrecorder.model.PointData;
import com.wangan.gpsrecorder.model.PointDetails;

import javax.microedition.khronos.opengles.GL10;


public class RecordDetailActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;

    private Button  dealPhoto,save_point_details,save_all_points_details,cancel_point_details;

    //新建设施点的几何类型
    String geometryType = "";

    //必填的信息
    private EditText scene1,scene2,facilityType,county,street,community,facilityAddress;
    private RadioGroup quality_button_group;
    private RadioButton use_button,update_button,unusable_button;
    private ImageView[] imageView = null;
    private String[] imagePaths = new String[3];

    //地图上点的经纬度* 10^6
    int mLongitude,mLatitude;
    //该设施是否可以，1为可用，-1为不可用，0为改建中
    int facilityQuality = 1;

    //选填信息
    private EditText more_information;

    private int imageViewIndex = 2;
    private Uri mImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        scene1 = findViewById(R.id.scene1);
        scene2 = findViewById(R.id.scene2);
        facilityType = findViewById(R.id.facility_type);
        facilityAddress = findViewById(R.id.facility_address);
        county = findViewById(R.id.county);
        street = findViewById(R.id.street);
        community = findViewById(R.id.community);
        quality_button_group =findViewById(R.id.quality_button_group);
        use_button = findViewById(R.id.use_button);
        update_button =findViewById(R.id.update_button);
        unusable_button =findViewById(R.id.update_button);
        more_information =findViewById(R.id.more_information);

        imageView = new ImageView[3];
        imageView[0] =  findViewById(R.id.image_1);
        imageView[1] =  findViewById(R.id.image_2);
        imageView[2] =  findViewById(R.id.image_3);
        dealPhoto = findViewById(R.id.deal_photo);

        save_point_details = findViewById(R.id.save_point_details);
        save_all_points_details = findViewById(R.id.save_all_points_details);
        cancel_point_details = findViewById(R.id.cancel_point_details);

        MapView mMapView =  findViewById(R.id.detail_map_view);

        geometryType = getIntent().getStringExtra("geometryType");
        /////////////////////////////////////////////////////////////////////////////////////////
        //地图相关
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
        /////////////////////////////////////////////////////////////////////////////////////////
        quality_button_group.check(R.id.use_button);//默认选择第一个
        quality_button_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.use_button:
                        facilityQuality= 1;
                        break;
                    case R.id.update_button:
                        facilityQuality = 0;
                        break;
                    case R.id.unusable_button:
                        facilityQuality = -1;
                        break;
                    default:
                        break;
                }
            }
        });



        dealPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] strArray = new String[]{"点我拍照","选择照片"};
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(RecordDetailActivity.this);//实例化builder
                //builder.setIcon(R.mipmap.ic_launcher);//设置图标
                //builder.setTitle("设施点类型");//设置标题
                //设置列表
                builder.setItems(strArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //改变照片文件下标
                        imageViewIndex++;

                        if(which == 1){
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            // 打开相册
                            startActivityForResult(intent, CHOOSE_PHOTO);
                        }else{
                            //新建一个File，传入文件夹目录
                            File file = new File(
                                    Environment.getExternalStorageDirectory()
                                            + "/GPSRecorder");
                            //判断文件夹是否存在，如果不存在就创建，否则不创建
                            if (!file.exists()) {
                                //通过file的mkdirs()方法创建<span style="color:#FF0000;">
                                // 目录中包含却不存在</span>的文件夹
                                file.mkdirs();
                            }
                            // 创建File对象，用于存储拍照后的图片，并存放在SD卡的根目录下
                            String imagePath =System.currentTimeMillis() +
                                    "my_img"+imageViewIndex%3+".jpg";
                            File outputImg = new File(file, imagePath);
                            imagePaths[imageViewIndex%3] = imagePath;
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
                    }
                });
                builder.create().show();//创建并显示对话框
            }
        });

        //保存按钮
        save_point_details.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndFocus(scene1)){
                    if (checkAndFocus(scene2)){
                        if (checkAndFocus(facilityType)){
                            if (checkAndFocus (county)){
                                if (checkAndFocus(street)){
                                    if(checkAndFocus(community)){
                                        if (checkAndFocus(facilityAddress)){
                                            if (imageView[0].getDrawable() != null){
                                                PointDetails p = new PointDetails();
                                                p.setId((int)System.currentTimeMillis());
                                                p.setGeometrytype(geometryType);

                                                Coordinate coordinate =new Coordinate(
                                                        mLongitude,mLatitude);
                                                PointData pointData = new PointData(
                                                        scene1.getText().toString().trim(),
                                                        scene2.getText().toString().trim(),
                                                        facilityType.getText().toString().trim(),
                                                        county.getText().toString().trim(),
                                                        street.getText().toString().trim(),
                                                        community.getText().toString().trim(),
                                                        facilityAddress.getText().toString().trim(),
                                                        facilityQuality,
                                                        imagePaths,
                                                        coordinate,
                                                        more_information.getText().toString().trim()
                                                );
                                                ArrayList<PointData> all = new ArrayList<>();
                                                all.add(pointData);
                                                p.setData(all);
                                                Toast.makeText(RecordDetailActivity.this,
                                                        "保存成功",
                                                        Toast.LENGTH_SHORT).show();

                                            }else{
                                                Toast.makeText(RecordDetailActivity.this,
                                                        "请拍摄现场照片,最多拍摄三张。",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                }

            }
        });

        cancel_point_details.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                        final Bitmap bigBitmap = BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(mImgUri));

                        Bitmap smallBitmap = createScaledBitmap(bigBitmap,
                                22,40,true);
                        imageView[imageViewIndex%3].setImageBitmap(smallBitmap);
                        imageView[imageViewIndex%3].setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getBigPicture(bigBitmap);

                            }
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(mImgUri));
                        // 将裁剪后的照片显示出来
                        imageView[imageViewIndex%3].setImageBitmap(bitmap);
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
     * 4.4以上系统处理图片的方法(Android系统从4.4开始选取相册中的图片不再返回真实地Uri，
     * 而是一个封装过的Uri)
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
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        imagePaths[imageViewIndex%3] = imagePath;
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
        imagePaths[imageViewIndex%3] = imagePath;
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
            // 调用压缩方法压缩图片
            final Bitmap bigBitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap smallBitmap = createScaledBitmap
                    (bigBitmap,22,40,true);
            imageView[imageViewIndex%3].setImageBitmap(smallBitmap);
            imageView[imageViewIndex%3].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getBigPicture(bigBitmap);

                }
            });

        } else {
            Toast.makeText(RecordDetailActivity.this, "获取图片失败",
                    Toast.LENGTH_SHORT).show();
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
        Cursor cursor = getContentResolver().query(uri, null,
                selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 压缩图片
     *
     *
     */
    private Bitmap createThumbnail(String filepath, int i) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = i;
        return BitmapFactory.decodeFile(filepath, options);
    }

    /**
     * 判断网络是否正常
     *
     *
     */
    public boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 点击图片放大查看
     *
     */
    private void getBigPicture(Bitmap b) {
        LayoutInflater inflater = LayoutInflater.from(this);
        //加载自定义的布局文件
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        ImageView img = (ImageView) imgEntryView.findViewById(R.id.large_image);
        if (b != null) {
            Display display = RecordDetailActivity.this.getWindowManager()
                    .getDefaultDisplay();
            int scaleWidth = display.getWidth();
            int height = b.getHeight();// 图片的真实高度
            int width = b.getWidth();// 图片的真实宽度
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) img.getLayoutParams();
            lp.width = scaleWidth;// 调整宽度
            lp.height = (height * scaleWidth) / width;// 调整高度
            img.setLayoutParams(lp);
            img.setImageBitmap(b);
            dialog.setView(imgEntryView); // 自定义dialog
            dialog.show();
        }
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View paramView) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        });
    }


    public static Bitmap createScaledBitmap(Bitmap bitmap, int iconWidth,
                                            int iconHeight, boolean filter) {
        Bitmap bitmap2;
        try {
            bitmap2 = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, filter);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            System.gc();
            bitmap2 = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, filter);
        }
        return bitmap2;
    }

    /**
     * 判断输入框是否为空，若为空返回false并转移焦点至此输入框
     * @param e 对话框
     * @return 若为空返回false，不为空返回true
     */
    public boolean checkAndFocus(EditText e){
        if (e.getText().toString().trim().isEmpty()){
            e.setFocusable(true);
            e.setFocusableInTouchMode(true);
            e.requestFocus();
            return false;
        } else {
            return true;
        }
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
            mLongitude = point.getLongitudeE6();
            mLatitude =  point.getLatitudeE6();
            //more_information.setText("" + point.getLongitudeE6()+ " "+point.getLatitudeE6());
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
