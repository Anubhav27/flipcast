package com.flipcast.sms.config

import com.flipcast.config.BaseConfig

/**
  * @author phaneesh
  */
case class SmsConfig(configName: String, provider: SmsProviderConfig) extends BaseConfig {

  def config = configName
}
