package com.wangan.gpsrecorder.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wangan.gpsrecorder.R;

import java.util.List;

/**
 * Created by 10394 on 2018-02-05.
 */

public class PointDetailsAdapter extends ArrayAdapter<PointDetails> {
    private int resourceId;
    /**
     *context:当前活动上下文
     *textViewResourceId:ListView子项布局的ID
     *objects：要适配的数据
     */
    public PointDetailsAdapter(Context context, int textViewResourceId,
                        List<PointDetails> objects) {
        super(context, textViewResourceId, objects);
        //拿取到子项布局ID
        resourceId = textViewResourceId;
    }

    /**
     * LIstView中每一个子项被滚动到屏幕的时候调用
     * position：滚到屏幕中的子项位置，可以通过这个位置拿到子项实例
     * convertView：之前加载好的布局进行缓存
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PointDetails PointDetails = getItem(position);  //获取当前项的Fruit实例
        //为子项动态加载布局
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView facility_geometry_type =  view.findViewById(R.id.facility_geometry_type);
        TextView facility_id =  view.findViewById(R.id.facility_id);
        facility_geometry_type.setText(PointDetails.getGeometrytype());
        facility_id.setText(""+PointDetails.getId());
        return view;
    }

}
