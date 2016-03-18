package com.flipcast.sms.protocol

import com.flipcast.model.requests.SmsUnicastRequest
import com.flipcast.sms.model.requests.FlipcastSmsRequest
import spray.httpx.SprayJsonSupport
import spray.json._

import scala.util.Try

/**
  * @author phaneesh
  */
trait FlipcastSmsProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val FlipcastSmsRequestFormat = jsonFormat4(FlipcastSmsRequest)

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[String, _] => mapFormat[String, Any].write(m)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
    }

    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
    }
  }



}
