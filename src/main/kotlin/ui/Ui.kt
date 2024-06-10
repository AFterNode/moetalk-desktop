/*
 *    Copyright 2024 afternode.cn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
