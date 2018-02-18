package com.project.mp3singroom;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater; 
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.database.Cursor;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import java.io.File;
import java.util.ArrayList;

public class HistoryList extends ListActivity {
	private ListView listview;	
	private EditText keyword;
	private Button search;
	private String key;
	private ArrayList<Music> songs;
	MusicInformation songList;
	private DbOpenHelper mDbOpenHelper;
	ProgressDialog progressDialog;
	boolean IsFirst = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songlist);
        
        this.setTitle("MP3 노래방 > 나의 기록");
        
        mDbOpenHelper = new DbOpenHelper(this); 
        mDbOpenHelper.open(); 
        
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
		        registerForContextMenu(listview);
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
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	int index = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
    	menu.setHeaderTitle(songs.get(index).getTitle());
    	menu.add(0, Menu.FIRST, Menu.NONE, "이름 변경");
    	menu.add(0, Menu.FIRST+1, Menu.NONE, "삭제");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	super.onContextItemSelected(item);
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	final int index = menuInfo.position;
    	final AlertDialog.Builder alert = new AlertDialog.Builder(menuInfo.targetView.getContext());
    	switch(item.getItemId()) {
    		case Menu.FIRST :
    			final EditText rename = new EditText(this);
    			String name = songs.get(index).getPath().toString();
    			final String format = name.substring(name.length()-4);
    			int pos = name.lastIndexOf("/") + 1;
    			name = name.substring(pos, name.length()-4);
    			rename.setText(name);
    		    
        		alert.setTitle("이름 변경");
        		alert.setView(rename);
        		
        		alert.setPositiveButton("변경", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// TODO Auto-generated method stub
    					File file = new File(songs.get(index).getPath());
    					
    					if(IsExistName(file.getParentFile().getAbsolutePath(), rename.getText().toString()))
    						Toast.makeText(getApplicationContext(), "중복된 파일명입니다.", 2000).show();    					
    					else {
        					File newfile = new File(file.getParentFile().getAbsolutePath() + "/" + rename.getText().toString() + format);
        					file.renameTo(newfile);
        					
        					mDbOpenHelper.updateColumn(songs.get(index).getTitle(), rename.getText().toString(), newfile.getPath());
        					songs.get(index).setPath(newfile.getPath());
        					songs.get(index).setTitle(rename.getText().toString());
        					songList.notifyDataSetChanged();
        					dialog.dismiss();
    					}
    				}
    			});
        		alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
        				dialog.dismiss();
        			}
        		});
        		alert.show();
				return true;
				
    		case Menu.FIRST+1 :
        		alert.setTitle("삭제");
        		alert.setMessage("삭제 하시겠습니까?");
        		alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// TODO Auto-generated method stub
    					File file = new File(songs.get(index).getPath());
    					file.delete();
    					
    					mDbOpenHelper.deleteColumn(songs.get(index).getTitle());
    					songs.remove(index);
    					songList.notifyDataSetChanged();
    					dialog.dismiss();
    				}
    			});
        		alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
        				dialog.dismiss();
        			}
        		});
        		alert.show();
    			return true;
    	}
    	return false;
    }
    
    public void updateSongList(String key){

		songs = new ArrayList<Music>();
	
    	Music[] music = new Music[500];
    	int cnt = 0;
    	    	    	
    	Cursor cur = null;
    	if(key == "")
    		cur = mDbOpenHelper.getAllColumns();
    	else
    		cur = mDbOpenHelper.getMatchName(key);
    	
    	if( cur.moveToFirst() ){    		
    		
    		String title, date, path, duration, origin;
    		
    		int titleColumn = cur.getColumnIndex("filename");		
    		int dateColumn = cur.getColumnIndex("date");
    		int pathColumn = cur.getColumnIndex("path");
    		int durationColumn = cur.getColumnIndex("duration");
    		int originColumn = cur.getColumnIndex("origin");
    		
    		do{
	   			title = cur.getString(titleColumn);
	   			date = cur.getString(dateColumn);
    			path = cur.getString(pathColumn);
    			duration = cur.getString(durationColumn);
    			origin = cur.getString(originColumn);
    			
    			music[cnt] = new Music();
    			music[cnt].setTitle(title);
    			music[cnt].setDate(date);
    			music[cnt].setPath(path);
    			music[cnt].setDuration(duration);
    			music[cnt].setOrigin(origin);
    			songs.add(music[cnt]);
    			cnt++;
    			
    		}while(cur.moveToNext());    		
    	}
 
    	cur.close();
	    songList = new MusicInformation(this, R.layout.list_item, songs);
	    
    	if(!IsFirst) {
    		setListAdapter(songList);
    	}
    }
    
    @Override
    // 리스트에서 곡을 클릭했을때 재생
    protected void onListItemClick(ListView l,View v, int position, long id){	
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);   
		imm.hideSoftInputFromWindow(keyword.getWindowToken(),0);

		Music music = (Music)this.getApplicationContext();
		music.setTitle(songs.get(position).getTitle());
		music.setDate(songs.get(position).getDate());
		music.setPath(songs.get(position).getPath());
		music.setDuration(songs.get(position).getDuration());
		music.setOrigin(songs.get(position).getOrigin());
		
		Intent intent = new Intent(HistoryList.this, History.class);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
    }
    
    private class MusicInformation extends ArrayAdapter<Music>{
    	Resources r = getResources();
    	BitmapDrawable mDefaultAlbumIcon = (BitmapDrawable)r.getDrawable(R.drawable.git);
    	
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
    			
    			if( tt != null ){
    				tt.setText(m.getTitle());	
    			}
    			if( bt != null ){
    				bt.setText(m.getDate());
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
    			// songs.getScore() 에 따라 그림 출력!!
    			if( imageview != null ){    		
        			imageview.setImageBitmap(mDefaultAlbumIcon.getBitmap());
    			}
    		}
    		
    		return v;
    	}
    }	
    
    public boolean IsExistName(String path, String name) {
    	boolean result = false;

    	File f = new File(path);
    	String list[] = f.list();
    	for(int i = 0; i < list.length; i++) {
    		if(list[i].substring(0, list[i].length() - 4).equals(name)) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    protected void onDestory() {
    	super.onDestroy();
    	mDbOpenHelper.close();
    }
}