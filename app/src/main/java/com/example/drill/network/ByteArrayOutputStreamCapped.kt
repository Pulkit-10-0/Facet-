package com.example.drill.network

import java.io.ByteArrayOutputStream

class ByteArrayOutputStreamCapped(private val maxBytes: Long) : ByteArrayOutputStream() {
    override fun write(b: Int) {
        if (count + 1L > maxBytes) throw IllegalStateException("Response too large")
        super.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (count + len.toLong() > maxBytes) throw IllegalStateException("Response too large")
        super.write(b, off, len)
    }
}

