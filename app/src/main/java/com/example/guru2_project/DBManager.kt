package com.example.guru2_project

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBManager(context: Context?,
                name: String?,
                factory: SQLiteDatabase.CursorFactory?,
                version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS member (" +
                    "userId TEXT PRIMARY KEY, " +
                    "userPasswd TEXT, " +
                    "userName TEXT, "+
                    "birthDate TEXT,"+
                    "userType TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS session (" +
                    "userId TEXT PRIMARY KEY, " +
                    "userPasswd TEXT)"
        )

        // 복약 체크 테이블 --> "복약 등록하기" 버튼 클릭 시 생성
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS mediTBL (" +
                "medi_num INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +
                "medi_name TEXT, " +
                "medi_day_of_week TEXT," +
                "medi_time TEXT," +
                "medi_date TEXT," +
                "medi_check INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS member")
        db?.execSQL("DROP TABLE IF EXISTS session")
        db?.execSQL("DROP TABLE IF EXISTS mediTBL") // 복약 체크 테이블
        onCreate(db)
    }
}