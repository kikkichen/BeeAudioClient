package com.chen.beeaudio.screen.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.chen.beeaudio.R

/** 错误数据提示
 *
 */
@Composable
fun ErrorDataTipsWidget(
    text: String,
    modifier: Modifier = Modifier,
    onClickEvent: () -> Unit = {  },
) {
    ConstraintLayout(
        modifier = modifier
            .clickable { onClickEvent() }
    ) {
        val block = createRef()
        Column(
            modifier = Modifier.constrainAs(block) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error_occurred),
                contentDescription = "Sorry, Here is a Error",
                tint = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                textAlign = TextAlign.Center
            )
        }
    }
}