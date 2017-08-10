package com.example.sky;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import jc.sky.SKYHelper;
import jc.sky.view.SKYActivity;
import jc.sky.view.SKYBuilder;

public class AActivity extends SKYActivity<ABiz> {

	@BindView(R.id.tv_text) TextView tvText;

	@Override protected SKYBuilder build(SKYBuilder initialSKYBuilder) {
		initialSKYBuilder.layoutId(R.layout.activity_a);
		return initialSKYBuilder;
	}

	@Override protected void initData(Bundle savedInstanceState) {

	}

	@OnClick(R.id.tv_text) public void login(View view) {
		biz().login(view.getContext());
	}

	@OnClick(R.id.button) public void btn(View view) {
		biz().aaaaa();
	}

	public void fff(int i, Object a) {
		tvText.setText("a.toString()");
		SKYHelper.toast().show(a.toString());
	}
}
