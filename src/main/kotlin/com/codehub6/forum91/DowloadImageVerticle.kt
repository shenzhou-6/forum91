package com.codehub6.forum91

import io.vertx.core.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.file.createFileAwait
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.core.file.mkdirsAwait
import io.vertx.kotlin.core.file.writeFileAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.webClientOptionsOf
import kotlinx.coroutines.launch

/**
 * 图片下载
 */
class DowloadImageVerticle : CoroutineVerticle() {
  lateinit var webClient: WebClient
  private val log = LoggerFactory.getLogger(this::class.java)
  override suspend fun start() {
    val option = webClientOptionsOf()
    webClient = WebClient.create(vertx, option)

    val dowloadConfig = config.getJsonObject("dowload")
    vertx.eventBus().consumer<JsonArray>(DowloadImageVerticle::class.java.name) {
      val fileInfoList = it.body()
      val path = dowloadConfig.getString("path")
      launch {
        for (index in 0 until fileInfoList.size()){
          val fileInfo = fileInfoList.getJsonObject(index)
          val url = fileInfo.getString("url")
          val title = fileInfo.getString("title")
          val filename = fileInfo.getString("filename")
          val dir = path+title
          val file = "$dir/$filename"
          val fileSystem = vertx.fileSystem()
          if (!fileSystem.existsAwait(dir))fileSystem.mkdirsAwait(dir)
          fileDowload(url,file)
        }
      }
    }
  }

  /**
   * 文件下载
   * @param url 图片链接
   * @param path 文件存放路径
   */
  private suspend fun fileDowload(url: String, path: String) {
    val buffer = webClient.getAbs(url).sendAwait().bodyAsBuffer()
    val fileSystem = vertx.fileSystem()
    fileSystem.createFileAwait(path)
    fileSystem.writeFileAwait(path,buffer)
    println("$url dowload over......")
  }
}
