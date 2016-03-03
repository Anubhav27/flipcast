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
        val profile = Profiles.profile(config).get
        val producerRef = RabbitMQConnectionHelper
          .createProducer(profile.queue, profile.exchange, profile.exchangeType, None, 0, profile.delayedDelivery.getOrElse(false))
        registry.put(config, producerRef)
        producerRef
    }
  }
}