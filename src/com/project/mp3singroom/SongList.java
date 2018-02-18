package com.project.mp3singroom;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;
import android.content.Context;
import android.view.LayoutInflater; 
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import java.io.File;
import java.util.ArrayList;

public class SongList extends ListActivity {
	private ListView listview;	
	private EditText keyword;
	private Button search;
	private String key;
	private ArrayList<Music> songs;
	private MusicInformation songList;
	private Music[] music;
	ArrayList<String> pathList = new ArrayList<String>();
	ProgressDialog progressDialog;
	boolean IsFirst = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songlist);
        
        this.setTitle("MP3 노래방 > 노래방");
        		
        keyword = (EditText) findViewById(R.id.keyword);
        keyword.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        keyword.setInputType(InputType.TYPE_CLASS_TEXT);
        
        search = (Button) findViewById(R.id.search);
        
        search.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);   
        		imm.hideSoftInputFromWindow(keyword.getWindowToken(),0);
        		
        		key = keyword.getText().toString();
        		updateSongList(key);
        	}
	    });
        
        keyword.setOnEditorActionListener(new OnEditorActionListener() { 
        	@Override
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        		switch (actionId) {
        			case EditorInfo.IME_ACTION_SEARCH:
        				key = keyword.getText().toString();
        				updateSongList(key);
        				break;
        			default:
        				return false;
        		}
        		return true;
        	}
        });
        
        createThreadAndDialog();
    }
    
	private void createThreadAndDialog(){
		progressDialog = ProgressDialog.show(this,"","리스트를 불러오는 중...",true);  
		Thread thread = new Thread(new Runnable() {
			public void run() {
               // 시간걸리는 처리 부분
				listview = getListView();
				updateSongList("");
				fristhandler.sendEmptyMessage(0);
            }
        });
        thread.start();
	}

	private Handler fristhandler = new Handler() {
		public void handleMessage(Message msg) {

		    setListAdapter(songList);
		    IsFirst = false;
			progressDialog.dismiss();
		}    
	};
	
    public void updateSongList(String key){
  		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory())));

		songs = new ArrayList<Music>();
	
    	music = new Music[500];
    	int cnt = 0;
    	    	
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
    	
    	String selection = "", order = "";
    	
		 selection = "_data LIKE '%mp3') AND ( " + MediaStore.Audio.Media.IS_RINGTONE + " == 0";    		 
		 if(key != "")
	    		selection += " ) AND ( artist Like \"" + key + "%\" OR artist Like \"%" + key + "\" OR artist Like \"%" + key + "%\""
	    		+ "OR title Like \"" + key + "%\" OR title Like \"%" + key + "\" OR title Like \"%" + key +"%\""
	    		+ "OR album Like \"" + key + "%\" OR album Like \"%" + key + "\" OR album Like \"%" + key +"%\"";
		 order =  "artist ASC, album ASC, title ASC";

    	
    	Cursor cur = getContentResolver().query(uri, mCursorCols, selection, null, order);
    	if( cur.moveToFirst() ){    		
    		
    		String title, artist, album, duration, path;
    		int albumId;
    		    		
    		int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);		
    		int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    		int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);	 		
    		int albumIdColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);	 	
    		int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
    		int pathColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
    		
    		do{
	   			title = cur.getString(titleColumn);
    			artist = cur.getString(artistColumn);
    			album = cur.getString(albumColumn);
    			albumId = cur.getInt(albumIdColumn);
    			duration = cur.getString(durationColumn);
    			path = cur.getString(pathColumn);
    			
    			music[cnt] = new Music();
    			music[cnt].setArtist(artist);
    			music[cnt].setTitle(title);
    			music[cnt].setAlbumName(album);
    			music[cnt].setAlbumArtId(albumId);
    			music[cnt].setDuration(duration);
    			music[cnt].setPath(path);
    			songs.add(music[cnt]);
    			cnt++;
    			
    		}while(cur.moveToNext());    		
    	}
    	if(pathList.size() == 0)
    		loadPathList();
    	
    	songList = new MusicInformation(this, R.layout.list_item, songs);
    	
    	if(!IsFirst) {
    		setListAdapter(songList);
    	}
   }
    
	public void loadPathList() {
        String ext = Environment.getExternalStorageState();
        String path = null;
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
             path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        else
        {
             path = Environment.MEDIA_UNMOUNTED;
        }

   		fileScan(path);
	}
	
   	public void fileScan(String path) {   		
   		File f = new File(path);
   		File[] fileList = null;
   		fileList = f.listFiles();
   		
   		if(fileList != null) {
   			for (int i = 0; i < fileList.length; i++) {
   				String path2 = fileList[i].getAbsolutePath();
   				if (fileList[i].isDirectory()) {
   					fileScan(path2);
   				} else {
   					try {
   						if (path2.toLowerCase().endsWith(".lrc")) {
   							pathList.add(path2);
   						}
   					} catch (NullPointerException e) {
   					}
   				}
   			}
   		}
   	}
   	
    @Override
    // 리스트에서 곡을 클릭했을때 재생
    protected void onListItemClick(ListView l,View v, int position, long id){	
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);   
		imm.hideSoftInputFromWindow(keyword.getWindowToken(),0);

		Music globe_music = (Music)this.getApplicationContext();
		globe_music.setArtist(songs.get(position).getArtist());
		globe_music.setTitle(songs.get(position).getTitle());
		globe_music.setAlbumName(songs.get(position).getAlbumName());
		globe_music.setAlbumArtId(songs.get(position).getAlbumArtId());
		globe_music.setDuration(songs.get(position).getDuration());
		globe_music.setPath(songs.get(position).getPath());
		globe_music.setLyricPath(songs.get(position).getLyricPath());
		
		Intent intent = new Intent(SongList.this, TabLayout.class);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
    }
    
    private class MusicInformation extends ArrayAdapter<Music>{
    	Resources r = getResources();
    	BitmapDrawable defaultIcon = (BitmapDrawable)r.getDrawable(R.drawable.git);
    	BitmapDrawable LyricIcon = (BitmapDrawable)r.getDrawable(R.drawable.lyric);
    	
    	private ArrayList<Music> items;
    	
    	public MusicInformation(Context context, int textViewResourceId, ArrayList<Music> items){
    		super(context, textViewResourceId, items); 
			this.items = items;
    	}
    	    	
    	@Override
    	public View getView(int position, View view, ViewGroup parent){
    		View v = view;
    		if( v == null ){
    			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = vi.inflate(R.layout.list_item, null);
    		}
    		Music m = items.get(position);
    		if( m != null ){
    			ImageView imageview = (ImageView)v.findViewById(R.id.row_album_art);
    			TextView tt = (TextView)v.findViewById(R.id.row_title);
    			TextView bt = (TextView)v.findViewById(R.id.row_album);
    			TextView dt = (TextView)v.findViewById(R.id.row_duration);
    			ImageView lyricview = (ImageView)v.findViewById(R.id.has_lyric);
    			
    			if( tt != null ){
    				tt.setText(m.getArtist() + " - " + m.getTitle());	
    			}
    			if( bt != null ){
    				bt.setText(m.getAlbumName());
    			}
    			if( dt != null) {
    				int min = Integer.parseInt(m.getDuration()) / 60000;
    				int sec = (Integer.parseInt(m.getDuration()) % 60000)/1000;
    				String duration = null;
    				if(sec < 10)
    					duration = Integer.toString(min) + ":0" + sec;
    				else
    					duration = Integer.toString(min) + ":" + sec;
    				dt.setText(duration);
    			}
    			if( imageview != null ){
    				Bitmap albumArt = List_utils.getBitmapImage(getBaseContext(), m.getAlbumArtId(), 200, 200);
        			if(albumArt != null)
        				imageview.setImageBitmap(albumArt);
        			else
        				imageview.setImageBitmap(defaultIcon.getBitmap());
    			}

    			String temp = findLyricPath(m.getPath());
    			if(temp != null) {
    				music[position].setLyricPath(temp);
    				lyricview.setImageBitmap(LyricIcon.getBitmap());
    			}
    			else
    				lyricview.setImageBitmap(null);
    		}
    		
    		return v;
    	}
    	
       	public String findLyricPath(String song_path) {
       		for(int i = 0; i < pathList.size(); i++) {    				
   				String path1 = song_path.substring(song_path.lastIndexOf("/") + 1, song_path.length() - 4);
  				String path2 = pathList.get(i).substring(pathList.get(i).lastIndexOf("/") + 1, pathList.get(i).length() - 4);
   				   				
   				if(path1.equalsIgnoreCase(path2)) 
   					return pathList.get(i);
       		}
       		return null;
    	}
    }	
    
    
    protected void onDestory() {
    	super.onDestroy();
    }
}

