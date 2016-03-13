package com.flipcast.push.config

import com.flipcast.config.BaseConfig

/**
 * PushConfig
 *
 * @author Phaneesh Nagaraja
 */
case class PushConfig(configName: String, gcm: GcmConfig, apns: ApnsConfig, mpns: MpnsConfig) extends BaseConfig {

  def config() = configName
}
