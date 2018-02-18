package com.project.mp3singroom;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private boolean m_bFlag = false;
	private Handler m_hHandler;
	Help dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setBackgroundResource(R.drawable.background);
        
		m_hHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0) {
					m_bFlag = false;
				}
			}
		};
	
		
        Button mp3playerBtn = (Button) findViewById(R.id.mp3player);
        Button historyBtn = (Button) findViewById(R.id.history);
        Button helpBtn = (Button) findViewById(R.id.help); 
        
    	Drawable alpha1 = mp3playerBtn.getBackground();
		alpha1.setAlpha(255);
		
        
        mp3playerBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(getBaseContext(), SongList.class);
        		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        		startActivity(intent);
        	}
        });
        historyBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(getBaseContext(), HistoryList.class);
        		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        		startActivity(intent);
        	}
        });
        helpBtn.setOnClickListener(this);
    }
    public void onClick(View v) {
    	if(v.getId() == R.id.help) {
    		dialog = new Help(this);
    		dialog.show();
    	}
    }
    
    public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed(); //지워야 실행됨

		if(!m_bFlag) {
			Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
			m_bFlag = true;
			m_hHandler.sendEmptyMessageDelayed(0, 2000);

		}
		else {
			finish();
		}
	} 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
