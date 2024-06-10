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

import logCtx
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import utils.Downloader
import java.io.ByteArrayInputStream
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object MoeTalk {
    val logger = logCtx.getLogger("Installer")
    val installDir by lazy { File("moe-talk") }

    var proxyHost: String? = null
    var proxyPort: Int = 0
    var useProxy: Boolean = false

    var url: String = "https://mirror.ghproxy.com/https://github.com/ggg555ttt/MoeTalk/archive/refs/heads/main.zip"

    fun install(listener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit, stateListener: (InstallState) -> Unit) {
        logger.info("Downloading package: $url")
        val client = Downloader.requestProgress(
            OkHttpClient.Builder().apply {
                if (useProxy) {
                    logger.info("Using proxy: $proxyHost:$proxyPort")
                    proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyHost, proxyPort)))
                }
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(10, TimeUnit.SECONDS)
                retryOnConnectionFailure(false)
            },
            listener
        )
        stateListener(InstallState.DOWNLOAD)
        val data = client.newCall(Request.Builder().url(url).build()).execute().use {
            it.body!!.bytes()
        }

        stateListener(InstallState.EXTRACT)
        val path = installDir.toPath()
        if (!path.exists()) path.createDirectories()
        ZipInputStream(ByteArrayInputStream(data)).use {
            var entry = it.nextEntry
            while (entry != null) {
                logger.info("Extracting: ${entry.name}")
                val sub = path.resolve(entry.name)
                if (entry.name.endsWith("/") && !sub.exists()) {
                    sub.createDirectories()
                } else {
                    Files.copy(it, sub, StandardCopyOption.REPLACE_EXISTING)
                }

                entry = it.nextEntry
            }
            it.closeEntry()
        }

        stateListener(InstallState.COMPLETED)
    }

    enum class InstallState(val text: String) {
        DOWNLOAD("下载中"), EXTRACT("解压中"), COMPLETED("完成")
    }
}