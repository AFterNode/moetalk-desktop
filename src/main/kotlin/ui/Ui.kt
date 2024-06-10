package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

val colors by lazy {
    lightColors(
        primary = Color(0xff95bae5),
        secondary = Color(0xff4f5b6d)
    )
}

@Composable
fun ui() {
    var selectedItem by remember { mutableStateOf(0) }

    MaterialTheme(
        colors = colors
    ) {
        Column {
            TabRow(selectedTabIndex = selectedItem) {
                Tab(
                    selected = selectedItem == 0,
                    text = { Text("主页") },
                    onClick = { selectedItem = 0 }
                )
                Tab(
                    selected = selectedItem == 1,
                    text = { Text("MoeTalk") },
                    onClick = { selectedItem = 1 }
                )
                Tab(
                    selected = selectedItem == 2,
                    text = { Text("帮助") },
                    onClick = { selectedItem = 2 }
                )
            }

            when (selectedItem) {
                0 -> homePage()
                1 -> moeTalkPage()
                2 -> helpPage()
            }
        }
    }
}
