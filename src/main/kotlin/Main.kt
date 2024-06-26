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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ajalt.mordant.terminal.Terminal
import org.apache.logging.log4j.core.config.Configurator
import server.MoeTalk
import server.Server
import ui.ui
import java.util.concurrent.Executors
import kotlin.system.exitProcess

val logCtx = Configurator.initialize("MoeTalkDesktop", MoeTalk::class.java.classLoader, "log4j2.xml")
private val logger by lazy { logCtx.getLogger("Main") }

val terminal by lazy { Terminal() }
val threads by lazy { Executors.newFixedThreadPool(3) }

@Composable
@Preview
fun App() {
    ui()
}

fun main() = application {
    logger.info("Starting application")
    Window(onCloseRequest = ::onExit, title = "MoeTalk Desktop") {
        App()
    }
}

private fun onExit() {
    logger.info("Exiting")
    if (Server.isRunning) {
        Server.stop()
    }
    exitProcess(0)
}
