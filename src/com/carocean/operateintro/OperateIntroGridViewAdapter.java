package com.carocean.operateintro;

import java.util.List;

import com.carocean.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OperateIntroGridViewAdapter extends BaseAdapter{
	private Context mContext;
    private List<Integer> mListTitleIds;

    public OperateIntroGridViewAdapter(Context context, List<Integer> listItem) {
        mListTitleIds = listItem;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListTitleIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mListTitleIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_operateintro_icon_view, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.operate_intro_icon_title);
        textView.setText(mListTitleIds.get(position));
        return convertView;
    }

}
