package com.project.mp3singroom;

import android.content.Context;

public class decoding {
	Music music;
	int samplerate;
	
	public decoding(Context context, String path) {
		music = (Music) context.getApplicationContext();
		samplerate = decode(path);
		music.setSampleRate(samplerate);
	}
	
    static {
        System.loadLibrary("deco");
    }
    
    public static native int decode(String filename);
}
