package com.thoughtworks.cconnapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconn.Method
import com.thoughtworks.cconn.utils.DataConverter
import com.thoughtworks.cconnapp.R

data class PubSubPanelData(
    val publishTopic: String = "/execute_cmd_list",
    val publishMethod: Method = Method.REQUEST,
    val publishData: String = "",
    val subscribeTopic: String = "/execute_cmd_list",
    val subscribeMethod: Method = Method.REQUEST,
    val subscribeState: Boolean = false,
)

interface PubSubPanelCallback {
    fun publish(topic: String, method: Method, data: ByteArray)
    fun subscribe(topic: String, method: Method)
    fun unsubscribe(topic: String, method: Method)
}

val methodList =
    listOf(
        Method.REPORT,
        Method.QUERY,
        Method.REPLY,
        Method.REQUEST,
        Method.RESPONSE
    )

@Composable
fun PubSubPanel(
    modifier: Modifier = Modifier,
    connectionState: ConnectionState,
    receivedData: String,
    pubSubPanelCallback: PubSubPanelCallback,
) {
    var panelData by remember { mutableStateOf(PubSubPanelData()) }

    Column(
        modifier = Modifier
            .then(modifier)
    ) {
        Text(
            modifier = Modifier
                .padding(8.dp),
            text = stringResource(id = R.string.publish),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(3f),
                singleLine = true,
                value = panelData.publishTopic,
                onValueChange = {
                    panelData = panelData.copy(publishTopic = it)
                },
                label = { Text(stringResource(id = R.string.publish_topic)) }
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            Spinner(
                modifier = Modifier
                    .weight(2f),
                items = methodList,
                selectedItem = panelData.publishMethod,
                onMenuItemClick = { _, item ->
                    panelData = panelData.copy(publishMethod = item)
                },
                itemToString = {
                    it.name
                }
            )
        }
    }
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = panelData.publishData,
        onValueChange = {
            panelData = panelData.copy(publishData = it)
        },
        label = { Text(stringResource(id = R.string.publish_data)) }
    )
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        enabled = connectionState == ConnectionState.CONNECTED,
        onClick = {
            pubSubPanelCallback.publish(
                panelData.publishTopic,
                panelData.publishMethod,
                DataConverter.stringToByteArray(panelData.publishData)
            )
        }) {
        Text(text = stringResource(id = R.string.publish))
    }
    Text(
        modifier = Modifier
            .padding(8.dp),
        text = stringResource(id = R.string.subscribe),
        fontWeight = FontWeight.Bold
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .weight(3f),
            singleLine = true,
            value = panelData.subscribeTopic,
            onValueChange = {
                panelData = panelData.copy(subscribeTopic = it)
            },
            label = { Text(stringResource(id = R.string.subscribe_topic)) }
        )
        Spacer(
            modifier = Modifier
                .width(8.dp)
        )
        Spinner(
            modifier = Modifier
                .weight(2f),
            items = methodList,
            selectedItem = panelData.subscribeMethod,
            onMenuItemClick = { _, item ->
                panelData = panelData.copy(subscribeMethod = item)
            },
            itemToString = {
                it.name
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            enabled = connectionState == ConnectionState.CONNECTED,
            onClick = {
                pubSubPanelCallback.subscribe(
                    panelData.subscribeTopic,
                    panelData.subscribeMethod
                )
            }) {
            Text(text = stringResource(id = R.string.subscribe))
        }
        Spacer(
            modifier = Modifier
                .width(8.dp)
        )
        Button(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            enabled = connectionState == ConnectionState.CONNECTED,
            onClick = {
                pubSubPanelCallback.unsubscribe(
                    panelData.subscribeTopic,
                    panelData.subscribeMethod
                )
            }) {
            Text(text = stringResource(id = R.string.unsubscribe))
        }
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        text = receivedData,
    )
}