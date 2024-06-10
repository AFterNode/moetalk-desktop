package server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

object Server {
    val logger = LoggerFactory.getLogger("Server")

    var port: Int = 8080

    private var thread: Thread? = null
    private var engine: NettyApplicationEngine? = null

    val isRunning: Boolean
        get() = engine != null

    fun startAsync() {
        thread = Thread(::start).apply {
            name = "Server Thread"
            start()
        }
    }

    fun start() {
        try {
            logger.info("Starting server on $port")
            engine = embeddedServer(Netty, port = port) {
                install(CallLogging)

                routing {
                    routeMoeTalk()
                }
            }
            engine!!.start(wait = true)
        } catch (t: Throwable) {
            logger.error("Server startup error", t)
            engine = null
        }
    }

    fun stop() {
        if (engine == null) throw NullPointerException("No application engine")
        logger.info("Stopping server")
        engine!!.stop()
        engine = null
    }
}