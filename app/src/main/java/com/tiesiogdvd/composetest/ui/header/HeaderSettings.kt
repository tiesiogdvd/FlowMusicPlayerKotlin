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
fun HeaderSettings(
    headerName:String,
    headerExtraText:String? = null,
    content: @Composable BoxScope.() -> Unit
){
    FlowPlayerTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = GetThemeColor.getBackground(isSystemInDarkTheme())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {

                Text(headerName, fontSize = 45.sp, modifier = Modifier.padding(bottom = 15.dp))
                if(headerExtraText!=null){
                    Text(headerExtraText, fontSize = 22.sp, modifier = Modifier.padding(bottom = 3.dp))
                }

                Box(modifier = Modifier
                    .fillMaxWidth(), content = content)
            }
        }

    }
}