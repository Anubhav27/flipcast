package com.flipcast.sms.common

import com.flipcast.common.FlipcastRequestConsumer
import com.flipcast.rmq.RabbitMQConnectionHelper
import com.flipcast.sms.model.requests.FlipcastSmsRequest
import com.flipcast.sms.protocol.FlipcastSmsProtocol
import com.github.sstone.amqp.Amqp.Publish
import spray.json.{JsonParser, _}

/**
  * Created by phaneesh on 13/3/16.
  */
trait FlipcastSmsRequestConsumer  extends FlipcastRequestConsumer[FlipcastSmsRequest] with FlipcastSmsProtocol {

  implicit val executionContext = context.dispatcher

  def consume(message: FlipcastSmsRequest) : Boolean


  override def sideline(message: FlipcastSmsRequest) {
    sidelineChannel ! Publish(configType() +"_" +priority +"_sideline_exchange",
      configType() +"_" +priority +"_sideline", message.toJson.compactPrint.getBytes,
      RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
  }

  override def resend(message: FlipcastSmsRequest) {
    resendChannelRef ! Publish(configType() +"_" +priority +"_exchange",
      configType() +"_" +priority, message.toJson.compactPrint.getBytes,
      RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
  }

  def messageData(data: Array[Byte]) : FlipcastSmsRequest = JsonParser(data).convertTo[FlipcastSmsRequest]

}
