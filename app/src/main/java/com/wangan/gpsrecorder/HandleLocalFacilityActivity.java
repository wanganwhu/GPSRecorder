package com.wangan.gpsrecorder;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wangan.gpsrecorder.model.PointDetails;
import com.wangan.gpsrecorder.model.PointDetailsAdapter;

import java.util.List;

public class HandleLocalFacilityActivity extends AppCompatActivity {
    ListView unUploadData;
    final Gson gson = new Gson();
    SharedPreferences pref = null;

    //int age = pref.getInt(“age”,0);//第二个参数为默认值
    //boolean married = pref.getBoolean(“married”,false);//第二个参数为默认值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_local_facility);
        unUploadData = findViewById(R.id.un_upload_data);
        pref = getSharedPreferences("data",MODE_PRIVATE);
        String unUploadDataJson = pref.getString("UnUploadData","");
        if(unUploadDataJson == null ||
                unUploadDataJson.equals("")){
            Toast.makeText(this,"没有未上传的设施信息",Toast.LENGTH_LONG).show();
        } else {
            List<PointDetails> allUnUploadData = gson.fromJson(unUploadDataJson,
                            new TypeToken<List<PointDetails>>(){}.getType());
            PointDetailsAdapter adapter = new PointDetailsAdapter(
                    HandleLocalFacilityActivity.this, R.layout.facility_item,
                    allUnUploadData);
            unUploadData.setAdapter(adapter);
        }
    }
}
