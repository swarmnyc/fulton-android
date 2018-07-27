package com.swarmnyc.fulton.android.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.swarmnyc.fulton.android.util.fromJson
import com.swarmnyc.fulton.android.util.urlEncode
import java.lang.reflect.Type


class SqliteCacheManager(context: Context) : CacheManager {
    companion object {
        internal const val DbName = "fulton-cache.db"
        private const val DbVersion = 1
        private const val TableName = "api_caches"
        private const val FieldURL = "url"
        private const val FieldClass = "class"
        private const val FieldExpiredAt = "expired_at"
        private const val FieldData = "data"
    }

    private val helper = DbHelper(context)

    override fun add(cls: String, url: String, durationMs: Int, data: ByteArray) {
        val values = ContentValues()
        values.put(FieldURL, url.urlEncode())
        values.put(FieldClass, cls)
        values.put(FieldExpiredAt, System.currentTimeMillis() + durationMs)
        values.put(FieldData, data)

        helper.writableDatabase.replace(TableName, null, values)
    }

    override fun <T> get(url: String, type: Type): T? {
        val cursor = helper.readableDatabase.query(
                TableName,
                arrayOf(FieldData),
                "$FieldURL = ? AND $FieldExpiredAt > ${System.currentTimeMillis()}",
                arrayOf(url.urlEncode()), null, null, null)

        val data: T? = if (cursor.moveToFirst()) {
            cursor.getBlob(0).fromJson(type)
        } else {
            null
        }

        cursor.close()

        return data
    }

    override fun clean(cls: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class DbHelper(context: Context) : SQLiteOpenHelper(context, DbName, null, DbVersion) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TableName ($FieldURL TEXT PRIMARY KEY, $FieldClass TEXT, $FieldExpiredAt INTEGER, $FieldData BLOB)")
        }

        override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}