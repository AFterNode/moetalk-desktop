package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import server.Server
import java.net.URI

@Composable
@Preview
fun homePage() {
    val snackbar = remember { SnackbarHostState() }
    var portInt by remember { mutableStateOf(8080) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("端口", modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                TextField(
                    value = portInt.toString(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        portInt = it.toIntOrNull() ?: portInt
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    if (Server.isRunning) {
                        CoroutineScope(Dispatchers.IO).launch {
                            snackbar.showSnackbar("服务器已经启动")
                        }
                    } else {
                        Server.port = portInt
                        Server.startAsync()
                        CoroutineScope(Dispatchers.IO).launch {
                            snackbar.showSnackbar("已在新线程启动服务")
                        }
                    }
                },
                content = { Text("启动") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Server.stop()
                            snackbar.showSnackbar("关闭中")
                        } catch (t: Throwable) {
                            Server.logger.error("Unable to stop", t)
                            snackbar.showSnackbar("出现错误：${t.message}")
                        }
                    }
                },
                content = { Text("关闭") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        java.awt.Desktop.getDesktop().browse(URI("http://127.0.0.1:$portInt"))
                    }
                },
                content = { Text("浏览器中打开") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
