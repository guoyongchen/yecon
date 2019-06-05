package com.carocean.operateintro;

import java.util.ArrayList;
import java.util.List;

import com.carocean.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class OperateIntroMainFragment extends Fragment implements AdapterView.OnItemClickListener{
	private GridView mGridView;
	private int mListTitleId[] = { R.string.operate_intro_icon_title_key, R.string.operate_intro_icon_title_panel,
			R.string.operate_intro_icon_title_fxp, R.string.operate_intro_icon_title_airconditioner,
			R.string.operate_intro_icon_title_driving, R.string.operate_intro_icon_title_oil,
			R.string.operate_intro_icon_title_braking, R.string.operate_intro_icon_title_service };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_operateintro_main, container, false);
		initViews(view);
		return view;
	}

	private void initViews(View parent) {
		mGridView = (GridView) parent.findViewById(R.id.operate_intro_gridview);

		mGridView.setOnItemClickListener(this);
		List<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < mListTitleId.length; i++) {
			list.add(mListTitleId[i]);
		}

		OperateIntroGridViewAdapter adapter = new OperateIntroGridViewAdapter(this.getActivity(), list);

		mGridView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		OperateIntroActivity activity = (OperateIntroActivity)getActivity();
		activity.onItemClick(position);
	}
}
