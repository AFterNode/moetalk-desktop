import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.slf4j.LoggerFactory
import server.Server
import ui.ui
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

@Composable
@Preview
fun App() {
    ui()
}

fun main() = application {
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
