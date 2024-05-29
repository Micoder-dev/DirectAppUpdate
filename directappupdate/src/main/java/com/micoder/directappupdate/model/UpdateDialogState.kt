package com.micoder.directappupdate.model

data class UpdateDialogState(
    val visible: Boolean = false,
    val updateType: UpdateType = UpdateType.None,
    val status: String = "",
    val progress: Float = 0f,
    val showUpdateButton: Boolean = false
)