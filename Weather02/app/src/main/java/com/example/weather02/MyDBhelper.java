package com.example.weather02;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBhelper extends SQLiteOpenHelper {
    public static final String DB_NAME="Concern3.db";
    public static final int VERSION=1;
    public static final String TABLE_NAME="Concern";
    //对数据库进行创建
    public static final String CREATE_CONCERN = "create table Concern("
            + "city_code String primary key not null,"
            + "city_name String not null)";

    private Context mContext;

    public MyDBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_CONCERN);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
