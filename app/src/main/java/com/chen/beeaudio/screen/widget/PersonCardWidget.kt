package com.chen.beeaudio.screen.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.chen.beeaudio.R
import com.chen.beeaudio.mock.PremiumMock
import com.chen.beeaudio.mock.RequestUserDetailMock
import com.chen.beeaudio.model.audio.Premium
import com.chen.beeaudio.model.blog.RequestUserDetail
import com.chen.beeaudio.ui.theme.BeeAudioTheme

@Composable
fun PersonCardWidget(
    person : RequestUserDetail,
    isPremium : Premium,
    parentWidth : Int,
    onOpenPersonDetailEvent : () -> Unit
) {
    Card(
        modifier = Modifier
            .height(148.dp)
            .width(parentWidth.dp)
            .padding(vertical = 20.dp, horizontal = 18.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colors.primary.copy(alpha = .3f),
        elevation = 3.dp
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, nameText, descBrief, premiumLogo) = createRefs()
            val startGuideLine = createGuidelineFromStart(.3f)
            val centerGuideLine = createGuidelineFromTop(.5f)
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start, margin = 12.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(3.dp),
                shape = CircleShape,
                border = BorderStroke(3.dp, MaterialTheme.colors.surface),
            ) {
                AsyncImage(
                    model = person.avatar_url,
                    contentDescription = "${person.name}'s avatar",
                    placeholder = painterResource(id = R.drawable.personnel)
                )
            }
            Text(
                text = person.name,
                fontStyle = MaterialTheme.typography.h2.fontStyle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .constrainAs(nameText) {
                        start.linkTo(startGuideLine, margin = 10.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(centerGuideLine, margin = 2.dp)
                    }
            )
            Text(
                text = person.description,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onSurface.copy(.6f),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
                    .constrainAs(descBrief) {
                        start.linkTo(startGuideLine, margin = 10.dp)
                        top.linkTo(centerGuideLine, margin = 2.dp)
                        end.linkTo(parent.end, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    }
            )
            AnimatedVisibility(
                visible = isPremium.card_id.isNotEmpty(),
                modifier = Modifier.constrainAs(premiumLogo) {
                    start.linkTo(nameText.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(centerGuideLine, margin = 2.dp)
                }
            ) {
                PremiumLogoForUser()
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewPersonCardWidget() {
    BeeAudioTheme {
        PersonCardWidget(
            person = RequestUserDetailMock,
            isPremium = PremiumMock,
            parentWidth = LocalConfiguration.current.screenWidthDp,
            onOpenPersonDetailEvent =  {  }
        )
    }
}