package com.tiesiogdvd.playlistssongstest.data

import android.app.Application
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiesiogdvd.composetest.data.PlaylistSongCrossRef
import com.tiesiogdvd.composetest.util.SongDataGetMusicInfo
import com.tiesiogdvd.playlistssongstest.di.SingletonComponentScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Playlist::class, Song::class, PlaylistSongCrossRef::class], version = 2)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao():MusicDao

    class Callback @Inject constructor(
        private val database: Provider<MusicDatabase>,
        val songDataGetMusicInfo: Provider<SongDataGetMusicInfo>,
        val application: Application,
        @SingletonComponentScope private val applicationScope:CoroutineScope
    ):RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().musicDao()
            applicationScope.launch {
                songDataGetMusicInfo.get().getMusicInfo(dao,application)
               /* dao.insertPlaylist(Playlist(playlistName = "name"))
                dao.insertSong(Song(playlistId = dao.getPlaylist("name").id, songName = "hahda", songPath = "zhaha"))
                dao.insertPlaylist(Playlist(playlistName = "Dududuqweqwedididi"))*/
            }

        }

    }


}