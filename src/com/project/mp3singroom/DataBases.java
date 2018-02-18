package com.project.mp3singroom;

import android.provider.BaseColumns;

public final class DataBases { 
    
    public static final class CreateDB implements BaseColumns{ 
        public static final String FILENAME = "filename"; 
        public static final String DATE = "date"; 
        public static final String PATH = "path"; 
        public static final String DURATION = "duration"; 
        public static final String ORIGIN = "origin"; 
        public static final String _TABLENAME = "historyTable"; 
        public static final String _CREATE =  
            "create table "+_TABLENAME+"("    
            		+_ID+" integer primary key autoincrement, "
                    +FILENAME+" text not null , " 
                    +DATE+" text not null , " 
                    +PATH+" text not null , "
                    +DURATION+" text not null , "
                    +ORIGIN+" text not null);";
    } 
} 