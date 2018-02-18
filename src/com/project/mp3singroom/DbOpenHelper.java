package com.project.mp3singroom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper { 
	  
    private static final String DATABASE_NAME = "history.db"; 
    private static final int DATABASE_VERSION = 1; 
    public static SQLiteDatabase mDB; 
    private DatabaseHelper mDBHelper; 
    private Context mCtx; 
  
    private class DatabaseHelper extends SQLiteOpenHelper{ 
  
        // 생성자 
        public DatabaseHelper(Context context, String name, 
                CursorFactory factory, int version) { 
            super(context, name, factory, version); 
        } 
  
        // 최초 DB를 만들때 한번만 호출된다. 
        @Override
        public void onCreate(SQLiteDatabase db) { 
            db.execSQL(DataBases.CreateDB._CREATE); 
  
        } 
  
        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다. 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME); 
            onCreate(db); 
        } 
    } 
  
    public DbOpenHelper(Context context){ 
        this.mCtx = context; 
    } 
  
    public DbOpenHelper open() throws SQLException{ 
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION); 
        mDB = mDBHelper.getWritableDatabase(); 
        return this; 
    } 
  
    public void close(){ 
        mDB.close(); 
    }
    
	// Insert DB
	public long insertColumn(String filename, String date, String path, String duration, String origin){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateDB.FILENAME, filename);
		values.put(DataBases.CreateDB.DATE, date);
		values.put(DataBases.CreateDB.PATH, path);
		values.put(DataBases.CreateDB.DURATION, duration);
		values.put(DataBases.CreateDB.ORIGIN, origin);
		return mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
	}
	
	// Delete DB
	public boolean deleteColumn(String filename){
		return mDB.delete(DataBases.CreateDB._TABLENAME, "filename=\""+filename + "\"", null) > 0;
	}
	
	// Update DB
	public boolean updateColumn(String Oldfilename, String filename, String path){
		ContentValues values = new ContentValues();
		values.put(DataBases.CreateDB.FILENAME, filename);
		values.put(DataBases.CreateDB.PATH, path);
		return mDB.update(DataBases.CreateDB._TABLENAME, values, "filename=\""+Oldfilename + "\"", null) > 0;
	}
	
	// SELECT (rawQuery)
	public Cursor getMatchName(String filename){
		return mDB.query(DataBases.CreateDB._TABLENAME, null, "(filename Like \"" + filename + "%\" OR filename Like \"%" + filename + "\" OR filename Like \"%" + filename +"%\") AND (path LIKE '%3gp' OR path LIKE '%mp4')", null, null, null, "filename");
	}
	
	// Select All
	public Cursor getAllColumns(){
		return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, "filename");
	}
} 
  