package com.tiesiogdvd.composetest.ui.selectionBar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiesiogdvd.composetest.ui.theme.GetThemeColor
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SelectionBarComposable(
    items: List<SelectionBarItem>,
    noOfSelected: Int,
    totalSize: Int,
    selectionType: SelectionType,
    modifier: Modifier = Modifier,
    onItemClick: (SelectionBarItem) -> Unit,
    onCheckChange: () -> Unit
    ){
        Surface(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .height(90.dp),
            shape = RoundedCornerShape(30.dp),
            color = GetThemeColor.getBackgroundThird(isSystemInDarkTheme())
        ) {
                //---------------------------------------------------------------------------
            BottomAppBar(
                modifier = modifier,
                elevation = 0.dp,
                backgroundColor = Color.Transparent
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {


                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)

                        .height(30.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 20.dp)) {
                            Checkbox(checked = noOfSelected==totalSize, onCheckedChange = {onCheckChange()}, modifier = Modifier.scale(0.8f))
                            Text(text = "Select All", fontSize = 14.sp)
                        }

                        Text(text = "Selected $noOfSelected/$totalSize", modifier = Modifier.padding(end = 20.dp), fontSize = 14.sp)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()) {
                        items.forEach { item ->
                            if(item.selectionType==selectionType) {

                                AnimatedVisibility(visible = if(item.isMultipleAllowed) true else{
                                    noOfSelected==1
                                }) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { onItemClick(item) }) {
                                        Icon(
                                            painterResource(id = item.icon),
                                            contentDescription = item.name,
                                            modifier = Modifier
                                                .height(40.dp)

                                        )
                                        Text(
                                            modifier = Modifier
                                                .offset(y = -8.dp)
                                                .padding(bottom = 0.dp),
                                            text = item.name,
                                            textAlign = TextAlign.Center,
                                            fontSize = 10.sp
                                        )
                                    }

                                }


                            }

                        }

                    }
                }


                //---------------------------------------------------------------------------
            }
        }

}