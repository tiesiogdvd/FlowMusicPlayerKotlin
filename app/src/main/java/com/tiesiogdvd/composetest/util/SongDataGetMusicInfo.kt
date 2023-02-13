package com.tiesiogdvd.composetest.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.tiesiogdvd.playlistssongstest.data.MusicDao
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import java.io.File
import javax.inject.Inject

object SongDataGetMusicInfo{

    @SuppressLint("Range", "Recycle")
    suspend fun getMusicInfo(    musicDao: MusicDao,
                                 context: Application) {


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
                    cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                )
                //println(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                if (isMusic != 0) {
                   // println(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                    if(!musicDao.songExistsInPlaylist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)), playlist.id)){
                    musicDao.insertSong(Song(
                            songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                            songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            folder = File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))).parent,
                            length = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                            songArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                            albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)),
                            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                            genre = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)),
                            trackNumber = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)), //Might be CD TRACK NUMBER
                            year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
                            playlistId = playlist.id)

                    )
                }
                    }
            }
        }
    }
}