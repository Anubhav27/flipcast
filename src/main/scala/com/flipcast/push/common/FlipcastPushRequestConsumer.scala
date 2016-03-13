package com.flipcast.push.common

import com.flipcast.Flipcast
import com.flipcast.common.FlipcastRequestConsumer
import com.flipcast.push.model.requests.FlipcastPushRequest
import com.flipcast.push.protocol.FlipcastPushProtocol
import com.flipcast.rmq.RabbitMQConnectionHelper
import com.github.sstone.amqp.Amqp.Publish
import spray.json._

/**
 * A simple message consumer actor for consuming RMQ messages
 *
 * @author Phaneesh Nagaraja
 */
trait  FlipcastPushRequestConsumer extends FlipcastRequestConsumer[FlipcastPushRequest] with FlipcastPushProtocol {

  implicit val executionContext = context.dispatcher

  def consume(message: FlipcastPushRequest) : Boolean


  override def sideline(message: FlipcastPushRequest) {
    sidelineChannel ! Publish(configType() +"_" +priority +"_sideline_exchange",
      configType() +"_" +priority +"_sideline", message.toJson.compactPrint.getBytes,
      RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
  }

  override def resend(message: FlipcastPushRequest) {
    resendChannelRef ! Publish(configType() +"_" +priority +"_exchange",
        configType() +"_" +priority, message.toJson.compactPrint.getBytes,
        RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
  }

  def messageData(data: Array[Byte]) : FlipcastPushRequest = JsonParser(data).convertTo[FlipcastPushRequest]

}
