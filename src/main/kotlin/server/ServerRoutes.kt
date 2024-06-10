package server

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Route.routeMoeTalk() {
    staticFiles("/", File("moe-talk"))
}
