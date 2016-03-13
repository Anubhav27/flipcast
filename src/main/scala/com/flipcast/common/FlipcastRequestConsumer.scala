package com.flipcast.common

import com.github.sstone.amqp.Amqp.{Reject, Ack, Delivery}

/**
  * A simple message consumer actor for consuming RMQ messages
  *
  * @author Phaneesh Nagaraja
  */
abstract class FlipcastRequestConsumer[T] extends BaseRequestConsumer {

  def consume(message: T) : Boolean

  def sideline(message: T)

  def resend(message: T)

  def messageData(data: Array[Byte]): T

  def receive = {
    case Delivery(consumerTag, envelope, properties, body) =>
      val message = messageData(body)
      try {
        consume((message)) match {
          case true =>
            sender ! Ack(envelope.getDeliveryTag)
          case false =>
            sideline(message)
            sender ! Reject(envelope.getDeliveryTag, requeue = false)
        }
      } catch {
        case ex: Exception =>
          log.error("Error sending notification", ex)
          sideline(message)
      }
  }


}
