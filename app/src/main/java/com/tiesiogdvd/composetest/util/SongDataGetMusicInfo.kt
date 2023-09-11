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
        val songListNotInDatabase: ArrayList<Song> = ArrayList()
        val songList: List<Long>
        if(!musicDao.playlistExists("All Songs")){
            musicDao.insertPlaylist(Playlist("All Songs", isHidden = true))
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
                    val songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val song = Song(
                        songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                        songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                        folder = File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))).parent,
                        length = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                        songArtist = if(artist=="<unknown>"){null}else{artist},
                        albumArtist = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST))}else{null},
                        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                        genre = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))}else{null},
                        trackNumber = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)), //Might be CD TRACK NUMBER
                        year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))
                    )
                    if(musicDao.songExists(songPath) == false){
                        songListNotInDatabase.add(song)
                    }
/*                    else{
                        println(song.songName)
                        val id = musicDao.getSong(songPath)?.id
                        if(id!=null && musicDao.songExistsInPlaylist(songId = id, playlistId = playlist.id) == false){
                            val songItem = musicDao.getSong(songPath)
                            if(songItem!=null){
                                songList.add(songItem)
                            }
                        }
                    }*/
                }
            }
        }
        cursor?.close()
        songList = musicDao.insertSongs(songListNotInDatabase)
        songList.forEach{ println(it)}
        musicDao.insertSongsToPlaylist(songIds = songList, playlistId = playlist.id)
    }
}