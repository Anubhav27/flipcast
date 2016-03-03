package com.flipcast.push.common

import java.util.concurrent.TimeUnit._

import akka.actor.{PoisonPill, ActorRef, ActorSystem, Actor}
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.flipcast.Flipcast
import com.flipcast.common.MetricsRegistry
import com.flipcast.push.config.WorkerConfigurationManager
import com.flipcast.rmq.RabbitMQConnectionHelper
import com.github.sstone.amqp.Amqp.Publish

import scala.concurrent.duration.Duration

/**
  * Created by phaneesh on 6/2/16.
  */
abstract class BaseRequestConsumer extends Actor {

  implicit val timeout: Timeout = Duration(10, SECONDS)

  implicit val system: ActorSystem = Flipcast.system

  val DEFAULT_TIMEOUT = Duration(120, SECONDS)

  var sidelineChannel: ActorRef = null

  var consumerRef: ActorRef = null

  def consumerLatency = MetricsRegistry.timer(configType())

  def consumerIn = MetricsRegistry.meter(configType(), "Incoming")

  def consumerAck = MetricsRegistry.meter(configType(), "Ack")

  def consumerReject = MetricsRegistry.meter(configType(), "Reject")

  def consumerSideline = MetricsRegistry.meter(configType(), "Sideline")

  def configType() : String

  def config = WorkerConfigurationManager.config(configType())

  def priority() : String

  def init()

  lazy val log = Logger(configType())

  override def preStart() {
    if(sidelineChannel == null) {
      sidelineChannel = RabbitMQConnectionHelper.createProducer(configType() +"_" +priority +"_sideline", configType() +"_" +priority +"_sideline_exchange", "direct", None, -1, delayedDelivery = false)
    }
    if(consumerRef == null) {
      consumerRef = RabbitMQConnectionHelper.createProducer(configType() +"_" +priority, configType() +"_" +priority +"_exchange", "direct", None, -1, delayedDelivery = false)
    }
    init()
    log.info("Starting message consumer on: " +config.configName +" Worker: " +self.path)
  }

  override def postStop(): Unit = {
    log.info("Stopping message consumer on: " +config.configName +" Worker: " +self.path)
    if(consumerRef != null) {
      consumerRef ! PoisonPill
    }
    if(sidelineChannel != null) {
      sidelineChannel ! PoisonPill
    }
  }

  def sideline(message: Array[Byte]) {
    MetricsRegistry.SidelineGlobal.mark()
    consumerSideline.mark()
    sidelineChannel ! Publish(configType() +"_" +priority +"_sideline_exchange", configType() +"_" +priority +"_sideline", message, RabbitMQConnectionHelper.messageProperties, mandatory = false,
      immediate = false)
  }

}
