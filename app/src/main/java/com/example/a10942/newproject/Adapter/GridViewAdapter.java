package com.example.a10942.newproject.Adapter;

/**
 * Created by 10942 on 2017/6/23 0023.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.a10942.newproject.R;

import java.util.List;

/**
 * gridView的adapter
 */

public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList;
    private int selectorPosition;

    public GridViewAdapter(Context context, List<String> mList) {
        this.mContext = context;
        this.mList = mList;

    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mList != null ? mList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return mList != null ? position : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.item_gridview, null);
        RelativeLayout mRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.ll);
        TextView textView = (TextView) convertView.findViewById(R.id.tv);
        textView.setText(mList.get(position));
        //如果当前的position等于传过来点击的position,就去改变他的状态
        if (selectorPosition == position) {
            mRelativeLayout.setBackgroundResource(R.drawable.grid_shap_two);
            textView.setTextColor(Color.parseColor("#FF4081"));
        } else {
            //其他的恢复原来的状态
            mRelativeLayout.setBackgroundResource(R.drawable.grid_shap_one);
            textView.setTextColor(Color.parseColor("#3F51B5"));
        }
        return convertView;
    }


    public void changeState(int pos) {
        selectorPosition = pos;
        notifyDataSetChanged();

    }
}
