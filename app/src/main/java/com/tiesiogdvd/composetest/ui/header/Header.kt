package com.tiesiogdvd.composetest.ui.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.R
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import kotlinx.coroutines.flow.MutableStateFlow



@Composable
fun Header(
    bitmapSource:MutableStateFlow<ImageBitmap?>,
    headerName:String,
    height: Dp = 250.dp,
    headerExtraText:String? = "",
    scrollOffsetY: MutableState<Float>
    ){

    val bitmap = bitmapSource.collectAsState().value
    FlowPlayerTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier.height(height)) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "desc",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .offset(y = with(LocalDensity.current){-scrollOffsetY.value.toDp()})
                                .fillMaxSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_bg_8),
                            contentDescription = "desc",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .offset(y = with(LocalDensity.current){-scrollOffsetY.value.toDp()})
                                .fillMaxSize(),
                        )
                    }

                    Surface(color = Color.Transparent,modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0F to Color.Transparent,
                                0.5F to GetThemeColor
                                    .getBackground(isSystemInDarkTheme())
                                    .copy(alpha = 0.4F),
                                1F to GetThemeColor
                                    .getBackground(isSystemInDarkTheme())
                                    .copy(alpha = 1F)
                            )
                        )
                    ){
                    }

                    Column(modifier = Modifier
                        .offset(y= with(LocalDensity.current){-scrollOffsetY.value.toDp()/3})
                        .padding(start = 25.dp, bottom = 40.dp)
                        .align(Alignment.BottomStart)) {
                        Text(headerName, fontSize = 40.sp)
                    }
                }
            }
        }

    }
}