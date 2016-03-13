package com.flipcast.common

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem}
import com.flipcast.Flipcast
import com.flipcast.rmq.RabbitMQConnectionHelper
import com.google.common.cache.{CacheLoader, CacheBuilder}

object QueueProducerCache {

  implicit val system = Flipcast.system

  private val registry = new ConcurrentHashMap[String, ActorRef]()

  private lazy val producerCache = CacheBuilder.newBuilder()
    .maximumSize(5000)
    .concurrencyLevel(30)
    .recordStats()
    .build(
      new CacheLoader[String, ActorRef]() {
        def load(config: String) = {
          RabbitMQConnectionHelper
            .createProducer(config, config +"_exchange", "direct", None, 0, delayedDelivery = false)
        }
      })

  def producer(config: String) (implicit system: ActorSystem) = {
    producerCache.get(config)
  }
}