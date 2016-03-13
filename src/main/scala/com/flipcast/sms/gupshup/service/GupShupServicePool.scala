package com.flipcast.sms.gupshup.service

import java.net.URLEncoder

import com.flipcast.Flipcast
import com.flipcast.sms.config.GupShupConfig
import com.google.common.cache.{CacheBuilder, CacheLoader}

/**
  * Created by phaneesh on 13/3/16.
  */
object GupShupServicePool {

  private lazy val serviceCache = CacheBuilder.newBuilder()
    .maximumSize(5000)
    .concurrencyLevel(30)
    .recordStats()
    .build(
      new CacheLoader[String, GupShupService]() {
        def load(key: String) = {
          val config = Flipcast.smsConfigurationProvider.config(key).provider.asInstanceOf[GupShupConfig]
          val uri = String.format("http://enterprise.smsgupshup.com/GatewayAPI/rest?&v=1.1&msg_type=TEXT&auth_scheme=PLAIN&userid=%s&password=%s&method=sendMessage", config.username, URLEncoder.encode(config.password, "UTF-8"))
          GupShupService(uri)
        }
      })

  def service(configName: String) = {
    serviceCache.get(configName)
  }

}
