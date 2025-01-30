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

        // 내원 기록 테이블
        db!!.execSQL("CREATE TABLE clinicRecord(userId TEXT, date TEXT, reason TEXT)")

        // 생활 습관 체크 테이블 --> 나의 생활 체크 등록하기 버튼 클릭 시 생성
        db?.execSQL("CREATE TABLE habitTBL (" +
                "habit_num INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +
                "habit_name TEXT, " +
                "habit_day_of_week TEXT," +
                "habit_time TEXT," +
                "habit_date TEXT," +
                "habit_content TEXT," +
                "habit_check INTEGER)"
        )

        // 혈당 측정 관련 테이블
        db!!.execSQL("CREATE TABLE bloodRecord(userId Text, " +
                "date TEXT, blood TEXT, bloodtime Text," +
                " PRIMARY KEY(id,date, bloodtime))")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS member")
        db?.execSQL("DROP TABLE IF EXISTS session")
        db?.execSQL("DROP TABLE IF EXISTS mediTBL") // 복약 체크 테이블
        db?.execSQL("DROP TABLE IF EXISTS clinicRecord")
        db?.execSQL("DROP TABLE IF EXISTS habitTBL")
        db?.execSQL("DROP TABLE IF EXISTS bloodRecord")

        onCreate(db)
    }
}