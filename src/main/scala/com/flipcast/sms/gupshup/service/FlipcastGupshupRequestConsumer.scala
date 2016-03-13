package com.flipcast.sms.gupshup.service

import java.net.URLEncoder

import com.flipcast.sms.common.FlipcastSmsRequestConsumer
import com.flipcast.sms.model.requests.FlipcastSmsRequest
import com.flipcast.sms.protocol.FlipcastSmsProtocol
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse, StatusCodes}

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
    message.mobileNumbers.map(no => {
      val request_uri = String.format("&msg=%s&send_to=%s",
        URLEncoder.encode(message.message, "UTF-8"), URLEncoder.encode(no, "UTF-8"))
      val gupshupResponse = pipeline(
          Get(service.uri + request_uri)
      )
      val gupshupResult = Await.result(gupshupResponse, DEFAULT_TIMEOUT)
      gupshupResult.status == StatusCodes.OK
    })
    true
  }


}
