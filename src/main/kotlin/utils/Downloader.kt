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

package utils

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.*
import java.io.IOException


object Downloader {
    fun requestProgress(client: OkHttpClient.Builder, listener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit): OkHttpClient {
        return client
            .addNetworkInterceptor {
                    val resp = it.proceed(it.request())
                    resp.newBuilder()
                        .body(ProgressResponseBody(resp.body!!, listener))
                        .build()
                }
            .build()

    }

    private class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit
    ) :
        ResponseBody() {
        private var bufferedSource: BufferedSource? = null

        override fun contentType(): okhttp3.MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): ForwardingSource {
            return object : ForwardingSource(source) {
                var totalBytesRead: Long = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead: Long = super.read(sink, byteCount)
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    progressListener(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
                    return bytesRead
                }
            }
        }
    }
}
