/*
package com.wangan.gpsrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.wangan.gpsrecorder.model.Coordinate;
import com.wangan.gpsrecorder.model.PointData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.wangan.gpsrecorder.RecordDetailActivity.CHOOSE_PHOTO;
import static com.wangan.gpsrecorder.RecordDetailActivity.TAKE_PHOTO;

*/
/**
 * Created by 10394 on 2018-02-28.
 *//*


public class FragmentView extends Fragment {

    int imageViewIndex = 2;//图片下标
    String[] imagePaths = new String[3];
    private Uri mImgUri;

    private Bundle arg;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arg=getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.record_detail_layout,null);
        final Button dealPhoto,save_point_details, save_all_points_details,cancel_point_details;
        //必填信息
        final EditText scene1,scene2,facilityType,county,street,community,facilityAddress;
        final RadioGroup quality_button_group;
        //选填信息
        final EditText more_information;
        //此view保存的信息
        final PointData pointData = new PointData();

        int page=arg.getInt("pager_num");
        String geometryType = arg.getString("geometryType");


        scene1 = view.findViewById(R.id.scene1);
        scene2 = view.findViewById(R.id.scene2);
        facilityType = view.findViewById(R.id.facility_type);
        facilityAddress = view.findViewById(R.id.facility_address);
        county = view.findViewById(R.id.county);
        street = view.findViewById(R.id.street);
        community = view.findViewById(R.id.community);
        quality_button_group =view.findViewById(R.id.quality_button_group);
        more_information =view.findViewById(R.id.more_information);

        ImageView[] imageView = new ImageView[3];
        imageView[0] =  view.findViewById(R.id.image_1);
        imageView[1] =  view.findViewById(R.id.image_2);
        imageView[2] =  view.findViewById(R.id.image_3);
        dealPhoto = view.findViewById(R.id.deal_photo);

        save_point_details = view.findViewById(R.id.save_point_details);
        save_all_points_details = view.findViewById(R.id.save_all_points_details);
        cancel_point_details = view.findViewById(R.id.cancel_point_details);

        if (geometryType.equals("point")) {
            save_all_points_details.setVisibility(View.GONE);
        }

        MapView myMapView =  view.findViewById(R.id.detail_map_view);
        final BaiduMap mBaiduMap = myMapView.getMap();
        //TODO
        //baiduMaps.put(mBaiduMap,false);

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                pointData.setCoordinate(new Coordinate((int)latLng.longitudeE6,
                        (int)latLng.latitudeE6));
                updateMapState(mBaiduMap,latLng);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                pointData.setCoordinate(new Coordinate((int)mapPoi.getPosition().longitudeE6,
                        (int)mapPoi.getPosition().latitudeE6));
                updateMapState(mBaiduMap,mapPoi.getPosition());
                return false;
            }
        });

        if(null != pointDetails.getData() && pointDetails.getData().size()>=position+1
                && null != pointDetails.getData().get(position) ){
            reloadData(pointDetails.getData().get(position),viewList.get(position),mBaiduMap);
            updateMapState(mBaiduMap,new LatLng(
                    (double)(pointDetails.getData()
                            .get(position).getCoordinate().getLatitude()/1000000),
                    (double)pointDetails.getData()
                            .get(position).getCoordinate().getLongitude()/1000000));//定位
        }else {
            quality_button_group.check(R.id.use_button);//默认选择第一个


            Log.d("lala1","mCurrentLon:"+mCurrentLon+" mCurrentLat:"+mCurrentLat);
            setUserMapCenter(
                    new Coordinate((int)(mCurrentLon*1000000),(int)(mCurrentLon*1000000))
                    ,mBaiduMap);//设置地图中心点
            updateMapState(mBaiduMap,new LatLng(mCurrentLat,mCurrentLon));//定位

            //定位成功后写入pointData
            pointData.setCoordinate(new Coordinate((int)(mCurrentLon*1000000),
                    (int)(mCurrentLat*1000000)));

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
                //TODO
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(getActivity());//实例化builder
                //builder.setIcon(R.mipmap.ic_launcher);//设置图标
                //builder.setTitle("设施点类型");//设置标题
                //设置列表
                builder.setItems(strArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 1){
                            Intent intent = new Intent();
                            intent.setType("image*/
/*");
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
                                    imageViewIndex%3+".jpg";
                            File outputImg = new File(file, imagePath);
                            imagePaths[imageViewIndex%3] =
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
                            mImgUri = FileProvider.getUriForFile(getActivity(),
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
                    if(checkAllEdit(view)){//所有出入框都输入
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
                        pointData.setImage(imagePaths);

                        //pointDataList.set(position,pointData);
                        //pointDetails.setData(pointDataList);


                        //创建退出对话框
                        AlertDialog.Builder isExit=new AlertDialog.
                                Builder(getActivity());
                        //设置对话框消息
                        isExit.setMessage("保存成功,是否上传");
                        // 添加选择按钮并注册监听
                        isExit.setPositiveButton("上传",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO  上传模块
                                        showProgress(true);
                                        mAuthTask = new RecordUploadTask(gson.toJson(pointDetails));
                                        mAuthTask.execute((Void) null);
                                        //RecordDetailActivity.this.finish();
                                    }
                                });
                        isExit.setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                        vp_pager.setCurrentItem(viewList.size()-1);
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

                if (viewList.size() < 3 && geometryType.equals("polygon")){
                    Toast.makeText(RecordDetailActivity.this,
                            "至少输入三个设施点"
                            ,Toast.LENGTH_LONG).show();
                    return;
                }

                if(checkAllEdit(viewList.get(position))){//所有出入框都输入
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
                                    showProgress(true);
                                    mAuthTask = new RecordUploadTask(gson.toJson(pointDetails));
                                    mAuthTask.execute((Void) null);
                                }
                            });
                    isExit.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //存放在sharedPreference中
                                    String unUploadDataJson = pref.getString
                                            ("UnUploadData","");
                                    //若手机内保存为空，则新建数组包装一下
                                    if(unUploadDataJson.equals("")){
                                        List<PointDetails> newRecordDetail = new ArrayList<>();
                                        newRecordDetail.add(pointDetails);
                                        editor.putString("UnUploadData",
                                                gson.toJson(newRecordDetail));
                                        editor.apply();
                                    } else{
                                        List<PointDetails> allUnUploadData =
                                                gson.fromJson(unUploadDataJson,
                                                        new TypeToken<List<PointDetails>>
                                                                (){}.getType());

                                        if(isReload == 0){//若是新建则加入本地数组中
                                            allUnUploadData.add(pointDetails);
                                        }else{//是修改得把数组中的删了再加入
                                            for (int i = allUnUploadData.size()-1;i>=0;--i){
                                                PointDetails tempPointDetails =
                                                        allUnUploadData.get(i);
                                                if (tempPointDetails.getId() ==
                                                        pointDetails.getId()){
                                                    allUnUploadData.remove(i);
                                                    break;
                                                }
                                            }
                                            allUnUploadData.add(pointDetails);
                                        }
                                        editor.putString("UnUploadData",
                                                gson.toJson(allUnUploadData));
                                        editor.apply();
                                    }

                                    RecordDetailActivity.this.finish();
                                }
                            });
                    //对话框显示
                    isExit.show();
                }else{
                    */
/*Toast.makeText(RecordDetailActivity.this,
                            "还有必填项没有填写！"
                            ,Toast.LENGTH_LONG).show();*//*

                }
            }
        });

        cancel_point_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                //finish();
            }
        });

        return view;
    }


    public static FragmentView newInstance(Bundle args) {
        FragmentView fragment = new FragmentView();
        fragment.setArguments(args);
        return fragment;
    }

    */
/**
     * 更新地图状态显示面板
     *//*

    private void updateMapState(BaiduMap mBaiduMap, LatLng currentPt) {
        if (currentPt == null) {
        } else {
            MarkerOptions ooA = new MarkerOptions().position(currentPt).icon(bdA);
            mBaiduMap.clear();
            mBaiduMap.addOverlay(ooA);

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(currentPt).zoom(16.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    */
/*
    * 检查所有输入框是否为空
    * @params view
    *         需要检查的view
    * @return boolean
    *         全部填有信息则返回true，否则返回false
    * *//*

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

    */
/**
     * 判断输入框是否为空，若为空返回false并转移焦点至此输入框
     * @param e 对话框
     * @return 若为空返回false，不为空返回true
     *//*

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
}*/
