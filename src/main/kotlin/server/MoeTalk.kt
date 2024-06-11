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

import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import logCtx
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor
import terminal
import threads
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

    var gitUrl: String = "https://ghproxy.org/https://github.com/ggg555ttt/MoeTalk.git"
    var git: Git? = null
        private set

    fun installHttp(listener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit, stateListener: (InstallState) -> Unit) {
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
        stateListener(InstallState.HTTP_DOWNLOAD)
        val data = client.newCall(Request.Builder().url(url).build()).execute().use {
            it.body!!.bytes()
        }

        stateListener(InstallState.HTTP_EXTRACT)
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

        stateListener(InstallState.HTTP_COMPLETED)
    }

    fun installGit(listener: (task: String, completed: Int, total: Int, done: Boolean) -> Unit) {
        listener("启动中", 0, 0, false)
        logger.info("Starting git clone from $gitUrl")
        val progress = progressBarLayout {
            progressBar()
        }.animateInCoroutine(terminal)
        CoroutineScope(threads.asCoroutineDispatcher()).launch { progress.execute() }
        git = Git.cloneRepository()
            .setURI(gitUrl)
            .setDirectory(installDir)
            .setProgressMonitor(object: ProgressMonitor {
                var totWrk = 0
                var tskTit = ""

                override fun start(totalTasks: Int) {
                    logger.info("Git开始")
                    listener("Git开始", 0, 0, false)
                }

                override fun beginTask(title: String?, totalWork: Int) {
                    progress.update {
                        completed = 0
                        total = totalWork.toLong()
                    }

                    tskTit = title ?: "未知任务"
                    totWrk = totalWork
                    listener(tskTit, 0, totalWork, false)
                }

                override fun update(comp: Int) {
                    progress.update {
                        this.completed = comp.toLong()
                    }
                    listener(tskTit, comp, totWrk, false)
                }

                override fun endTask() {}
                override fun isCancelled(): Boolean = false
                override fun showDuration(enabled: Boolean) {}

            })
            .call()
        listener("完成", 1, 1, true)
    }

    fun updateGit(listener: (InstallState) -> Unit) {
        if (git == null) {
            logger.info("Opening git repository")
            git = Git.open(installDir)
        }

        listener(InstallState.GIT_PULL)
        logger.info("Starting git pull")
        git!!
            .pull()
            .call()
        listener(InstallState.GIT_COMPLETED)
    }

    enum class InstallState(val text: String) {
        HTTP_DOWNLOAD("下载中"), HTTP_EXTRACT("解压中"), HTTP_COMPLETED("完成"),
        GIT_CLONE("执行Git Clone"), GIT_PULL("执行Git Pull"), GIT_COMPLETED("Git完成")
    }
}