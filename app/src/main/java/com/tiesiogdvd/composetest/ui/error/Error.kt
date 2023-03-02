package com.tiesiogdvd.composetest.ui.error

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor

@Composable
fun ErrorText(
    errorType: ErrorType,
    onClickClose: () -> Unit
){
    Surface(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp)
        .height(40.dp), color = GetThemeColor.getError(isSystemInDarkTheme()), shape = RoundedCornerShape(30.dp)) {
        Box(modifier = Modifier.padding(start = 10.dp), contentAlignment = Alignment.CenterStart){
           // Text(text = errorType.name, color = GetThemeColor.getText(isSystemInDarkTheme()), fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = errorType.name, color = GetThemeColor.getText(isSystemInDarkTheme()), fontSize = 15.sp)
                Icon(Icons.Default.Close, contentDescription = "Close", tint = GetThemeColor.getText(isSystemInDarkTheme()), modifier = Modifier.padding(end = 10.dp).clickable { onClickClose() })
            }
        }
    }
}
