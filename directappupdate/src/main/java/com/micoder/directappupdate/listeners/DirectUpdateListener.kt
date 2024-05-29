package com.micoder.directappupdate.listeners

/**
 * [DirectUpdateListener] is an interface that provides callbacks for the direct app update process.
 */
interface DirectUpdateListener {
    fun onImmediateUpdateAvailable()
    fun onFlexibleUpdateAvailable()
    fun onAlreadyUpToDate()
    fun onDownloadStart()
    fun onProgress(progress: Float)
    fun onDownloadComplete()
    fun onDownloadFailed(error: String)
}