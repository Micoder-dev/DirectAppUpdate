package com.micoder.directappupdate.listeners

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

private const val EXHAUSTED_SOURCE = -1L

class DownloadProgressBody(
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource {
        return bufferedSource ?: source(responseBody.source()).buffer().also { bufferedSource = it }
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != EXHAUSTED_SOURCE) bytesRead else 0L
                progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == EXHAUSTED_SOURCE)
                return bytesRead
            }
        }
    }
}

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}