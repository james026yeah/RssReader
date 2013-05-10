package com.james026yeah.rssreader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.james026yeah.rssreader.R;

public class LoadingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_layout);
	}
	
	Handler mHandle = new Handler() {
		public void handleMessage(android.os.Message msg) {
		Intent intent = new Intent(getApplicationContext(), RssReader.class);
		startActivity(intent);
		finish();
		};
	};
	@Override
	protected void onResume() {
		super.onResume();
		mHandle.sendEmptyMessageDelayed(1, 2000);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
}
