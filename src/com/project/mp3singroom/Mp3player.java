package com.project.mp3singroom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

public class Mp3player extends Activity {
	
	private MediaPlayer mediaPlayer;
	SeekBar bar;
	TextView dt;
	Button start;
	TextView lyric;
	
	String song_path;
	String lyric_path;
	
	ArrayList<Integer> time = new ArrayList<Integer>();
	ArrayList<String> line = new ArrayList<String>();
	
	private final Handler handler = new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3player);
        
        Resources r = getResources();
        BitmapDrawable defaultIcon = (BitmapDrawable)r.getDrawable(R.drawable.git);
        
    	Music music = (Music)this.getApplicationContext();
               
        String artist = music.getArtist();
        String title = music.getTitle();
        String album = music.getAlbumName();
        int albumId = music.getAlbumArtId();
        String duration = music.getDuration();
        song_path = music.getPath();
        lyric_path = music.getLyricPath();
        
        View info = (View) findViewById(R.id.music_info);
        
		ImageView image  = (ImageView) info.findViewById(R.id.row_album_art);
		TextView tt = (TextView)info.findViewById(R.id.row_title);
		TextView bt = (TextView)info.findViewById(R.id.row_album);
		dt = (TextView)info.findViewById(R.id.row_duration);
		
		tt.setText(artist + " - "+ title);
		tt.setSelected(true);
		bt.setText(album);
		dt.setText("0:00");
		
		Bitmap albumArt = List_utils.getBitmapImage(getBaseContext(), albumId, 200, 200);
		if(albumArt != null)
			image.setImageBitmap(albumArt);
		else
			image.setImageBitmap(defaultIcon.getBitmap());
		
		mediaPlayer = new MediaPlayer();
		
		try {
			mediaPlayer.setDataSource(song_path);
			mediaPlayer.prepare();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		start = (Button) findViewById(R.id.start);
        Button stop = (Button) findViewById(R.id.stop);
                
        bar = (SeekBar) findViewById(R.id.playbar);
        bar.setVisibility(ProgressBar.VISIBLE);
        bar.setProgress(0);
        bar.setMax(Integer.parseInt(duration));        
        bar.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		seekChange(v);
        		return false;
        	}
        });
        
		start.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if(mediaPlayer.isPlaying()) {
        				mediaPlayer.pause();
        				start.setText("▶");
        			}
        			else {	        				
        				mediaPlayer.start();
        				start.setText("ll");
        				startPlayProgressUpdater();
        			}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });
			
        stop.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if(bar.getProgress() == 0)
        				return;
        			
        			mediaPlayer.stop();
					mediaPlayer.prepare();
					mediaPlayer.seekTo(0);
	        		start.setText("▶");
	        		bar.setProgress(0);
	        		dt.setText("0:00");
				}  catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });
        
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				try {
					mediaPlayer.stop();
					mediaPlayer.prepare();
					mediaPlayer.seekTo(0);
					start.setText("▶");
					bar.setProgress(0);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
        });
        
        lyric = (TextView) findViewById(R.id.lyrics);
        lyric.setMovementMethod(ScrollingMovementMethod.getInstance());
	       
        if(lyric_path != null)
        	printLyrics();
		        
        music.time = time;
        music.line = line;
    }
    
    public void startPlayProgressUpdater() {
    	if(mediaPlayer == null)
    		return;
    	
    	if(mediaPlayer.isPlaying()) {
			bar.setProgress(mediaPlayer.getCurrentPosition());
			
			int m = mediaPlayer.getCurrentPosition()/60000;
			int s = (mediaPlayer.getCurrentPosition()%60000)/1000;
			if(s < 10) {
				dt.setText(m + ":0" + s);
			}
			else {
				dt.setText(m + ":" + s);
			}
			
			Runnable notification = new Runnable() {
				
				public void run() {
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
    	}
    }
    
    private void seekChange(View v) {
		SeekBar sb = (SeekBar) v;
		mediaPlayer.seekTo(sb.getProgress());
    }

   	public void printLyrics() {
		BufferedReader br = null;
		try {
			// 가사파일을 연다.
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(lyric_path)),"MS949"));
			
			String temp;
			StringBuilder contentGetter = new StringBuilder();
			int prev_sec = 0;
			temp = br.readLine();
					
   			for (; temp != null; temp = br.readLine()) {		// 한줄씩 읽는다.
   				if(temp.charAt(1) == '0') {
   					if((Integer.parseInt(temp.substring(4, 6)) - prev_sec) > 5)
   						contentGetter.append('\n');
   					if((Integer.parseInt(temp.substring(4, 6)) - prev_sec) > 10)
   						contentGetter.append('\n');
   					prev_sec = Integer.parseInt(temp.substring(4, 6));
   					  		   				
   					int temp_time = Integer.parseInt(temp.substring(2, 3)) * 60000 + Integer.parseInt(temp.substring(4, 6)) * 1000 + Integer.parseInt(temp.substring(7, 8)) * 100;
   					time.add(temp_time);
   					
   					String temp_line = temp.substring(10).replace("/", "\n");
   					line.add(temp_line);
   					
   					contentGetter.append(temp_line + '\n');
   				}
   			}
   			lyric.setText(contentGetter.toString());	
		}
           
		// 입출력 관련된 예외 처리
		catch (IOException e) {
			String exceptionMessage = "파일을 읽는 도중에 오류가 발생했습니다.";
			Toast.makeText(getApplicationContext(), exceptionMessage, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    
		// 기타 예외 처리
		catch (Exception e) {
			Toast.makeText(getApplicationContext(), "알 수 없는 오류입니다.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    
		// 파일을 닫을 때에도 try ... catch 문 작성을 해야 합니다.
		try {
			if (null != br)
				br.close();
		}
		catch (IOException e) {
			Toast.makeText(getApplicationContext(), "파일을 닫을 수 없습니다.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
   	
	protected void onPause() {
		super.onPause();
		if(mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			start.setText("▶");
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
