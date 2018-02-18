package com.project.mp3singroom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle; 
import android.os.Handler;
import android.view.Menu; 
import android.view.View; 
import android.widget.Button; 
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Sing extends Activity { 
	private DbOpenHelper mDbOpenHelper;
	
	AudioRecord recorder;
    AudioRecord echorecorder;
    AudioTrack track;
    CheckBox IsRecord, IsEcho;
    TextView hint, lyric1, lyric2;
    AnimationDrawable frame;
    ImageView view;
    
    ArrayList<Integer> time = new ArrayList<Integer>();
    ArrayList<String> line = new ArrayList<String>();
    
    private final Handler handler = new Handler();

    boolean setup, switching, exception, has_lyric, Rec = false, Echo = false;
    int curr_time, read_index, focus_index, dura = 0;
    int buff_size;
    int samplerate;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    
    String artist, title, duration, filename, path;
    Intent intent;
    String dirPath = "/sdcard/MP3 Singroom";
    
    @Override
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.sing); 
                
        Music music = (Music) this.getApplicationContext();
        
        artist = music.getArtist();
        title = music.getTitle();
        duration = music.getDuration();
        path = music.getPath();
        time = music.time;
        line = music.line;
        samplerate = music.getSampleRate();
                
    	view = (ImageView) findViewById(R.id.imageAni);
        view.setBackgroundResource(R.drawable.ani_list);
        frame = (AnimationDrawable) view.getBackground();        
        
        Button StartBtn = (Button) findViewById(R.id.StartBtn); 
        Button StopBtn = (Button) findViewById(R.id.StopBtn); 
        IsRecord = (CheckBox) findViewById(R.id.checkbox1);
        IsEcho = (CheckBox) findViewById(R.id.checkbox3);
        IsRecord.setChecked(false);
        IsEcho.setChecked(false);
        
        hint = (TextView) findViewById(R.id.hint);
        lyric1 = (TextView) findViewById(R.id.line1);
        lyric2 = (TextView) findViewById(R.id.line2);
  
        lyric1.setText(title);
        lyric2.setText("- " + artist + " -");
                        	            
	    init();
	    
        StartBtn.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View v) { 
            	if(intent != null)
            		return;
            	
            	IsRecord.setEnabled(false);
            	            	
            	setMR();
           		if(Rec)
           			setRecorder();
           		else
                	setEcho();
            	
                if(time.size() != 0 && line.size() != 0) {  
                	if(time.get(0) < 3000) {
                     	exception = true;
                     	curr_time = time.get(0) - 3000;
                	}
                	if(!exception) {

                		if(Rec) {
                			startRecording();
                		}
                		else if(Echo) {
	    					(new Thread() {
	    						@Override
	    						public void run() {
	    							recordAndPlay();
	    						}
	    					}).start();
                		}
    					
                		startService(intent);
                	}
                	has_lyric = true;
                }
                else {
                	hint.setText("가사 파일(.LRC)이 없습니다.");
                	
            		if(Rec) {
            			startRecording();
            		}
            		else if(Echo) {
    					(new Thread() {
    						@Override
    						public void run() {
    							recordAndPlay();
    						}
    					}).start();
            		}
                	
                    startService(intent);
                    has_lyric = false;
                }
                printSyncLyrics();	
                frame.start();
            } 
        }); 
  
        StopBtn.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View v) { 
                if (intent == null) 
                    return; 
                
                stopRecording();
                init();
                if(Rec)  
                	addDB();
                
                IsRecord.setEnabled(true);
            } 
        });
        
        IsRecord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (buttonView.getId() == R.id.checkbox1) {
                    if (isChecked) {
                        Rec = true;

                       	Echo = false;
                       	IsEcho.setChecked(false);
                       	IsEcho.setEnabled(false);
                        	
                       	if(echorecorder != null) { 
                       		echorecorder.stop();
                       		echorecorder.release(); 
                       		echorecorder = null;  
                       	}
                        	
                       	if(track != null) {
                       		track.stop();
                       		track.release(); 
                            track = null; 
                       	}
                        
                    } else {
                    	Rec = false;
                    	                    	
                        IsEcho.setEnabled(true);
                    }
                }
            }
        });
                
        IsEcho.setOnCheckedChangeListener(new OnCheckedChangeListener() {
       	 
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (buttonView.getId() == R.id.checkbox3) {
                    if (isChecked) {
                        Echo = true;
                    } else {
                    	Echo = false;                  	
                    }
                }
            }
        });
    } 
	
    public void recordAndPlay() {
    	short[] lin = new short[512];
    	int num = 0;

		echorecorder.startRecording();
    	track.play();
    	track.setStereoVolume(0.7f, 0.7f);
		
		while(echorecorder != null && track != null) {
			num = echorecorder.read(lin, 0, 512);
			
			if(Echo && num > 0) {		
				track.write(lin,  0,  num);
			}
		}
    }  
    
	public void setMR() {
		intent = new Intent(this, MRplayer.class);
	}
	
	public void setRecorder() {		  	
		buff_size = AudioRecord.getMinBufferSize(samplerate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, AudioFormat.CHANNEL_IN_STEREO,
		AudioFormat.ENCODING_PCM_16BIT, buff_size);
	}
	
    private void startRecording(){
         
        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();
         
        isRecording = true;
         
        recordingThread = new Thread(new Runnable() {
                 
                @Override
                public void run() {
                        writeAudioDataToFile();
                }
        },"AudioRecorder Thread");
         
        recordingThread.start();
	}
	 
	private void writeAudioDataToFile(){
        byte data[] = new byte[buff_size];
        
        File file = new File(dirPath); 
        if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
          file.mkdirs();
        
		filename = checkFileName(dirPath, artist.replace("/", " ") + " - " + title.replace("/", " ") + "_recorded");
		
        FileOutputStream os = null;
         
        try {
                os = new FileOutputStream(dirPath + "/" + filename + ".raw");
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
         
        int read = 0;
         
        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, buff_size);
                 
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                            os.write(data);
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
            }
             
            try {
                    os.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
	}
	 
	private void stopRecording(){
        if(null != recorder){
                isRecording = false;
                 
                int i = recorder.getState();
                if(i==1)
                    recorder.stop();
                recorder.release();
                 
                recorder = null;
                recordingThread = null;
        }
         
        copyWaveFile(dirPath + "/" + filename + ".raw", dirPath + "/" + filename + ".wav");
        
        File file = new File(dirPath + "/" + filename + ".raw");
         
        file.delete();
	}
	 
	private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = samplerate;
        int channels = 2;
        long byteRate = 16 * samplerate * channels/8;
         
        byte[] data = new byte[buff_size];
         
        try {
                in = new FileInputStream(inFilename);
                out = new FileOutputStream(outFilename);
                totalAudioLen = in.getChannel().size();
                totalDataLen = totalAudioLen + 36;
                 	                 
                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                                longSampleRate, channels, byteRate);
                 
                while(in.read(data) != -1){
                        out.write(data);
                }
                 
                in.close();
                out.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
	}
	
	private void WriteWaveFileHeader(
	                FileOutputStream out, long totalAudioLen,
	                long totalDataLen, long longSampleRate, int channels,
	                long byteRate) throws IOException {
	         
        byte[] header = new byte[44];
         
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
	}
	
	public void setEcho() {
		buff_size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		echorecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO,
		AudioFormat.ENCODING_PCM_16BIT, buff_size);

		int maxJitter = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
		AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
	}
	    
    public void init() {    	
    	if(frame.isRunning()) {
    		frame.stop();
	    	frame.selectDrawable(0);
    	}
    	
    	if(intent != null) {
    		stopService(intent);
    		intent = null;
    	}	

    	if(recorder != null) {
    		recorder.stop();
    		recorder.release();
    		recorder = null;
    	}
    	    	
    	if(echorecorder != null) { 
    		echorecorder.stop();
    		echorecorder.release(); 
    		echorecorder = null;  
    	}
    	
    	if(track != null) {
    		track.stop();
    		track.release(); 
            track = null; 
    	}
    	
    	hint.setText("");
		lyric1.setTextColor(Color.BLACK);
		lyric2.setTextColor(Color.BLACK);
        lyric1.setText(title);
        lyric2.setText("- " + artist + " -");
        
        isRecording = false;
        setup = true;
        switching = true;
        exception = false;
        curr_time = 0;
        read_index = 0;
        focus_index = 0;
    }
  
    public void printSyncLyrics() {	
    	if(intent == null)
    		return;
    	
		// MR 재생이 끝났을 경우 초기화한 후 종료
		if(curr_time / 100 == Integer.parseInt(duration) / 100 && intent != null ) {
			
			stopRecording();
			init();
						
			if(Rec) 
				addDB();
			
			IsRecord.setEnabled(true);
	    	
	        return;
		}
    	    	
    	if(intent != null && has_lyric) {			
			// 노래의 첫소절이 3초 이내 나올 경우 : 바로 시작하는 노래
			if(time.get(0) < 3000 && exception) {  
				
				if(curr_time == 0) { // 3초 카운터가 끝나면 포커스 시작							
            		if(Rec) {
            			startRecording();
            		}
            		else if(Echo) {
    					(new Thread() {
    						@Override
    						public void run() {
    							recordAndPlay();
    						}
    					}).start();
            		}
		            startService(intent);
				}
				
				if(curr_time == time.get(0)) {
					exception = false;
					
					lyric1.setTextColor(Color.BLUE);
					hint.setText("");
					setup = false;
					read_index++;
					read_index++;
					focus_index++;
				}
				else {
					if(curr_time == time.get(0) - 3000)
						hint.setText("3!");
					else if(curr_time == time.get(0) - 2000)
						hint.setText("2!");
					else if(curr_time == time.get(0) - 1000)
						hint.setText("1!");
					lyric1.setText(line.get(0));
					lyric2.setText(line.get(1));
				}
			}
			// 노래의 첫소절이 3초 후에 나올 경우 : 일반적인 노래
			else {
				// 포커스할 다음 가사가 있을 경우
		    	if(focus_index < time.size()) {   
		    		// 초기설정 : 반주 다음 첫소절을 출력해야 할 경우
		    		if(setup) { 	
		    			if(time.get(focus_index) - curr_time <= 3000) {  // 다음 가사를 포커스하기 3초 이내
		    				
		    				int key = (time.get(focus_index) - curr_time);
		    				
		    				if(key == 1000 || key == 2000 || key == 3000)			    				
			    				hint.setText(Integer.toString(key/1000) + "!");
		    				
		    				lyric1.setTextColor(Color.BLACK);
		    				lyric2.setTextColor(Color.BLACK);
		    			
		    				if(read_index < time.size())   // 출력할 다음 가사가 있을 경우1
		    					lyric1.setText(line.get(read_index));
		    				if(read_index+1 < time.size()) // 출력할 다음 가사가 있을 경우2
		    					lyric2.setText(line.get(read_index+1));
		    				
		    			}    			
		    			else  // 다음 가사 포커스하기 10초보다 더 남았을 경우 "반주중" 출력 : 전주, 간주
		    				hint.setText("반주 중~♬");
		    		}
		    		
		    		//  가사에 포커스를 해야 할 경우
		    		if(curr_time == time.get(focus_index)) {
		    			
		    			if(setup) {  // 반주 다음 첫소절일 경우
		    				lyric1.setTextColor(Color.BLUE);
		    				hint.setText("");
		    				setup = false;
		    				switching = true;
		    				read_index++;
		    				read_index++;
		    			}  
		    			else {      // 반주 다음 첫소절이 아닐 경우	
		    				String next = "";
		    				if(read_index < time.size()) {  // 다음 가사가 있으면
		    					if(time.get(read_index) - curr_time < 10000) {   // 읽어 올 다음 가사가 10초 이내일 경우 그냥 읽어옴
		    						next = line.get(read_index);
		    						read_index++;
		    					}
		    					else    // 10초 이상일 경우 다음 가사를 읽지 않고 '초기설정'으로
		    						setup = true;
		    				}
		    				else  	// 다음 가사가 없으면 "반주중" 출력 : 후주
		    					hint.setText("반주 중~♬");
		    					
		    				if(switching) {  // 2번째 줄에 포커스할 경우
		    					lyric2.setTextColor(Color.BLUE);
		    					lyric1.setText(next);
		    					lyric1.setTextColor(Color.BLACK);
		    					switching = false;
		    				}
		    				else {  // 1번째 줄에 포커스할 경우
		    					lyric1.setTextColor(Color.BLUE);
		    					lyric2.setText(next);
		    					lyric2.setTextColor(Color.BLACK);
		    					switching = true;
		    				}
		    			}
		    			focus_index++;
		    		}
		    	}
			}
    	}
		curr_time += 100;
		
		if(isRecording)
			dura+=100;
		
		Runnable notification = new Runnable() {
			public void run() {
				printSyncLyrics();
			}
		};
		handler.postDelayed(notification, 100);
    }
    
    public String checkFileName(String path, String name) {
    	String result = name;
    	int count = 1;
    	File f = new File(path);
    	String list[] = f.list();
    	for(int i = 0; i < list.length; i++) {
    		if(list[i].substring(0, list[i].length() - 4).equals(result)) {
    			result = name + "(" + count + ")";
    			count++;
    		}
    	}
    	return result;
    }
        
    public void addDB() {
        Date date = new Date(System.currentTimeMillis());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH.mm.ss");

        mDbOpenHelper = new DbOpenHelper(this); 
        mDbOpenHelper.open(); 
          
        mDbOpenHelper.insertColumn(filename, dateFormat.format(date).toString(), dirPath + "/" + filename + ".wav", Integer.toString(dura), path);
        dura = 0;
    }
    
	protected void onPause() {
		super.onPause();
        if (intent == null) 
            return; 
        
        init();
        if(Rec)  {
        	addDB();
        	IsRecord.setEnabled(true);
        }
	}
    
	protected void onDestroy() {
		super.onDestroy();
		if (recorder != null || echorecorder != null || track != null || intent != null) {
			init();
		}
		File file = new File("/mnt/sdcard/out.pcm");
		file.delete();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { 
        // Inflate the menu; this adds items to the action bar if it is present. 
        getMenuInflater().inflate(R.menu.main, menu); 
        return true; 
    }  
} 
