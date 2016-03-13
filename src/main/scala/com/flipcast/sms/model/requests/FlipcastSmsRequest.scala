package com.flipcast.sms.model.requests

import com.flipcast.model.requests.FlipcastRequest

/**
  * @author phaneesh
  */
case class FlipcastSmsRequest(configName: String, mobileNumbers: List[String], message: String,  priority: Option[String]) extends FlipcastRequest