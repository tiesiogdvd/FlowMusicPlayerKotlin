package com.tiesiogdvd.composetest.ui.ytDownload

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiesiogdvd.composetest.ui.theme.FlowPlayerTheme
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor


@Composable
fun YtDownloadScreen(viewModel: YtDownloadViewModel = hiltViewModel()) {
    FlowPlayerTheme {
        var text by remember { mutableStateOf("") }
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
                        .padding(start = 10.dp),
                    textStyle = MaterialTheme.typography.body1.copy(
                        fontSize = 12.sp,
                        color = GetThemeColor.getText(isSystemInDarkTheme())
                    )
                )


                Surface(shape = RoundedCornerShape(30.dp),
                    color = GetThemeColor.getButton(isSystemInDarkTheme()),
                    modifier = Modifier
                        .height(50.dp)
                        .padding(top = 15.dp)
                        .padding(end = 20.dp)
                        .fillMaxWidth()
                        .clickable { viewModel.onButtonPress() }) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = "Load song",
                            fontSize = 20.sp,
                            color = GetThemeColor.getText(isSystemInDarkTheme()),
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 1.dp)
                                .height(50.dp)
                                .wrapContentSize()
                        )
                    }
                }
            }

        }
    }
}