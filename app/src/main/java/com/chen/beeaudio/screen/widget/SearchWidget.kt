@file:Suppress("UNUSED_EXPRESSION")

package com.chen.beeaudio.screen.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun SearchWidget(
    text: String,
    onTextChange: (String) -> Unit,
    onSearchClicked: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .statusBarsPadding()
        ,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "SearchWidget"
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            elevation = 0.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = .1f),
            shape = CircleShape
        ) {
            BasicTextField(
                value = text,
                onValueChange = {
                    onTextChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "TextField"
                    }
                    .height(50.dp)
                    .background(color = Color.Transparent),
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 16.sp,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClicked(text)
                    }
                ),
                cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                decorationBox = { innerTextField ->
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (leading, trailing, textContent) = createRefs()
                        IconButton(
                            modifier = Modifier
                                .constrainAs(leading) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                }
                                .semantics {
                                    contentDescription = "CloseButton"
                                },
                            onClick = {
                                if (text.isNotEmpty()) {
                                    onTextChange("")
                                } else {
                                    onCloseClicked()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close Icon",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier.constrainAs(textContent) {
                                start.linkTo(leading.end)
                                top.linkTo(parent.top)
                                end.linkTo(trailing.start)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.fillToConstraints
                            },
                        ) {
                            if (text.isEmpty()) Text(
                                "搜索你感兴趣的音乐",
                                style = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            innerTextField()
                        }
                        IconButton(
                            modifier = Modifier
                                .alpha(alpha = ContentAlpha.medium)
                                .constrainAs(trailing){
                                      top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                }
                            ,
                            onClick = { onSearchClicked(text) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }
                }
            )
        }
    }
}

@Composable
@Preview
fun SearchWidgetPreview() {
    SearchWidget(
        text = "",
        onTextChange = {},
        onSearchClicked = {},
        onCloseClicked = {}
    )
}