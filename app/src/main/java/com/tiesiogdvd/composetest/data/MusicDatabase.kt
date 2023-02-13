package com.tiesiogdvd.playlistssongstest.data

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.di.SingletonComponentScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Playlist::class, Song::class], version = 2)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao():MusicDao

    class Callback @Inject constructor(
        private val database: Provider<MusicDatabase>,
       // private val songDataGetMusicInfo: SongDataGetMusicInfo,
        val songDataGetMusicInfo: Provider<SongDataGetMusicInfo>,
        val application: Application,
        @SingletonComponentScope private val applicationScope:CoroutineScope
    ):RoomDatabase.Callback(){
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().musicDao()
            applicationScope.launch {
                //songDataGetMusicInfo.getMusicInfo()
                songDataGetMusicInfo.get().getMusicInfo(dao,application)
                //songDataGetMusicInfo.getMusicInfo()
                dao.insertPlaylist(Playlist(playlistName = "name"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "hahda", songPath = "zhaha"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "hetfaha", songPath = "chahwa"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "hfsdaha", songPath = "ahafgha"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "vb", songPath = "hreaha"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "hvdddaha", songPath = "qqwqhaha"))

                dao.insertPlaylist(Playlist(playlistName = "Dududu"))
                dao.insertPlaylist(Playlist(playlistName = "Dududu"))
                dao.insertPlaylist(Playlist(playlistName = "Duduasddudididi"))
                dao.insertPlaylist(Playlist(playlistName = "Dudusfdudididi"))
                dao.insertPlaylist(Playlist(playlistName = "Duduewdudididi"))
                dao.insertPlaylist(Playlist(playlistName = "Dudufgdudididi"))
                dao.insertPlaylist(Playlist(playlistName = "Dududuqweqwedididi"))
            }

        }

    }


}