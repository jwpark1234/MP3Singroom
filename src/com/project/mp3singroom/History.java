package com.project.mp3singroom;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class History extends Activity {
	
	private MediaPlayer mediaPlayer;
	TextView dt;
	Button start;
	SeekBar bar;
	
	RadioGroup radioGroup;
	RadioButton rb1;
	RadioButton rb2;
	
	ArrayList<String> pathList = new ArrayList<String>();
	String title, date, duration, song_path, origin;
	private final Handler handler = new Handler();
	ProgressDialog progressDialog;
	boolean result = false;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);	
		setContentView(R.layout.history);
		
		this.setTitle("MP3 노래방 > 나의 기록");
		        		
    	Music music = (Music)this.getApplicationContext();
    	
        title = music.getTitle();
        date = music.getDate();
        duration = music.getDuration();
        song_path = music.getPath();
        origin = music.getOrigin();
		
		start = (Button) findViewById(R.id.start);
        Button stop = (Button) findViewById(R.id.stop);
        
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
        
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        rb1 = (RadioButton)findViewById(R.id.rb1);
        rb2 = (RadioButton)findViewById(R.id.rb2);
        
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {         
            	if (radioGroup.getCheckedRadioButtonId() == R.id.rb1) {
            		setupPlayer(1);
            	} else if (radioGroup.getCheckedRadioButtonId() == R.id.rb2) {
            		setupPlayer(2);
            	}
            }
        });
        
        radioGroup.check(R.id.rb1);     
        
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() { //// 식바가 다되면 끝내게 변경
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
    }
    
    public void setupPlayer(int key) {
    	if(mediaPlayer != null) {
			mediaPlayer.stop();
    		start.setText("▶");
    		dt.setText("0:00");
    	}
    	    	
        Resources r = getResources();
    	BitmapDrawable mDefaultAlbumIcon = (BitmapDrawable)r.getDrawable(R.drawable.git);
		        
        View info = (View) findViewById(R.id.music_info);
        
		ImageView image  = (ImageView) info.findViewById(R.id.row_album_art);
		TextView tt = (TextView)info.findViewById(R.id.row_title);
		TextView at = (TextView)info.findViewById(R.id.row_album);
		dt = (TextView)info.findViewById(R.id.row_duration);
		    	
    	if(key == 1) {
    		tt.setText(title);
    		tt.setSelected(true);
    		at.setText(date);
    		dt.setText("0:00");
    		
    		image.setImageBitmap(mDefaultAlbumIcon.getBitmap());

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
    	}
    	else {
    		String[] mCursorCols = new String[]{
    	    		MediaStore.Audio.Media._ID,
    	    		MediaStore.Audio.Media.ARTIST,
    	    		MediaStore.Audio.Media.TITLE,
    	    		MediaStore.Audio.Media.ALBUM,
    	    		MediaStore.Audio.Media.ALBUM_ID,
    	    		MediaStore.Audio.Media.DURATION,
    	    		MediaStore.Audio.Media.DATA
	    	};
	    	
	    	Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	    	
	    	String selection = "_data LIKE \"" + origin + "\"";    		 

	    	Cursor cur = getContentResolver().query(uri, mCursorCols, selection, null, null);
	    	
	    	String title = "", artist = "", album = "";
    		int albumId = 0;
    		
	    	if( cur.moveToFirst() ){    		
	    		  		
	    		int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);		
	    		int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
	    		int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);	 		
	    		int albumIdColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);	 	
	    		int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
	    		
		   		title = cur.getString(titleColumn);
	    		artist = cur.getString(artistColumn);
	    		album = cur.getString(albumColumn);
	    		albumId = cur.getInt(albumIdColumn);
	    		duration = cur.getString(durationColumn);
	    	}
    		
    		tt.setText(artist + " - " + title);
    		tt.setSelected(true);
    		at.setText(album);
    		dt.setText("0:00");

			Bitmap albumArt = List_utils.getBitmapImage(getBaseContext(), albumId, 200, 200);
			if(albumArt != null)
				image.setImageBitmap(albumArt);
			else
				image.setImageBitmap(mDefaultAlbumIcon.getBitmap());
    		
    		mediaPlayer = new MediaPlayer();
    		
    		try {
    			mediaPlayer.setDataSource(origin);
    			mediaPlayer.prepare();
    		} catch (IllegalStateException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    	}
    } 
    
    public void startPlayProgressUpdater() {
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
    
	protected void onDestroy() {
		super.onDestroy();
		try {			
			mediaPlayer.stop();
			mediaPlayer.prepare();
			mediaPlayer.seekTo(0);
    		start.setText("▶");
    		dt.setText("0:00");
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
