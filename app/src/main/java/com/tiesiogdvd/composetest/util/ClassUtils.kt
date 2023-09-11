package com.tiesiogdvd.composetest.util

import com.tiesiogdvd.playlistssongstest.data.PlaylistWithSongs

fun List<PlaylistWithSongs>.containtsPlaylist(playlistName: String):Boolean{
    for(playlistWithSongs in this){
        if(playlistName==playlistWithSongs.playlist.playlistName){
            return true
        }
    }
    return false
}