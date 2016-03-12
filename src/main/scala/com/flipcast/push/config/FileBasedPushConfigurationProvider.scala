package com.flipcast.push.config

import com.typesafe.config.Config
import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author phaneesh
  */
class FileBasedPushConfigurationProvider(implicit config: Config) extends PushConfigurationProvider {

  /**
    * Load the configuration set.
    * This will be called at bootstrap
    */
  override def load(): List[PushConfig] = {
    config.getStringList("push.configs").asScala.map(c => {
      PushConfig(
        configName = c,
        gcmConfig(c),
        apnsConfig(c),
        mpnsConfig(c)
      )
    }).toList
  }

  /**
    * Reload the configuration.
    * This will make changing configuration easier
    */
  override def reload(): List[PushConfig] = load()

  /**
    * Get configuration for a given configuration
    *
    * @param configName Name of the configuration
    * @return The push Configuration for the given config name
    */
  override def config(configName: String): PushConfig = {
    PushConfig(configName, gcmConfig(configName), apnsConfig(configName), mpnsConfig(configName))
  }

  /**
    * Get all configuration names
    *
    * @return List fo all configuration names
    */
  override def configs(): List[String] = config.getStringList("push.configs").asScala.toList

  /**
    * Delete a push config
    *
    * @param configName Name of the configuration that needs to be deleted
    * @return operation result
    */
  override def delete(configName: String): Boolean = true

  /**
    * Save/Update push configuration
    *
    * @param pushConfig Push configuration
    */
  override def save(pushConfig: PushConfig): Boolean = true

  private def gcmConfig(configName: String) = {
    GcmConfig(
      Try(config.getString("push." + configName + ".gcm.apiKey")).getOrElse("INVALID"),
      Try(config.getBoolean("push." + configName + ".gcm.defaultDelayWhileIdle")).getOrElse(false),
      Try(config.getInt("push." + configName + ".gcm.defaultExpiry")).getOrElse(600)
    )
  }

  private def apnsConfig(configName: String) = {
    ApnsConfig(
      Try(config.getString("push." + configName + ".apns.certificate")).getOrElse("INVALID"),
      Try(config.getString("push." + configName + ".apns.password")).getOrElse("INVAID"),
      Try(config.getBoolean("push." + configName + ".apns.sandbox")).getOrElse(true),
      Try(config.getInt("push." + configName + ".apns.defaultExpiry")).getOrElse(300)
    )
  }

  private def mpnsConfig(configName: String) = {
    MpnsConfig(
      Try(config.getBoolean("push." + configName + ".mpns.secured")).getOrElse(false),
      Try(config.getString("push." + configName + ".mpns.certificate")).getOrElse("INVALID"),
      Try(config.getString("push." + configName + ".mpns.password")).getOrElse("INVAID"),
    )
  }
}
