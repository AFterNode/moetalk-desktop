package ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun textInput(title: String,
              default: MutableState<String>,
              keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
              singleLine: Boolean = false,
              titlePaddingEnd: Dp = 16.dp
): String {
    var state by remember { default }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.padding(start = 16.dp, end = titlePaddingEnd))
        TextField(
            value = state,
            keyboardOptions = keyboardOptions,
            onValueChange = {
                state = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine
        )
    }

    return state
}

@Composable
fun intInput(title: String,
              default: MutableState<Int>,
              titlePaddingEnd: Dp = 16.dp
): Int {
    var state by remember { default }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.padding(start = 16.dp, end = titlePaddingEnd))
        TextField(
            value = state.toString(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            onValueChange = {
                state = it.toIntOrNull() ?: state
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    return state
}

@Composable
fun checkBox(title: String,
             default: MutableState<Boolean>,
             titlePaddingEnd: Dp = 16.dp): MutableState<Boolean> {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.padding(start = 16.dp, end = titlePaddingEnd))
        Checkbox(
            checked = default.value,
            onCheckedChange = {
                default.value = it
            }
        )
    }
    return default
}
