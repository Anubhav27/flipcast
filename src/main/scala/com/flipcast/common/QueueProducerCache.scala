package com.flipcast.common

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem}
import com.flipcast.rmq.RabbitMQConnectionHelper

object QueueProducerCache {

  private val registry = new ConcurrentHashMap[String, ActorRef]()

  def producer(config: String) (implicit system: ActorSystem) = {
    registry.containsKey(config) match {
      case true =>
        registry.get(config)
      case false =>
        val producerRef = RabbitMQConnectionHelper
          .createProducer(config +"_queue", config +"_exchange", "direct", None, 0, false)
        registry.put(config, producerRef)
        producerRef
    }
  }
}