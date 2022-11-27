package com.thoughtworks.cconnapp.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconnapp.R

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    connState: ConnectionState,
    leftButton: LeftButton? = null,
    rightButton: RightButton? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.purple_700))
    ) {
        Column(
            modifier = modifier
                .width(150.dp)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.white)
            )
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .wrapContentWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Image(
                    painter = painterResource(
                        id = connectionStateLight(
                            connState
                        )
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = connectionStateText(LocalContext.current, connState),
                    color = colorResource(id = R.color.white)
                )
            }
        }
        leftButton?.let { button ->
            Image(
                painter = painterResource(id = button.iconId),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        button.action.invoke()
                    }
                    .align(alignment = Alignment.CenterStart)
            )
        }
        rightButton?.let { button ->
            ClickableText(
                text = AnnotatedString(
                    text = stringResource(id = button.text),
                    spanStyle = SpanStyle(
                        color = colorResource(id = R.color.purple_500),
                        fontSize = 18.sp
                    )
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .wrapContentHeight()
                    .padding(10.dp, 0.dp)
                    .wrapContentWidth()
                    .then(modifier),
                onClick = {
                    button.action.invoke()
                }
            )
        }
    }
}

fun connectionStateText(context: Context, connectionState: ConnectionState): String {
    return when (connectionState) {
        ConnectionState.CONNECTING -> context.getString(R.string.connecting)
        ConnectionState.CONNECTED -> context.getString(R.string.connected)
        ConnectionState.RECONNECTING -> context.getString(R.string.reconnecting)
        else -> context.getString(R.string.disconnected)
    }
}

fun connectionStateLight(connectionState: ConnectionState): Int {
    return when (connectionState) {
        ConnectionState.CONNECTING -> R.drawable.yellow_light
        ConnectionState.CONNECTED -> R.drawable.green_light
        ConnectionState.RECONNECTING -> R.drawable.yellow_light
        else -> R.drawable.red_light
    }
}

data class LeftButton(
    val iconId: Int,
    val action: () -> Unit,
)

data class RightButton(
    val text: Int,
    val action: () -> Unit,
)
