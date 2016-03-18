package com.flipcast.sms.config

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author phaneesh
  */
class FileBasedSmsConfigurationProvider (implicit smsConfig: Config) extends SmsConfigurationProvider {

  /**
    * Load the configuration set.
    * This will be called at bootstrap
    */
  override def load(): List[SmsConfig] = {
    smsConfig.getStringList("sms.configs").asScala.map(c => {
      SmsConfig(
        configName = c,
        smsConfigByType(c, smsConfig.getString("sms." +c +".type")),
        templated = Try(smsConfig.getBoolean("sms." +c +".templated")).getOrElse(false),
        templatePath = Try(Option(smsConfig.getString("sms." +c +".templated"))).getOrElse(None)
      )
    }).toList
  }

  /**
    * Reload the configuration.
    * This will make changing configuration easier
    */
  override def reload(): List[SmsConfig] = load()

  /**
    * Get configuration for a given configuration
    *
    * @param configName Name of the configuration
    * @return The push Configuration for the given config name
    */
  override def config(configName: String): SmsConfig = {
    SmsConfig(configName,
      gupshupConfig(configName),
      Try(smsConfig.getBoolean("sms." +configName +".template")).getOrElse(false),
      templatePath = Try(Option(smsConfig.getString("sms." +configName +".templatePath"))).getOrElse(None)
    )
  }

  /**
    * Get all configuration names
    *
    * @return List fo all configuration names
    */
  override def configs(): List[String] = smsConfig.getStringList("sms.configs").asScala.toList

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
  override def save(pushConfig: SmsConfig): Boolean = true


  private def smsConfigByType(configName: String, configType: String) = {
    configType match {
      case "gupshup" =>
        gupshupConfig(configName)
      case _ =>
        GupShupConfig("INVALID", "INVALID")
    }
  }

  private def gupshupConfig(configName: String) = {
    GupShupConfig(
      Try(smsConfig.getString("sms." + configName +".gupshup.username")).getOrElse("INVALID"),
      Try(smsConfig.getString("sms." + configName +".gupshup.password")).getOrElse("INVALID")
    )
  }
}
