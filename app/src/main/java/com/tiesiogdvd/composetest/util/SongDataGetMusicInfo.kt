package com.tiesiogdvd.composetest.util

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import java.io.File

object SongDataGetMusicInfo{
    suspend fun getMusicInfo(musicDao: MusicDao, context: Application) {
        val songList: ArrayList<Song> = ArrayList()
        if(!musicDao.playlistExists("All Songs")){
            musicDao.insertPlaylist(Playlist("All Songs"))
        }
        val playlist = musicDao.getPlaylist("All Songs")

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
        if (cursor != null) {
            for (i in 0 until cursor.count) {
                cursor.moveToNext()
                val isMusic = cursor.getInt(
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                )
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                if (isMusic != 0) {
                    if(!musicDao.songExistsInPlaylist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)), playlist.id)){
                        songList.add(Song(
                            songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                            songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            folder = File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))).parent,
                            length = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                            songArtist = if(artist=="<unknown>"){null}else{artist},
                            albumArtist = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST))}else{null},
                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                            genre = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))}else{null},
                            trackNumber = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)), //Might be CD TRACK NUMBER
                            year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
                            playlistId = playlist.id))
                    }
                }
            }
        }
        cursor?.close()
        musicDao.insertSongsList(songList)
    }
}