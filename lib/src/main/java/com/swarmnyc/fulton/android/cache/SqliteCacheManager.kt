package com.swarmnyc.fulton.android.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.format.DateUtils
import android.util.Log
import com.swarmnyc.fulton.android.util.Logger
import com.swarmnyc.fulton.android.util.fromJson
import com.swarmnyc.fulton.android.util.urlEncode
import java.lang.reflect.Type

/**
 * Use sqlite to store cache
 */
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
    private val db = helper.writableDatabase
    private var nextCheckedMs = 0L

    override fun add(cls: String, url: String, durationMs: Int, data: ByteArray) {
        val values = ContentValues()
        val expiredAt = System.currentTimeMillis() + durationMs
        values.put(FieldURL, url.urlEncode())
        values.put(FieldClass, cls)
        values.put(FieldExpiredAt, expiredAt)
        values.put(FieldData, data)

        db.replace(TableName, null, values)

        Logger.Cache.d {
            "Add Cache for $cls, $url, +$durationMs at $expiredAt"
        }
    }

    override fun get(url: String): ByteArray? {
        cleanOld()

        val time = System.currentTimeMillis()
        val cursor = db.query(
                TableName,
                arrayOf(FieldData, FieldExpiredAt),
                "$FieldURL = '${url.urlEncode()}' AND $FieldExpiredAt > $time",
                arrayOf(), null, null, null)

        val data = if (cursor.moveToFirst()) {
            Logger.Cache.d { "Find Cache for $url and timeout: ${cursor.getLong(1) - time}" }
            cursor.getBlob(0)
        } else {
            Logger.Cache.d { "No Cache for $url" }
            null
        }

        cursor.close()

        return data
    }

    override fun clean(cls: String?) {
        if (cls == null) {
            Logger.Cache.d { "Clean All Cache" }
            db.delete(TableName, null, null)
        } else {
            Logger.Cache.d { "Clean Cache for $cls" }
            db.delete(TableName, "$FieldClass=?", arrayOf(cls))
        }
    }

    private fun cleanOld() {
        if (nextCheckedMs < System.currentTimeMillis()) {
            Logger.Cache.d { "Clean Old Cache" }

            db.delete(TableName, "$FieldExpiredAt < ?", arrayOf(nextCheckedMs.toString()))

            nextCheckedMs = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS
        }
    }

    class DbHelper(context: Context) : SQLiteOpenHelper(context, DbName, null, DbVersion) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TableName ($FieldURL TEXT PRIMARY KEY, $FieldClass TEXT, $FieldExpiredAt INTEGER, $FieldData BLOB)")
        }

        override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}