package com.project.mp3singroom;

import android.app.Dialog;
import android.content.Context;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Help extends Dialog implements OnClickListener {
	Button btn1;
	
	public Help(Context context){
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help_dialog);
		
		btn1=(Button)findViewById(R.id.helpbtn1);
		btn1.setOnClickListener(this);
	}

	public void onClick(View view){
		if(view==btn1){
			dismiss();
		}
	}
}

  