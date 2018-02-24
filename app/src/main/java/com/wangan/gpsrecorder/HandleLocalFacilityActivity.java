package com.wangan.gpsrecorder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wangan.gpsrecorder.model.PointDetails;
import com.wangan.gpsrecorder.model.PointDetailsAdapter;

import java.io.FileNotFoundException;
import java.util.List;

public class HandleLocalFacilityActivity extends AppCompatActivity {
    public final static int RELOAD = 1;

    ListView unUploadData;
    final Gson gson = new Gson();
    SharedPreferences pref = null;
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;

    PointDetailsAdapter adapter = null;
    List<PointDetails> allUnUploadData = null;

    //int age = pref.getInt(“age”,0);//第二个参数为默认值
    //boolean married = pref.getBoolean(“married”,false);//第二个参数为默认值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_local_facility);
        unUploadData = findViewById(R.id.un_upload_data);
        sharedPreferences = HandleLocalFacilityActivity.
                this.getSharedPreferences("data",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        pref = getSharedPreferences("data",MODE_PRIVATE);
        String unUploadDataJson = pref.getString("UnUploadData","");
        if(unUploadDataJson.equals("")){
            Toast.makeText(this,"没有未上传的设施信息",Toast.LENGTH_LONG).show();
        } else {
            allUnUploadData = gson.fromJson(unUploadDataJson,
                            new TypeToken<List<PointDetails>>(){}.getType());
            adapter = new PointDetailsAdapter(
                    HandleLocalFacilityActivity.this, R.layout.facility_item,
                    allUnUploadData);
            unUploadData.setAdapter(adapter);

            unUploadData.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //重新填写
                    Intent intent = new Intent(HandleLocalFacilityActivity.this,
                            RecordDetailActivity.class);
                    intent.putExtra("unUploadData",gson.toJson(allUnUploadData.get(position)));
                    intent.putExtra("geometryType",allUnUploadData.get(position).getGeometrytype());
                    intent.putExtra("reload",1);
                    startActivityForResult(intent,RELOAD);
                }
            });
            unUploadData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    initPopWindow(view, i);
                    return true;
                }
            });
        }

    }

    private void initPopWindow(View v , final int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_popup, null, false);
        Button btn_upload = view.findViewById(R.id.btn_upload);
        Button btn_delete = view.findViewById(R.id.btn_delete);
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.anim.anim_pop);  //设置加载动画

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效


        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown(v, 700, -200);

        //设置popupWindow里的按钮的事件
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HandleLocalFacilityActivity.this, "上传中。。。",
                        Toast.LENGTH_SHORT).show();
                //TODO
                popWindow.dismiss();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO
                AlertDialog.Builder adb=new AlertDialog.Builder(
                        HandleLocalFacilityActivity.this);
                adb.setMessage("确定删除此条设施信息？");
                final int positionToRemove = position;
                adb.setNegativeButton("取消", null);
                adb.setPositiveButton("确认", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        allUnUploadData.remove(positionToRemove);
                        editor.putString("UnUploadData",
                                gson.toJson(allUnUploadData));
                        editor.apply();

                        adapter.notifyDataSetChanged();
                    }});
                adb.show();
                popWindow.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RELOAD:
                if (resultCode == RESULT_OK) {
                    String unUploadDataJson = pref.getString("UnUploadData","");
                    allUnUploadData = gson.fromJson(unUploadDataJson,
                            new TypeToken<List<PointDetails>>(){}.getType());
                    adapter = new PointDetailsAdapter(
                            HandleLocalFacilityActivity.this, R.layout.facility_item,
                            allUnUploadData);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }
}
