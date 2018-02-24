package com.wangan.gpsrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.wangan.gpsrecorder.util.LocationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static android.graphics.BitmapFactory.decodeFile;


/**
 * Created by wangan on 2018/2/13.
 */

public class RecordDetailActivity extends AppCompatActivity {
    public static final String TAG = "RecordDetailActivity";
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;

    final Gson gson = new Gson();
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;
    SharedPreferences pref = null;

    int isReload = 0;//是否是修改模式，1为是 ，0为不是

    //新建设施点的几何类型
    String geometryType = "";

    private Uri mImgUri;

    //此activity中填写的所有信息
    PointDetails pointDetails = new PointDetails();
    //此activity中填写的所有信息（当保存的sharedperferences为空时，包装一下，方便存储）
    private
    //此activity中填写的点信息集合
    List<PointData> pointDataList = new ArrayList<>();

    //添加点按钮
    ImageButton addPointRecord;


    //图片路径
    ArrayList<String[]> imagePaths = new ArrayList<>();
    private ArrayList<Integer> imageViewIndex = new ArrayList<>();

    MorePagerAdapter morePagerAdapter;

    private List<View> viewList =new ArrayList<>();//view数组
    LayoutInflater inflater = null;

    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record_detail);
        tabLayout =  findViewById(R.id.tab_layout);
        vp_pager =  findViewById(R.id.tab_viewpager);
        addPointRecord = findViewById(R.id.add_point_record);
        inflater=getLayoutInflater();

        sharedPreferences = RecordDetailActivity.this.getSharedPreferences("data",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        pref = getSharedPreferences("data",MODE_PRIVATE);

        geometryType = getIntent().getStringExtra("geometryType");
        isReload = getIntent().getIntExtra("reload",0);
        if(geometryType.equals("point")){
            addPointRecord.setVisibility(View.GONE);
        }

        String unUploadData = getIntent().getStringExtra("unUploadData");

        if(isReload == 0){//当是新增初始化一个view
            pointDetails.setId((int)System.currentTimeMillis());
            pointDetails.setGeometrytype(geometryType);
            View view1 = inflater.inflate(R.layout.record_detail_layout,
                    null);
            viewList.add(view1);
            imageViewIndex.add(2);//增加一个图片下标
            imagePaths.add(new String[3]);
            pointDataList.add(new PointData());

        } else {//当是修改时初始化相应数量的view
            pointDetails = gson.fromJson(unUploadData,
                    new TypeToken<PointDetails>(){}.getType());
            //当是修改详细信息时，把数据重新写入输入框中
            pointDataList = pointDetails.getData();
            if(null != pointDataList){
                for(int i =0;i<pointDataList.size();++i){
                    View view1 = inflater.inflate(R.layout.record_detail_layout,
                            null);
                    viewList.add(view1);
                    imageViewIndex.add(2);//增加一个图片下标
                    imagePaths.add(new String[3]);
                }
            }
        }

        addPointRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 =  inflater.inflate(R.layout.record_detail_layout,
                        null);
                viewList.add(view2);
                morePagerAdapter.notifyDataSetChanged();
                imageViewIndex.add(2);//增加一个图片下标
                imagePaths.add(new String[3]);
                pointDataList.add(new PointData());
            }
        });

        initView();
    }
    private void initView() {
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        morePagerAdapter = new MorePagerAdapter();
        vp_pager.setAdapter(morePagerAdapter);
        tabLayout.setupWithViewPager(vp_pager);
    }
    class MorePagerAdapter extends PagerAdapter {
        private int position;//界面上主view的下标
        private View mCurrentView;//界面上主view

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            this.position = position;
            mCurrentView = (View)object;
        }

        public View getPrimaryItem() {
            return mCurrentView;
        }

        public int getPrimaryItemIndex() {
            return position;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            Log.d(TAG,"position: "+position);
            final Button dealPhoto,save_point_details,
                    save_all_points_details,cancel_point_details;
            //必填的信息
            ArrayList<EditText> allEdit = new ArrayList<>(10);
            final EditText scene1,scene2,facilityType,county,
                    street,community,facilityAddress;
            final RadioGroup quality_button_group;

            //选填信息
            final EditText more_information;

            //此view保存的信息
            final PointData pointData = new PointData();

            scene1 = viewList.get(position).findViewById(R.id.scene1);
            scene2 = viewList.get(position).findViewById(R.id.scene2);
            facilityType = viewList.get(position).findViewById(R.id.facility_type);
            facilityAddress = viewList.get(position).findViewById(R.id.facility_address);
            county = viewList.get(position).findViewById(R.id.county);
            street = viewList.get(position).findViewById(R.id.street);
            community = viewList.get(position).findViewById(R.id.community);

            allEdit.add(scene1);
            allEdit.add(scene2);
            allEdit.add(facilityType);
            allEdit.add(facilityAddress);
            allEdit.add(county);
            allEdit.add(street);
            allEdit.add(community);

            quality_button_group =viewList.get(position).findViewById(R.id.quality_button_group);
            more_information =viewList.get(position).findViewById(R.id.more_information);

            ImageView[] imageView = new ImageView[3];
            imageView[0] =  viewList.get(position).findViewById(R.id.image_1);
            imageView[1] =  viewList.get(position).findViewById(R.id.image_2);
            imageView[2] =  viewList.get(position).findViewById(R.id.image_3);
            dealPhoto = viewList.get(position).findViewById(R.id.deal_photo);

            save_point_details = viewList.get(position).findViewById(R.id.save_point_details);
            save_all_points_details = viewList.get(position).findViewById(R.id.save_all_points_details);
            cancel_point_details = viewList.get(position).findViewById(R.id.cancel_point_details);

            MapView mMapView =  viewList.get(position).findViewById(R.id.detail_map_view);

            if (geometryType.equals("point")) {
                save_all_points_details.setVisibility(View.GONE);
            }

            final MyOverlay myOverlay = new MyOverlay();

            if(null != pointDetails.getData() && pointDetails.getData().size()>=position+1
                    && null != pointDetails.getData().get(position) ){
                reloadData(pointDetails.getData().get(position),viewList.get(position),myOverlay);
            }else {
                //地图相关
                //设置启用内置的缩放控件
                mMapView.setBuiltInZoomControls(true);

                //得到mMapView的控制权,可以用它控制和驱动平移和缩放
                MapController mMapController = mMapView.getController();
                MyLocationOverlay myLocation = new MyLocationOverlay(RecordDetailActivity.this, mMapView);
                //myLocation.enableCompass();  //显示指南针
                myLocation.enableMyLocation(); //显示我的位置
                myOverlay.setGeoPoint(myLocation.getMyLocation());
                myOverlay.onTap(myLocation.getMyLocation(),mMapView);

                mMapView.addOverlay(myLocation);
                mMapView.addOverlay(myOverlay);
                mMapController.setCenter(myLocation.getMyLocation());
                //设置地图zoom级别
                mMapController.setZoom(15);
                quality_button_group.check(R.id.use_button);//默认选择第一个
            }

            quality_button_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (group.getCheckedRadioButtonId()) {
                        case R.id.use_button:
                            pointData.setQuality(1);
                            break;
                        case R.id.update_button:
                            pointData.setQuality(0);
                            break;
                        case R.id.unusable_button:
                            pointData.setQuality(-1);
                            break;
                        default:
                            break;
                    }
                }
            });

            dealPhoto.setOnClickListener(new View.OnClickListener() {
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
                                String imagePath = "my_img"+System.currentTimeMillis() +
                                        (imageViewIndex.get(position)+1)%3+".jpg";
                                File outputImg = new File(file, imagePath);
                                imagePaths.get(position)[(imageViewIndex.get(position)+1)%3] =
                                        outputImg.getAbsolutePath();
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

            if (geometryType.equals("point")) {
                //当是点的时候保存当前这个点，就结束采集
                save_point_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(checkAllEdit(viewList.get(position))){//所有出入框都输入
                            pointData.setCoordinate(myOverlay.getGeoPoint());
                            pointData.setScene1(scene1.getText().toString().trim());
                            pointData.setScene2(scene2.getText().toString().trim());
                            pointData.setFacilitytpye(facilityType.getText().toString().trim());
                            pointData.setCounty(county.getText().toString().trim());
                            pointData.setStreet(street.getText().toString().trim());
                            pointData.setCommunity(community.getText().toString().trim());
                            pointData.setFacilityaddress(facilityAddress.getText().toString().
                                    trim());
                            pointData.setOtherInformation(
                                    more_information.getText().toString().trim());
                            pointData.setImage(imagePaths.get(position));

                            pointDataList.set(position,pointData);
                            pointDetails.setData(pointDataList);

                            String unUploadDataJson = pref.getString
                                    ("UnUploadData","");
                            if(unUploadDataJson.equals("")){
                                List<PointDetails> newRecordDetail = new ArrayList<>();
                                newRecordDetail.add(pointDetails);
                                Log.d(TAG,"unUploadDataJson:"+
                                        gson.toJson(newRecordDetail));
                                editor.putString("UnUploadData",
                                        gson.toJson(newRecordDetail));
                                editor.apply();
                            } else{
                                List<PointDetails> allUnUploadData =
                                        gson.fromJson(unUploadDataJson,
                                                new TypeToken<List<PointDetails>>
                                                        (){}.getType());
                                if(isReload == 0){
                                    allUnUploadData.add(pointDetails);
                                }else{
                                    for (int i = allUnUploadData.size()-1;i>=0;--i){
                                        PointDetails tempPointDetails = allUnUploadData.get(i);
                                        if (tempPointDetails.getId() == pointDetails.getId()){
                                            allUnUploadData.remove(i);
                                            break;
                                        }
                                    }
                                    allUnUploadData.add(pointDetails);
                                }

                                Log.d(TAG,"unUploadDataJson:"+
                                        gson.toJson(allUnUploadData));
                                editor.putString("UnUploadData",
                                        gson.toJson(allUnUploadData));
                                editor.apply();
                            }
                            //创建退出对话框
                            AlertDialog.Builder isExit=new AlertDialog.
                                    Builder(RecordDetailActivity.this);
                            //设置对话框消息
                            isExit.setMessage("保存成功,是否上传");
                            // 添加选择按钮并注册监听
                            isExit.setPositiveButton("上传",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //TODO  上传模块
                                            RecordDetailActivity.this.finish();
                                        }
                                    });
                            isExit.setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            RecordDetailActivity.this.finish();
                                        }
                                    });
                            //对话框显示
                            isExit.show();
                        }else{
                            Toast.makeText(RecordDetailActivity.this,
                                    "还有必填项没有填写！"
                                    ,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else{//当是线面的时候，只是保存当前一个点的信息
                save_point_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(checkAllEdit(viewList.get(position))){//所有出入框都输入
                            pointData.setCoordinate(myOverlay.getGeoPoint());
                            pointData.setScene1(scene1.getText().toString().trim());
                            pointData.setScene2(scene2.getText().toString().trim());
                            pointData.setFacilitytpye(facilityType.getText().toString().trim());
                            pointData.setCounty(county.getText().toString().trim());
                            pointData.setStreet(street.getText().toString().trim());
                            pointData.setCommunity(community.getText().toString().trim());
                            pointData.setFacilityaddress(facilityAddress.getText().toString().
                                    trim());
                            pointData.setOtherInformation(
                                    more_information.getText().toString().trim());
                            pointData.setImage(imagePaths.get(position));
                            pointDataList.set(position,pointData);
                            Toast.makeText(RecordDetailActivity.this,
                                    "保存成功,请输入下一个设施点信息",
                                    Toast.LENGTH_SHORT).show();

                            //////////////////////////////////////////////
                            //为了跳转到下一个view
                            View view2 =  inflater.inflate(R.layout.record_detail_layout,
                                    null);
                            viewList.add(view2);

                            imageViewIndex.add(2);//增加一个图片下标
                            imagePaths.add(new String[3]);
                            pointDataList.add(new PointData());

                            morePagerAdapter.notifyDataSetChanged();
                            vp_pager.setCurrentItem(position+1);
                            /////////////////////////////////////////////

                        }else{
                            Toast.makeText(RecordDetailActivity.this,
                                    "还有必填项没有填写！"
                                    ,Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

            //结束此次采集
            save_all_points_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkAllEdit(viewList.get(position))){//所有出入框都输入
                        pointData.setCoordinate(myOverlay.getGeoPoint());
                        pointData.setScene1(scene1.getText().toString().trim());
                        pointData.setScene2(scene2.getText().toString().trim());
                        pointData.setFacilitytpye(facilityType.getText().toString().trim());
                        pointData.setCounty(county.getText().toString().trim());
                        pointData.setStreet(street.getText().toString().trim());
                        pointData.setCommunity(community.getText().toString().trim());
                        pointData.setFacilityaddress(facilityAddress.getText().toString().
                                trim());
                        pointData.setOtherInformation(
                                more_information.getText().toString().trim());
                        pointData.setImage(imagePaths.get(position));

                        pointDataList.set(position,pointData);
                        pointDetails.setData(pointDataList);

                        String unUploadDataJson = pref.getString
                                ("UnUploadData","");
                        if(unUploadDataJson.equals("")){
                            List<PointDetails> newRecordDetail = new ArrayList<>();
                            newRecordDetail.add(pointDetails);
                            Log.d(TAG,"unUploadDataJson:"+
                                    gson.toJson(newRecordDetail));
                            editor.putString("UnUploadData",
                                    gson.toJson(newRecordDetail));
                            editor.apply();
                        } else{
                            List<PointDetails> allUnUploadData =
                                    gson.fromJson(unUploadDataJson,
                                            new TypeToken<List<PointDetails>>
                                                    (){}.getType());
                            if(isReload == 0){
                                allUnUploadData.add(pointDetails);
                            }else{
                                if(isReload == 0){
                                    allUnUploadData.add(pointDetails);
                                }else{
                                    //TODO
                                    for (int i = allUnUploadData.size()-1;i>=0;--i){
                                        PointDetails tempPointDetails = allUnUploadData.get(i);
                                        if (tempPointDetails.getId() == pointDetails.getId()){
                                            allUnUploadData.remove(i);
                                            break;
                                        }
                                    }
                                    allUnUploadData.add(pointDetails);
                                }
                                allUnUploadData.add(pointDetails);
                            }
                            Log.d(TAG,"unUploadDataJson:"+
                                    gson.toJson(allUnUploadData));
                            editor.putString("UnUploadData",
                                    gson.toJson(allUnUploadData));
                            editor.apply();
                        }
                        //创建退出对话框
                        AlertDialog.Builder isExit=new AlertDialog.
                                Builder(RecordDetailActivity.this);
                        //设置对话框消息
                        isExit.setMessage("保存成功,是否上传");
                        // 添加选择按钮并注册监听
                        isExit.setPositiveButton("上传",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO  上传模块
                                        RecordDetailActivity.this.finish();
                                    }
                                });
                        isExit.setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RecordDetailActivity.this.finish();
                                    }
                                });
                        //对话框显示
                        isExit.show();
                    }else{
                        Toast.makeText(RecordDetailActivity.this,
                                "还有必填项没有填写！"
                                ,Toast.LENGTH_LONG).show();
                    }
                }
            });

            cancel_point_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            (container).addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "设施点" + (position+1);
        }
    }

    private void reloadData(PointData pointData, final View view,MyOverlay myOverlay){
        EditText myScene1= view.findViewById(R.id.scene1);
        myScene1.setText(pointData.getScene1());

        EditText myScene2 = view.findViewById(R.id.scene2);
        myScene2.setText(pointData.getScene2());

        EditText myFacilityType = view.findViewById(R.id.facility_type);
        myFacilityType.setText(pointData.getFacilitytpye());

        EditText myFacilityAddress = view.findViewById(R.id.facility_address);
        myFacilityAddress.setText(pointData.getFacilityaddress());

        EditText myCounty = view.findViewById(R.id.county);
        myCounty.setText(pointData.getCounty());

        EditText myStreet = view.findViewById(R.id.street);
        myStreet.setText(pointData.getStreet());

        EditText myCommunity = view.findViewById(R.id.community);
        myCommunity.setText(pointData.getCommunity());


        RadioGroup myQualityButtonGroup =view.findViewById(R.id.quality_button_group);

        switch(pointData.getQuality()){
            case 1:
                myQualityButtonGroup.check(R.id.use_button);
                break;
            case 0:
                myQualityButtonGroup.check(R.id.update_button);
                break;
            case  -1:
                myQualityButtonGroup.check(R.id.unusable_button);
                break;
            default:
                break;
        }

        EditText myMoreInformation =view.findViewById(R.id.more_information);
        myMoreInformation.setText(pointData.getOtherInformation());

        ImageView[] myImageView = new ImageView[3];
        myImageView[0] =  view.findViewById(R.id.image_1);
        myImageView[1] =  view.findViewById(R.id.image_2);
        myImageView[2] =  view.findViewById(R.id.image_3);
        final String[] myImagePaths = pointData.getImage();
        File file = new File(
                Environment.getExternalStorageDirectory()
                        + "/GPSRecorder");
        //判断文件夹是否存在
        if (!file.exists()) {
            file.mkdirs();
        }

        Glide
                .with(RecordDetailActivity.this)
                .load(myImagePaths[0])
                .into(myImageView[0]);
        myImageView[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBigPicture(myImagePaths[0]);
            }});

        Glide
                .with(RecordDetailActivity.this)
                .load(myImagePaths[1])
                .into(myImageView[1]);
        myImageView[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBigPicture(myImagePaths[1]);
            }});

        Glide
                .with(RecordDetailActivity.this)
                .load(myImagePaths[2])
                .into(myImageView[2]);
        myImageView[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBigPicture(myImagePaths[2]);
            }});

        MapView mMapView =  view.findViewById(R.id.detail_map_view);
        //地图相关
        //设置启用内置的缩放控件
        mMapView.setBuiltInZoomControls(true);
        //得到mMapView的控制权,可以用它控制和驱动平移和缩放
        MapController mMapController = mMapView.getController();

        GeoPoint location = new GeoPoint(pointData.getCoordinate().getLatitude(),
                pointData.getCoordinate().getLongitude());
        myOverlay.setGeoPoint(location);
        myOverlay.onTap(location,mMapView);

        mMapView.addOverlay(myOverlay);
        mMapController.setCenter(location);
        //设置地图zoom级别
        mMapController.setZoom(15);
    }


    /*
    * 检查所有输入框是否为空
    * @params view
    *         需要检查的view
    * @return boolean
    *         全部填有信息则返回true，否则返回false
    * */
    private boolean checkAllEdit(View view){
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.scene1))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.scene2))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.facility_type))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.facility_type))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.county))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.street))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.community))){
            return false;
        }
        if(!checkAndFocus((EditText)
                view.findViewById(R.id.facility_address))){
            return false;
        }
        ImageView tempImageView1 = view.findViewById(R.id.image_1);
        if (tempImageView1.getDrawable() == null){
            return false;
        }
        return true;
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
    protected void onStop(){
        super.onStop();
        /*if(!pointDataList.isEmpty()){
            pointDetails.setData(pointDataList);
            newRecordDetail.add(pointDetails);
            String unUploadDataJson = pref.getString("UnUploadData","");
            if(unUploadDataJson == null ||
                    unUploadDataJson.equals("")){
                Log.d(TAG,"unUploadDataJson:"+gson.toJson(newRecordDetail));
                editor.putString("UnUploadData",
                        gson.toJson(newRecordDetail));
                editor.apply();
            } else{
                List<PointDetails> allUnUploadData =
                        gson.fromJson(unUploadDataJson,
                                new TypeToken<List<PointDetails>>(){}.getType());
                allUnUploadData.addAll(newRecordDetail);
                Log.d(TAG,"unUploadDataJson:"+gson.toJson(allUnUploadData));
                editor.putString("UnUploadData",
                        gson.toJson(allUnUploadData));
                editor.apply();
            }
        }*/
    }



    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK) {
            //back key Constant Value: 4 (0x00000004)
            //创建退出对话框
            AlertDialog.Builder isExit=new AlertDialog.Builder(this);
            //设置对话框标题
            isExit.setTitle("消息提醒");
            //设置对话框消息
            isExit.setMessage("确定要退出吗");
            // 添加选择按钮并注册监听
            isExit.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecordDetailActivity.this.finish();
                }
            });
            isExit.setNegativeButton("取消", null);
            //对话框显示
            isExit.show();
        }
        return false;
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final RecordDetailActivity context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.
                                    READ_EXTERNAL_STORAGE,MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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
            final RecordDetailActivity context) {
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
                    Toast.makeText(RecordDetailActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //改变照片文件下标
                    int tempIndex = imageViewIndex.get(morePagerAdapter.getPrimaryItemIndex());
                    tempIndex++;
                    imageViewIndex.set(morePagerAdapter.getPrimaryItemIndex(),tempIndex);
                    try {
                        tempIndex = tempIndex%3;
                        switch (tempIndex){
                            case 0:
                                ImageView image1 = morePagerAdapter.getPrimaryItem().
                                        findViewById(R.id.image_1);
                                Glide
                                        .with(RecordDetailActivity.this)
                                        .load(mImgUri)
                                        .into(image1);
                                image1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getBigPicture(mImgUri);

                                    }
                                });
                                break;
                            case 1:
                                ImageView image2 = morePagerAdapter.getPrimaryItem().
                                        findViewById(R.id.image_2);
                                Glide
                                        .with(RecordDetailActivity.this)
                                        .load(mImgUri)
                                        .into(image2);
                                image2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getBigPicture(mImgUri);

                                    }
                                });

                                break;
                            case 2:
                                ImageView image3 = morePagerAdapter.getPrimaryItem().
                                        findViewById(R.id.image_3);
                                Glide
                                        .with(RecordDetailActivity.this)
                                        .load(mImgUri)
                                        .into(image3);
                                image3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getBigPicture(mImgUri);

                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
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
                        //imageView[imageViewIndex%3].setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //改变照片文件下标
                    int tempIndex = imageViewIndex.get(morePagerAdapter.getPrimaryItemIndex());
                    tempIndex++;
                    imageViewIndex.set(morePagerAdapter.getPrimaryItemIndex(),tempIndex);

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
        for(View v:viewList){
            Glide.clear(v);
        }
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
        imagePaths.get(morePagerAdapter.getPrimaryItemIndex())
                [imageViewIndex.get(morePagerAdapter.getPrimaryItemIndex())%3] = imagePath;


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
        imagePaths.get(morePagerAdapter.getPrimaryItemIndex())
                [imageViewIndex.get(morePagerAdapter.getPrimaryItemIndex())%3] = imagePath;
        displayImage(imagePath);
    }



    //计算压缩比率 android官方提供的算法
    public  int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //将当前宽和高 分别减小一半
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * 根据图片路径选择图片
     *
     * @param imagePath
     *            图片路径
     */
    private void displayImage(final String imagePath) {
        if (imagePath != null) {
            int tempIndex = imageViewIndex.get(morePagerAdapter.getPrimaryItemIndex())%3;
            switch (tempIndex){
                case 0:
                    ImageView image1 = morePagerAdapter.getPrimaryItem().
                            findViewById(R.id.image_1);
                    Glide
                            .with(RecordDetailActivity.this)
                            .load(imagePath)
                            .into(image1);


                    image1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getBigPicture(imagePath);

                        }
                    });
                    break;
                case 1:
                    ImageView image2 = morePagerAdapter.getPrimaryItem().
                            findViewById(R.id.image_2);
                    Glide
                            .with(RecordDetailActivity.this)
                            .load(imagePath)
                            .into(image2);
                    image2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getBigPicture(imagePath);

                        }
                    });
                    break;
                case 2:
                    ImageView image3 = morePagerAdapter.getPrimaryItem().
                            findViewById(R.id.image_3);
                    Glide
                            .with(RecordDetailActivity.this)
                            .load(imagePath)
                            .into(image3);
                    image3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getBigPicture(imagePath);
                        }
                    });
                    break;
                default:
                    break;
            }

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
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
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
        return decodeFile(filepath, options);
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
        return networkInfo != null;
    }

    /**
     * 点击图片放大查看
     *
     */
    private void getBigPicture(final String imagePath) {
        LayoutInflater inflater = LayoutInflater.from(this);
        //加载自定义的布局文件
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final ImageView img = imgEntryView.findViewById(R.id.large_image);

        Glide
                .with(RecordDetailActivity.this)
                .load(imagePath)
                .into(img);
        dialog.setView(imgEntryView); // 自定义dialog
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        });
    }

    /**
     * 点击图片放大查看
     *
     */
    private void getBigPicture(Uri  b) {
        LayoutInflater inflater = LayoutInflater.from(this);
        //加载自定义的布局文件
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final ImageView img = imgEntryView.findViewById(R.id.large_image);

        Glide
                .with(RecordDetailActivity.this)
                .load(b)
                .into(img);
        dialog.setView(imgEntryView); // 自定义dialog
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
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

    //清除输入框文字
    public void clearInput(List<EditText> list){
        if(list ==null || list.size() == 0){
            return;
        }
        for(EditText editText:list){
            editText.setText("");
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

        public Coordinate getGeoPoint(){
            return new Coordinate(mGeoPoint.getLongitudeE6(),mGeoPoint.getLatitudeE6());
        }


        @Override
        public boolean onTap(GeoPoint point, MapView mapView) {
            mGeoPoint = point;
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
            //mapView.getController().setCenter(mGeoPoint);
        }
    }
}