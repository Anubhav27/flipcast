package com.flipcast.sms.gupshup.service

import java.net.URLEncoder

import com.flipcast.Flipcast
import com.flipcast.sms.common.{SmsTemplatePool, FlipcastSmsRequestConsumer}
import com.flipcast.sms.model.requests.FlipcastSmsRequest
import com.flipcast.sms.protocol.FlipcastSmsProtocol
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse, StatusCodes}
import spray.json.JsonParser

import scala.concurrent.{Await, Future}

/**
  * @author phaneesh
  */
class FlipcastGupshupRequestConsumer(priorityTag: String) extends FlipcastSmsRequestConsumer with FlipcastSmsProtocol {

  override def configType() = "gupshup"

  override def priority(): String = priorityTag

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  override def init(): Unit = {

  }

  override def consume(message: FlipcastSmsRequest): Boolean = {
    val service = GupShupServicePool.service(message.configName)
    val config = Flipcast.smsConfigurationProvider.config(message.configName)
    val messagedata = config.templated match {
      case true =>
        val template  = SmsTemplatePool.template(message.configName)
        SmsTemplatePool.engine.layout(template.source.uri, template.template, JsonParser(message.message).convertTo[Map[String, Any]])
      case false =>  message.message
    }
    message.mobileNumbers.map(no => {
      val request_uri = String.format("&msg=%s&send_to=%s",
        URLEncoder.encode(messagedata, "UTF-8"), URLEncoder.encode(no, "UTF-8"))
      val gupshupResponse = pipeline(
          Get(service.uri + request_uri)
      )
      val gupshupResult = Await.result(gupshupResponse, DEFAULT_TIMEOUT)
      log.info("Gupshup Response: -> Status: {} | Resposne: {}", gupshupResult.status.intValue, gupshupResult.entity.asString)
      gupshupResult.status == StatusCodes.OK
    })
    true
  }


}