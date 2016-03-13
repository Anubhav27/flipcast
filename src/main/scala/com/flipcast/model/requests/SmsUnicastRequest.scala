package com.flipcast.model.requests

/**
  * @author phaneesh
  */
case class SmsUnicastRequest(configName: String, provider: String, message: String, to: String)
