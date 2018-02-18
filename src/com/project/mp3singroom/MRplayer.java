package com.project.mp3singroom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;

public class MRplayer extends Service {
	
	AudioTrack at;		// MR을 재생할 객체 생성
    String MRpath = "/mnt/sdcard/out.pcm";
    byte[] MR_buff;
    int MR_size;
    Thread play_thread;

    public void onCreate() { 
        super.onCreate(); 
        
        File file = new File(MRpath);
	    MR_size = (int) file.length();
	    MR_buff = new byte[MR_size];
	    FileInputStream in = null;

	    try {
			in = new FileInputStream( file );
			in.read(MR_buff);								// MR 파일을 버퍼로 불러온다.
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	    
		setAudioTrack();
    }
    
	public void setAudioTrack() {
		Music music = (Music) this.getApplicationContext();
		
		int intSize = android.media.AudioTrack.getMinBufferSize(music.getSampleRate(), AudioFormat.CHANNEL_CONFIGURATION_STEREO,
		AudioFormat.ENCODING_PCM_16BIT); 
		
		at = new AudioTrack(AudioManager.STREAM_MUSIC, music.getSampleRate(), AudioFormat.CHANNEL_CONFIGURATION_STEREO,
		AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM); 
		
		play_thread = new Thread(mRun);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && at !=  null) 
			play_thread.start();
		return START_STICKY;
	}
	public void onDestroy() {
    	if(at != null) {
    	    at.flush();
            at.stop(); 
            at.release(); 
            at = null;  
            play_thread = null;
    	}
    	super.onDestroy();
	}
	
	Runnable mRun = new Runnable() { 
		public void run() {
			at.play();
			at.write(MR_buff,0, MR_size);
		}
	};
}
