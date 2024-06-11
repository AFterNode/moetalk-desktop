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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import server.MoeTalk
import ui.components.checkBox
import ui.components.intInput
import ui.components.progress
import ui.components.textInput
import java.net.URI

@Composable
@Preview
fun moeTalkPage() {
    var selectedItem by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedItem) {
            Tab(
                selected = selectedItem == 0,
                text = { Text("HTTP") },
                onClick = { selectedItem = 0 }
            )
            Tab(
                selected = selectedItem == 1,
                text = { Text("Git") },
                onClick = { selectedItem = 1 }
            )
        }

        when (selectedItem) {
            0 -> moeTalkInstallHttp()
            1 -> moeTalkInstallGit()
        }
    }
}

@Composable
@Preview
private fun moeTalkInstallHttp() {
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

                            MoeTalk.installHttp({ bytesRead, contentLength, done ->
                                downloadProgress = (bytesRead / contentLength).toFloat()
                            }, {
                                if (it == MoeTalk.InstallState.HTTP_COMPLETED) {
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
            progress(downloadText, downloadProgress)
        }
    }
}

@Composable
@Preview
private fun moeTalkInstallGit() {
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) {
        var url = remember { mutableStateOf(MoeTalk.gitUrl) }

        var executing by remember { mutableStateOf(false) }
        var execText by remember { mutableStateOf("") }
        var execProg by remember { mutableStateOf(-1f) }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            textInput("Git地址", url)

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        MoeTalk.gitUrl = url.value

                        try {
                            executing = true
                            MoeTalk.installGit { task, completed, total, done ->
                                if (done) {
                                    executing = false
                                    launch {
                                        snackbar.showSnackbar("安装完成")
                                    }
                                }
                                execText = "$task ($completed/$total)"
//                                execProg = progress
                            }
                        } catch (t: Throwable) {
                            executing = false
                            MoeTalk.logger.error("Unable to install with git", t)
                            snackbar.showSnackbar("安装失败：${t.message}")
                        }
                    }
                },
                content = { Text("安装") }
            )
        }

        if (executing) {
            progress(execText, execProg)
        }
    }
}
