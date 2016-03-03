package com.flipcast.push.common

import java.util.{Date, UUID}

import com.flipcast.Flipcast
import com.flipcast.push.model.SidelinedMessage
import com.flipcast.push.model.requests.FlipcastPushRequest
import com.flipcast.push.protocol.FlipcastPushProtocol
import com.github.sstone.amqp.Amqp.Delivery
import spray.json._

/**
 * A simple message consumer actor for consuming RMQ messages
 *
 * @author Phaneesh Nagaraja
 */
abstract class  FlipcastRequestConsumer extends BaseRequestConsumer with FlipcastPushProtocol {


  def consume(message: FlipcastPushRequest) : Boolean


  def receive = {
    case Delivery(consumerTag, envelope, properties, body) =>
      val message = JsonParser(body).convertTo[FlipcastPushRequest]
      try {
        if(!consume(message)) {
            sideline(message)
        }
      } catch {
        case ex: Exception =>
          log.error("Error sending notification", ex)
          sideline(message)
      }
  }

  private def sideline(message: FlipcastPushRequest) {
    Flipcast.serviceRegistry.actor(config.priorityConfigs(message.priority.getOrElse("default")).sidelineWorkerName) !
      SidelinedMessage(UUID.randomUUID().toString,
        message.configName, configType(), message.toJson.compactPrint, new Date())
  }

  def resend(message: FlipcastPushRequest) {
    message match {
      case x: FlipcastPushRequest =>
        Flipcast.serviceRegistry.actor(config.priorityConfigs(x.priority.getOrElse("default")).workerName) ! message
    }
  }
}
