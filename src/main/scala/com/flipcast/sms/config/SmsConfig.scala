package com.flipcast.sms.config

import com.flipcast.config.BaseConfig

/**
  * @author phaneesh
  */
case class SmsConfig(configName: String, provider: SmsProviderConfig, templated: Boolean, templatePath: Option[String]) extends BaseConfig {

  def config = configName
}
