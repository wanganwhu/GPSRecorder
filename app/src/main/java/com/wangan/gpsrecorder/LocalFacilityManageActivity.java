package com.wangan.gpsrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LocalFacilityManageActivity extends AppCompatActivity {
    Button handleLocalFacility,addLocalFacility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_facility_manage);
        handleLocalFacility = (Button)findViewById(R.id.handle_local_facility);
        addLocalFacility = (Button)findViewById(R.id.add_local_facility);




        addLocalFacility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] strArray = new String[]{"点","线","面"};
                final String[] strArray2 = new String[]{"point","polyline","polygon"};
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(LocalFacilityManageActivity.this);//实例化builder
               //builder.setIcon(R.mipmap.ic_launcher);//设置图标
                builder.setTitle("设施点类型");//设置标题
                //设置列表
                builder.setItems(strArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Toast.makeText(LocalFacilityManageActivity.this,
                                strArray[which],Toast.LENGTH_SHORT).show();*/
                        Intent intent = new Intent(LocalFacilityManageActivity.this,
                                RecordDetailActivity.class);
                        intent.putExtra("geometryType",strArray2[which]);
                        startActivity(intent);
                    }
                });
                builder.create().show();//创建并显示对话框


            }
        });
    }

}
