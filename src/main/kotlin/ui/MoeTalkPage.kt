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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import server.MoeTalk
import ui.components.checkBox
import ui.components.intInput
import ui.components.textInput
import java.net.URI

@Composable
@Preview
fun moeTalkPage() {
    val snackbar = remember { SnackbarHostState() }

    val proxyHost = remember { mutableStateOf("localhost") }
    val proxyPort = remember { mutableStateOf(10808) }
    val useProxy = remember { mutableStateOf(false) }
    val url = remember { mutableStateOf(MoeTalk.url) }

    var downloading by remember { mutableStateOf(false) }
    var downloadText by remember { mutableStateOf("下载中") }
    var downloadProgress by remember { mutableStateOf(0.0f) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            checkBox("使用代理", useProxy)
            if (useProxy.value) {
                textInput("代理主机", proxyHost, singleLine = true)
                intInput("代理端口", proxyPort)
            }

            textInput("下载地址", url, singleLine = true)

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            MoeTalk.url = url.value
                            MoeTalk.proxyHost = proxyHost.value
                            MoeTalk.proxyPort = proxyPort.value
                            MoeTalk.useProxy = useProxy.value

                            downloading = true
                            downloadText = "下载中"
                            downloadProgress = 0.0f

                            MoeTalk.install({ bytesRead, contentLength, done ->
                                downloadProgress = (bytesRead / contentLength).toFloat()
                            }, {
                                if (it == MoeTalk.InstallState.COMPLETED) {
                                    downloading = false
                                    launch {
                                        snackbar.showSnackbar("完成")
                                    }
                                }

                                downloadText = it.text
                            })
                        } catch (t: Throwable) {
                            downloading = false
                            MoeTalk.logger.error("Unable to download", t)
                            snackbar.showSnackbar("出现错误：${t.message}")
                        }
                    }
                },
                content = { Text("安装 / 更新") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        java.awt.Desktop.getDesktop().browse(URI(url.value))
                    }
                },
                content = { Text("浏览器中打开") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (downloading) {
            Dialog(
                onDismissRequest = {  },
                DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(downloadText)

                        if (downloadProgress > 0) {
                            CircularProgressIndicator(downloadProgress)
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
