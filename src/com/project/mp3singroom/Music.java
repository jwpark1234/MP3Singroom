package com.project.mp3singroom;

import java.util.ArrayList;
import android.app.Application;

public class Music extends Application {
	private String artist;
	private String title;
	private String albumName;
	private int albumId;
	private String duration;
	private String path;
	private String lyric_path;
	private int samplerate;
	private String date;
	private String origin;
	public ArrayList<Integer> time = new ArrayList<Integer>(); 
	public ArrayList<String> line = new ArrayList<String>();
		
	public void setArtist(String artist){
		this.artist = artist;	
	}
	
	public void setTitle(String title){
		this.title = title;	
	}
	 	
	public void setAlbumName(String albumName){
		this.albumName = albumName;	
	}
	
	public void setAlbumArtId(int albumId){
		this.albumId = albumId;	
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void setLyricPath(String path) {
		this.lyric_path = path;
	}
	
	public void setSampleRate(int sampleRate) {
		this.samplerate = sampleRate;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public String getArtist(){
		return artist;	
	}
	
	public String getTitle(){
		return title;	
	}
	 	
	public String getAlbumName(){
		return albumName;	
	}
	
	public int getAlbumArtId(){
		return albumId;	
	}

	public String getDuration() {
		return duration;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getLyricPath() {
		return lyric_path;
	}
	
	public int getSampleRate() {
		return samplerate;
	}
	
	public String getDate() {
		return date;
	}
		
	public String getOrigin() {
		return origin;
	}
}
