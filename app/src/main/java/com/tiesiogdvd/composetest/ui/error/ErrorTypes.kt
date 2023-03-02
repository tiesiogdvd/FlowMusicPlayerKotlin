package com.tiesiogdvd.composetest.ui.error

enum class ErrorType(type: String, value: Int){
    LINK_NOT_FOUND ("Link not found", 1),
    EMPTY_PLAYLIST("Playlist empty", 2),
    NO_ERROR("No error", 0)
}
