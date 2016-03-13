package com.flipcast.sms.protocol

import com.flipcast.sms.model.requests.FlipcastSmsRequest
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * @author phaneesh
  */
trait FlipcastSmsProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val FlipcastSmsRequestFormat = jsonFormat4(FlipcastSmsRequest)

}
