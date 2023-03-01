package com.tiesiogdvd.composetest.ui.sortOrderDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tiesiogdvd.composetest.data.PlaylistSortOrder
import com.tiesiogdvd.composetest.data.SongSortOrder
import com.tiesiogdvd.composetest.data.SortOrder
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor

@Composable
fun SortOrderPlaylistsDialog(
    playlistsSortOrder: PlaylistSortOrder,
    sortOrder: SortOrder,
    onDismiss: () -> Unit,
    onPlaylistSortOrderSelected: (PlaylistSortOrder) -> Unit,
    onSortTypeSelected: (SortOrder) -> Unit,
){
    Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(
            elevation = 5.dp,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth(0.8F)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)) {
                Text(text = "Sort order", fontSize = 16.sp, modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(), textAlign = TextAlign.Start)
                LazyVerticalGrid(columns = GridCells.Fixed(3), content = {
                    PlaylistSortOrder.values().forEach {m_playlistsSortOrder ->
                        item {
                            Surface(shape = RoundedCornerShape(30.dp),
                                color = if(m_playlistsSortOrder==playlistsSortOrder) GetThemeColor.getPurple(isSystemInDarkTheme()) else GetThemeColor.getBackgroundSecondary(isSystemInDarkMode = isSystemInDarkTheme()),
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp)
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                                    .clickable {
                                        onPlaylistSortOrderSelected(m_playlistsSortOrder)
                                    }) {
                                Text(text = m_playlistsSortOrder.playlistSortOrderText, fontSize = 12.sp, modifier = Modifier
                                    .wrapContentSize(), textAlign = TextAlign.Center)
                             }
                        }
                    }
                })
                Spacer(modifier = Modifier.height(10.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(3), content = {
                    SortOrder.values().forEach {m_sortOrder ->
                        item {
                            Surface(shape = RoundedCornerShape(30.dp),
                                color = if(m_sortOrder==sortOrder) GetThemeColor.getPurple(isSystemInDarkTheme()) else GetThemeColor.getBackgroundSecondary(isSystemInDarkMode = isSystemInDarkTheme()),
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp)
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                                    .clickable {
                                        onSortTypeSelected(m_sortOrder)
                                    }) {
                                Text(text = m_sortOrder.sortOrderText, fontSize = 12.sp, modifier = Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                })
            }
        }
    }
}