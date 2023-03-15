package com.chen.beeaudio.screen.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chen.beeaudio.ui.theme.BeeAudioTheme
import com.chen.beeaudio.ui.theme.TealA700

/** Premium专享标识
 *
 */
@Composable
fun PremiumLogo(
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        elevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = "PRE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(color = TealA700)
                .padding(horizontal = 3.dp)
        )
    }
}

/** Premium专享标识
 *
 */
@Composable
fun PremiumLogoForUser(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = "PREMIUM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(color = TealA700)
                .padding(horizontal = 3.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewPremiumLogo() {
    BeeAudioTheme {
        PremiumLogo(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewPremiumLogoForUser() {
    BeeAudioTheme {
        PremiumLogoForUser(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}