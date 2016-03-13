package com.flipcast.common

import akka.event.slf4j.Logger
import com.flipcast.config.{BaseConfig, ConfigurationProvider}
import com.google.common.cache.CacheBuilder
import scala.collection.JavaConverters._

/**
  * Configuration Manager which will manage configuration
  */
trait ConfigurationManager[T <: BaseConfig] {

  val log = Logger("ConfigurationManager")

  var configurationProvider: ConfigurationProvider[T] = null

  private val cache = CacheBuilder.newBuilder()
    .maximumSize(1024)
    .initialCapacity(8)
    .build[String, T]()

  def init() (implicit provider: ConfigurationProvider[T]) {
    val start = System.currentTimeMillis()
    log.info("Loading push configuration...")
    configurationProvider = provider
    val configs = provider.load()
    configs.foreach( c => {
      log.info("Loading configuration: " +c.config)
      cache.asMap().putIfAbsent(c.config, c)
    })
    val end = System.currentTimeMillis()
    log.info("Loaded push configuration in " +(end - start) +" ms")
  }

  def save(config: T) = {
    val result = configurationProvider.save(config)
    //    mediator ! Publish("pushConfig", PushConfigUpdatedMessage(config.configName, config))
    result
  }

  def config(configName: String) : Option[T] = {
    cache.asMap().containsKey(configName) match {
      case true => Option(cache.asMap().get(configName))
      case false => None
    }
  }

  def configs() : List[String] = {
    cache.asMap().keySet().asScala.toList
  }

  def delete(configName: String) = {
    val result = configurationProvider.delete(configName)
    result match {
      case true =>
      //        mediator ! Publish("pushConfig", PushConfigDeletedMessage(configName))
      case false => None
    }
    result
  }

  def updateConfig(config: T): Unit = {
    cache.put(config.config, config)
  }

  def deleteConfig(configName: String): Unit = {
    cache.invalidate(configName)
  }

}
