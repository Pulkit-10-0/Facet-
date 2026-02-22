package com.example.drill.network

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object SecureHttp {
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 10_000
    private const val DEFAULT_READ_TIMEOUT_MS = 15_000
    private const val MAX_REDIRECTS = 3

    fun requireHttps(url: String): URL {
        val parsed = URL(url)
        if (parsed.protocol.lowercase() != "https") throw IllegalArgumentException("HTTPS required")
        return parsed
    }

    fun fetchUtf8Text(
        url: String,
        maxBytes: Long,
        connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS
    ): String {
        val conn =
            openHttpsGetFollowingRedirects(
                url = url,
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                acceptJson = true
            )
        val contentLength = conn.contentLengthLong
        if (contentLength > 0 && contentLength > maxBytes) {
            conn.disconnect()
            throw IllegalStateException("Response too large")
        }
        val stream = BufferedInputStream(conn.inputStream)
        stream.use { input ->
            val out = ByteArrayOutputStreamCapped(maxBytes)
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buf)
                if (read <= 0) break
                out.write(buf, 0, read)
            }
            conn.disconnect()
            return out.toByteArray().toString(Charsets.UTF_8)
        }
    }

    fun downloadToFile(
        url: String,
        destination: File,
        maxBytes: Long,
        connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS,
        onProgress: ((downloadedBytes: Long, totalBytes: Long?) -> Unit)? = null
    ) {
        val conn =
            openHttpsGetFollowingRedirects(
                url = url,
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                acceptJson = false
            )
        val total = conn.contentLengthLong.takeIf { it > 0 }
        if (total != null && total > maxBytes) {
            conn.disconnect()
            throw IllegalStateException("File too large")
        }
        destination.parentFile?.mkdirs()
        BufferedInputStream(conn.inputStream).use { input ->
            FileOutputStream(destination).use { output ->
                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L
                while (true) {
                    val read = input.read(buf)
                    if (read <= 0) break
                    downloaded += read
                    if (downloaded > maxBytes) throw IllegalStateException("File too large")
                    output.write(buf, 0, read)
                    onProgress?.invoke(downloaded, total)
                }
                output.flush()
            }
        }
        conn.disconnect()
    }

    private fun openHttpsGetFollowingRedirects(
        url: String,
        connectTimeoutMs: Int,
        readTimeoutMs: Int,
        acceptJson: Boolean
    ): HttpURLConnection {
        var current = requireHttps(url)
        repeat(MAX_REDIRECTS + 1) { attempt ->
            val conn = (current.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                requestMethod = "GET"
                if (acceptJson) setRequestProperty("Accept", "application/json")
            }
            conn.connect()
            val code = conn.responseCode
            if (code in 200..299) return conn
            if (code in 300..399) {
                val location = conn.getHeaderField("Location")?.trim().orEmpty()
                conn.disconnect()
                if (location.isEmpty()) throw IllegalStateException("Redirect with no Location")
                current = requireHttps(URL(current, location).toString())
                if (attempt == MAX_REDIRECTS) throw IllegalStateException("Too many redirects")
                return@repeat
            }
            conn.disconnect()
            throw IllegalStateException("HTTP $code")
        }
        throw IllegalStateException("HTTP error")
    }
}

