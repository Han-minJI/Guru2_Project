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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS member")
        db?.execSQL("DROP TABLE IF EXISTS session")
        onCreate(db)
    }
}