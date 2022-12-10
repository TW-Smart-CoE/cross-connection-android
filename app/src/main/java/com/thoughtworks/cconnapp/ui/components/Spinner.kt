package com.thoughtworks.cconnapp.ui.components

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun <T> Spinner(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onMenuItemClick: (index: Int, item: T) -> Unit,
    itemToString: (item: T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    var currentSelectedItem by remember { mutableStateOf(selectedItem) }

    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        items.forEachIndexed { index, item ->
            DropdownMenuItem(
                modifier = Modifier
                    .wrapContentWidth(),
                onClick = {
                    expanded = false
                    currentSelectedItem = item
                    onMenuItemClick(index, item)
                }) {
                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = item.toString(),
                )
            }
        }
    }
    TextButton(
        modifier = modifier,
        onClick = { if (items.isNotEmpty()) expanded = !expanded }) {
        Text(text = itemToString(currentSelectedItem), modifier = Modifier.wrapContentWidth())
        Icon(Icons.Default.ArrowDropDown, contentDescription = "")
    }
}
