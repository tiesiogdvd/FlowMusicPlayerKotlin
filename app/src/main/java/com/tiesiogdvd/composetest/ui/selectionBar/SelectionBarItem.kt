package com.tiesiogdvd.composetest.ui.selectionBar

import com.tiesiogdvd.composetest.R

enum class SelectionType{
    ALL_SONGS, PLAYLISTS, PLAYLIST, FOLDERS, FOLDER, STORAGE, FAVORITES, YT_DOWNLOAD
}

data class SelectionBarItem(
    val name:String,
    val selectionType: SelectionType,
    val isMultipleAllowed: Boolean,
    val icon: Int
)

object SelectionBarList{
    val list = listOf(

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.ALL_SONGS, isMultipleAllowed = true),
        SelectionBarItem("Hide", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.ALL_SONGS, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.ALL_SONGS, isMultipleAllowed = true),
        SelectionBarItem("Set playlist cover", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.ALL_SONGS, isMultipleAllowed = false),

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLIST, isMultipleAllowed = true),
        SelectionBarItem("Remove from playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLIST, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLIST, isMultipleAllowed = true),
        SelectionBarItem("Set playlist cover", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLIST, isMultipleAllowed = false),

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FAVORITES, isMultipleAllowed = true),
        SelectionBarItem("Remove from favorites", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FAVORITES, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FAVORITES, isMultipleAllowed = true),
        SelectionBarItem("Set favorites cover", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FAVORITES, isMultipleAllowed = false),

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDERS, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDERS, isMultipleAllowed = true),
        SelectionBarItem("Rename", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDERS, isMultipleAllowed = false),

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDER, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDER, isMultipleAllowed = true),
        SelectionBarItem("Rename", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.FOLDER, isMultipleAllowed = false),

        SelectionBarItem("Remove playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLISTS, isMultipleAllowed = true),
        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLISTS, isMultipleAllowed = true),
        SelectionBarItem("Rename playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.PLAYLISTS, isMultipleAllowed = false),

        SelectionBarItem("Add to playlist", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.STORAGE, isMultipleAllowed = true),
        SelectionBarItem("Delete", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.STORAGE, isMultipleAllowed = true),
        SelectionBarItem("Rename", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.STORAGE, isMultipleAllowed = false),


        SelectionBarItem("Download selected", icon = R.drawable.ic_action_playlist, selectionType = SelectionType.YT_DOWNLOAD, isMultipleAllowed = true)
    )
}
