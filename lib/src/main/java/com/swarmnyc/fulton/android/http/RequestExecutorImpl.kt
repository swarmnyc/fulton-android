package com.swarmnyc.fulton.android.http

import android.util.Log
import com.swarmnyc.fulton.android.util.toJson
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class RequestExecutorImpl : RequestExecutor() {
    override fun execute(req: Request, callback: RequestCallback) {
        if (Log.isLoggable(ApiClient.TAG, Log.DEBUG)) {
            val msg = buildString {
                appendln("--> ${req.method} (${req.url})")
                appendln("Body : ${if (req.body == null) "(empty)" else req.body?.toJson()}")
                appendln("Headers : (${req.headers.size})")

                for ((key, value) in req.headers) {
                    appendln("$key : $value")
                }
            }

            Log.d(ApiClient.TAG, msg)
            Log.d(ApiClient.TAG, "--> ${req.method} (${req.url})\n$req")
        }

        var conn: HttpURLConnection? = null
        val res: Response = try {
            val url = URL(req.url)
            conn = url.openConnection() as HttpURLConnection

            conn.apply {
                doInput = true
                connectTimeout = req.timeOutMs
                requestMethod = if (req.method == Method.PATCH) Method.POST.value else req.method.value
                instanceFollowRedirects = true

                for ((key, value) in req.headers) {
                    setRequestProperty(key, value)
                }

                if (req.method == Method.PATCH) {
                    setRequestProperty("X-HTTP-Method-Override", "PATCH")
                }

                if (req.body != null) {
                    conn.doOutput = true

                    val writer = OutputStreamWriter(if (req.useGzip) {
                        setRequestProperty("Content-Encoding", ApiClient.GZip)
                        setRequestProperty("Accept-Encoding", ApiClient.GZip)
                        GZIPOutputStream(conn.outputStream)
                    } else {
                        conn.outputStream
                    })

                    req.body!!.toJson(writer)

                    writer.flush()
                    writer.close()
                }

                connect()
            }

            val stream = conn.errorStream ?: conn.inputStream

            val contentEncoding = conn.contentEncoding ?: ""

            val data = if (contentEncoding.compareTo(ApiClient.GZip, true) == 0) {
                GZIPInputStream(stream).readBytes()
            } else {
                stream.readBytes()
            }

            stream.close()

            Response(conn.url.toString(), conn.responseCode, conn.headerFields.filterKeys { it != null }, data, null)
        } catch (e: Exception) {
            Response(error = e)
        } finally {
            conn?.disconnect()
        }

        if (Log.isLoggable(ApiClient.TAG, Log.DEBUG)) {
            val time = (System.currentTimeMillis() - req.startedAt)
            val msg = buildString {
                appendln("<-- ${res.status} (${res.url})")
                appendln("Time Spent : $time")
                appendln("Length : ${res.contentLength}")
                appendln("Headers : (${res.headers.size})")
                for ((key, value) in res.headers) {
                    appendln("$key : $value")
                }
                appendln("Body : ${if (res.data.isNotEmpty()) String(res.data) else "(empty)"}")
            }

            Log.d(ApiClient.TAG, msg)
        }

        callback(req, res)
    }
}