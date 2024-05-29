package com.micoder.directappupdate.listeners

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

private const val EXHAUSTED_SOURCE = -1L

/**
 * A [ResponseBody] that informs a [ProgressListener] about the download progress.
 */
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
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != EXHAUSTED_SOURCE) bytesRead else 0L
                progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == EXHAUSTED_SOURCE)
                return bytesRead
            }
        }
    }
}

/**
 * Callback getting informed when the download progress of [DownloadProgressBody] updates.
 */
interface ProgressListener {

    /**
     * Informs this listener that the download progress was updated.
     *
     * @param bytesRead The bytes that have been read.
     * @param contentLength The total bytes that are being read.
     * @param done Whether the download is complete.
     */
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}