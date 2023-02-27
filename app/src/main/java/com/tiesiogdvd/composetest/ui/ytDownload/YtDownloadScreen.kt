@file:OptIn(ExperimentalFoundationApi::class)

package com.tiesiogdvd.composetest.ui.ytDownload

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarComposable
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionBarList
import com.tiesiogdvd.composetest.ui.selectionBar.SelectionType
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


@Composable
fun YtDownloadScreen(viewModel: YtDownloadViewModel = hiltViewModel()) {
    FlowPlayerTheme {
        var text by remember { mutableStateOf("") }
        val selectionListSize = viewModel.selection.collectAsState().value
        val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value
        val totalSize = viewModel.itemListFlow.collectAsState().value.size

        Scaffold(bottomBar = {
            AnimatedVisibility(
                visible = isSelectionBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    SelectionBarComposable(items = SelectionBarList.list,
                        selectionType = SelectionType.YT_DOWNLOAD,
                        onItemClick = {
                            when(it.name){
                                "Download selected" -> viewModel.onDownloadSelected()
                            }
                            println(it.name)
                            println(selectionListSize)
                        }, onCheckChange = {viewModel.toggleSelectAll()}, noOfSelected = selectionListSize, totalSize = totalSize)
                }
            }

        }, content = {
                padding-> Column(modifier = Modifier.padding(padding))
        {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = GetThemeColor.getBackground(isSystemInDarkTheme()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp, start = 25.dp, end = 20.dp)
                ) {


                    Text("YT Downloader", fontSize = 45.sp, modifier = Modifier.padding(bottom = 15.dp))
                    Text("Enter song or playlist link", fontSize = 22.sp, modifier = Modifier.padding(bottom = 3.dp))

                    TextField(
                        value = text,
                        enabled = true,
                        singleLine = true,
                        onValueChange = { newText ->
                            viewModel.onInputChanged(newText)
                            text = newText
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 3.dp)
                            .padding(end = 10.dp),
                        textStyle = MaterialTheme.typography.body1.copy(
                            fontSize = 20.sp,
                            color = GetThemeColor.getText(isSystemInDarkTheme())
                        )
                    )

                    Surface(shape = RoundedCornerShape(30.dp),
                        color = GetThemeColor.getButton(isSystemInDarkTheme()),
                        modifier = Modifier
                            .height(50.dp)
                            .padding(top = 15.dp)
                            .padding(end = 10.dp)
                            .fillMaxWidth()
                            .clickable { viewModel.loadSongsFromLink() }) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = "Load songs",
                                fontSize = 15.sp,
                                color = GetThemeColor.getText(isSystemInDarkTheme()),
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 1.dp)
                                    .height(50.dp)
                                    .wrapContentSize()
                            )
                        }
                    }
                    DownloadableList()
                }

            }
        }
        })


    }
}



@Composable
fun DownloadableList(viewModel: YtDownloadViewModel = hiltViewModel()) {
    var downloadMap = viewModel.itemListFlow.collectAsState()
    val isSelectionBarVisible = viewModel.isSelectionBarVisible.collectAsState().value
    BackHandler(onBack = {
        if(isSelectionBarVisible){
            viewModel.toggleSelectionBar(false)
        }
    }, enabled = true)

    LazyColumn {
        items(downloadMap.value.toList()) { (key, item) ->
            val animatedOpacity = remember { Animatable(0f) }
            LaunchedEffect(downloadMap) {
                launch {
                    animatedOpacity.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }

            Column(modifier = Modifier
                .alpha(animatedOpacity.value)
                .combinedClickable(
                    onClick = {
                        println("hahahah")
                        if (isSelectionBarVisible) {
                            viewModel.toggleSelection(key)
                        }
                    },
                    onLongClick = {

                    })
                .animateItemPlacement())
            {
                DownloadableItemTest(songItem = item, item.isSelected, item.downloadState )
            }

        }
        item {
            Spacer(modifier = Modifier.padding(70.dp))
        }

    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DownloadableItemTest(
    songItem: DownloadableItem,
    isSelected:MutableStateFlow<Boolean>,
    downloadState: MutableStateFlow<DownloadState>
){
    var selected = isSelected.collectAsState().value
    var downloadType = downloadState.collectAsState().value
    val imageSource = songItem.imageSource.collectAsState().value
    val initialColor = if(!selected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}
    var surfaceColor by remember { mutableStateOf(initialColor) }
    val targetColor = if(downloadType == DownloadState.FINISHED){GetThemeColor.getGreen(isSystemInDarkTheme())}else{if(!selected){GetThemeColor.getButton(isSystemInDarkTheme())}else{GetThemeColor.getPurple(isSystemInDarkTheme())}}
    val animatedColor by animateColorAsState(targetValue = targetColor, tween(durationMillis = 400, easing = EaseIn))

    LaunchedEffect(animatedColor) {
        surfaceColor = animatedColor
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 10.dp)
            .padding(vertical = 5.dp),
        color = Color.Transparent
    ){
        Row(
            modifier = Modifier,
        ) {
            Surface(shape = RoundedCornerShape(30.dp), modifier = Modifier.align(Alignment.CenterVertically), color = GetThemeColor.getButton(isSystemInDarkTheme())) {
                if(imageSource!=null){
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(imageSource).crossfade(true).build(), contentDescription = "Downloadable Image")
                }
                if (downloadType != DownloadState.FINISHED && downloadType!=DownloadState.SELECTION) {
                    LinearProgressIndicator(modifier = Modifier.width(100.dp))
                }
                
            }
            Spacer(modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterVertically))
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(50.dp)
                    .fillMaxWidth(),
                color = surfaceColor,
                //color = GetThemeColor.getButton(isSystemInDarkTheme()),
                shape = RoundedCornerShape(30.dp)) {
                Row(modifier = Modifier
                    .padding(start = 15.dp)

                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier
                        .align(Alignment.CenterVertically)) {
                        Text(text = songItem.name, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, color = if(selected){GetThemeColor.getText(!isSystemInDarkTheme())} else {GetThemeColor.getText(isSystemInDarkTheme())}, modifier = Modifier.wrapContentHeight(Alignment.Bottom))
                    }
                }
            }
        }
    }
}
