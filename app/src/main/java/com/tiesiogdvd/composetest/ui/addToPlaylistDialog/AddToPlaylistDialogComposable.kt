@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.addToPlaylistDialog

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.addPlaylistDialog.AddPlaylistDialog
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import com.tiesiogdvd.composetest.util.containtsPlaylist
import com.tiesiogdvd.playlistssongstest.data.Playlist
import com.tiesiogdvd.playlistssongstest.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


@Composable
fun AddToPlaylistDialog(
    songList: MutableStateFlow<HashMap<Int, Song>>? = null,
    singleSong : Flow<Song>? = null,
    onDismiss: () -> Unit,
    viewModel: AddToPlaylistDialogViewModel = hiltViewModel()
    ){
        val singleSongAsMap = mutableMapOf<Int,Song>()
        singleSong?.collectAsState(initial = null)?.value?.let { singleSongAsMap.put(0, it) }
        val isItemEnabled by remember{mutableStateOf(viewModel.isAddPlaylistDialogEnabled)}.value
        val playlists = viewModel.playlists.collectAsState(initial = emptyList()).value
        val songs = songList?.collectAsState(initial = emptyMap())?.value ?: singleSongAsMap
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Card(
                elevation = 5.dp,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .height(400.dp)
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), verticalArrangement = Arrangement.Center) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)) {
                        Text(text = "Playlists", modifier = Modifier.align(CenterVertically), fontSize = 25.sp, textAlign = TextAlign.Center)
                        Surface(modifier = Modifier
                            .clickable { viewModel.toggleAddPlaylistDialog() }
                            .padding(end = 5.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_action_add_playlist) , contentDescription = "Add playlist", modifier = Modifier
                                .size(35.dp)
                                .align(CenterVertically)
                                .padding(end = 2.dp))
                    }
                }

                if(isItemEnabled){
                    AddPlaylistDialog(
                        onDismiss = { viewModel.toggleAddPlaylistDialog()},
                        onRequestAddPlaylist = {playlistToAddName ->
                            if(playlists.containtsPlaylist(playlistToAddName)){
                                Toast.makeText(context, "Playlist already exists", Toast.LENGTH_SHORT).show()
                            }else{
                                viewModel.addPlaylist(playlistToAddName,songs)

                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }

                                println("SIZE OF " + songs.size)
                                viewModel.isAddPlaylistDialogEnabled.value = false
                            }
                        })
                }

                LazyColumn(state = listState, content = {
                    itemsIndexed(items = playlists, key = {index, playlist -> playlist}){
                        index, playlist ->
                        var isSelected by remember{ mutableStateOf(false)}
                        LaunchedEffect(index){
                            if(songs.size==1){
                                isSelected = playlist.containsSong(songs.values.first())
                            }else{
                                isSelected = playlist.containsSongsFromMap(songs)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .animateItemPlacement(animationSpec = tween(durationMillis = 300))
                                .fillMaxWidth(), verticalArrangement = Arrangement.Center
                        ) {
                            PlaylistItem(playlist = playlist.playlist, onToggle = {
                                viewModel.toggleAddStatus(playlistId = playlist.playlist.id, songs, isSelected) }, isSelected = isSelected, showCheckBox = true
                            )
                        }
                    }
                })
            }
        }
    }
}




@Composable
fun PlaylistItem(
    playlist: Playlist,
    onToggle: () -> Unit,
    isSelected: Boolean,
    showCheckBox: Boolean
){
    Surface(shape = RoundedCornerShape(10.dp), modifier = Modifier
        .padding(bottom = 5.dp)
        .clickable { onToggle() }, color = GetThemeColor.getBackground(isSystemInDarkTheme()).copy(0.5f)) {
        Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)) {
            Text(text = playlist.playlistName, textAlign = TextAlign.Center, modifier = Modifier.padding(start = 5.dp))
            if(showCheckBox){
                Checkbox(checked = isSelected, enabled = true , onCheckedChange = {onToggle()}, modifier = Modifier
                    .align(CenterVertically)
                    .scale(0.7f)
                    .padding(end = 2.dp))
            }
        }
    }

}