package com.swarmnyc.fulton.android.cache

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.reflect.Type

class SqliteCacheManagement(context: Context) : CacheManager {
    init {

    }


    override fun add(api: String, url: String, durationMs: Int, data: ByteArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> get(url: String, type: Type): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clean(api: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    class DbHelper(context: Context) : SQLiteOpenHelper(context, "", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {}
        override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}