package com.project.mp3singroom;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TabHost; 
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;   

public class TabLayout extends TabActivity {
	ProgressDialog progressDialog;
	boolean result = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		this.setTitle("MP3 노래방 > 노래방");

		Music music = (Music) this.getApplicationContext();
		createThreadAndDialog(music.getPath());
		
	}
	
	private void createThreadAndDialog(final String path){
		progressDialog = ProgressDialog.show(this,"","잠시만 기다려주세요...",true);  
		Thread thread = new Thread(new Runnable() {
			public void run() {
               // 시간걸리는 처리 부분
				decoding deco = new decoding(getApplicationContext(), path);
				fristhandler.sendEmptyMessage(0);
            }
        });
        thread.start();
	}

	private Handler fristhandler = new Handler() {
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			setContents();
		}    
	};
	
	private void setContents() {
		final TabHost tabHost = getTabHost();
		
		Intent intent1 = new Intent(this, Mp3player.class);
		Intent intent2 = new Intent(this, Sing.class);
		
		intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("", getResources().getDrawable(R.drawable.head)).setContent(intent1));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("", getResources().getDrawable(R.drawable.mic)).setContent(intent2));
	}
}
