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

package server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import logCtx
import org.slf4j.LoggerFactory

object Server {
    val logger = logCtx.getLogger("Server")

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
                install(CallLogging) {
                    disableDefaultColors()
                }

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