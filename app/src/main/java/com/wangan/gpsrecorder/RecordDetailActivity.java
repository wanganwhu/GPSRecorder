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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;


public class RecordDetailActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;

    private Button mButtonTake, mButtonChoose, getmButtonGetLocation;
    private TextView location_information;
    private ImageView mImageView;
    private Uri mImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detial);

        mImageView =  findViewById(R.id.iv_img1);
        mButtonTake =  findViewById(R.id.btn_take_photo);
        mButtonChoose =  findViewById(R.id.btn_choose_photo);
        getmButtonGetLocation =  findViewById(R.id.get_location);
        location_information = findViewById(R.id.location_information);

        /*if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
        }else{
            checkPermissionREAD_EXTERNAL_STORAGE(this);
        }*/


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

        getmButtonGetLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = LocationUtils.getInstance( RecordDetailActivity.this ).showLocation();
                if (location != null) {
                    String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
                    Log.d( "FLY.LocationUtils", address );
                    location_information.setText( address );
                }

            }
        });
    }

    @Override
    protected  void onResume(){
        super.onResume();
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
                            Manifest.permission.READ_EXTERNAL_STORAGE);

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
                            Manifest.permission.ACCESS_FINE_LOCATION);

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
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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
                    Toast.makeText(RecordDetailActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
