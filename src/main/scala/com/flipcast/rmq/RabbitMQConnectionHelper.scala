package com.flipcast.rmq

import java.util.concurrent.Executors

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.event.slf4j.Logger
import com.flipcast.model.config.RmqConfig
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ChannelOwner, ConnectionOwner, Consumer}
import com.rabbitmq.client.{AMQP, Address, Connection, ConnectionFactory}

import scala.concurrent.duration._

/**
  * Helper to handle RabbitMQ Connections.
  */
object RabbitMQConnectionHelper {

  val log = Logger("RabbitMQConnectionHelper")

  private var connection: ActorRef = null

  private var rawRmqConnection: Connection = null

  val clientProps = Map[String, AnyRef]("ha-mode" -> "all", "x-ha-policy" -> "all", "x-priority" -> new Integer(5))


  val messageProperties = Option(new AMQP.BasicProperties.Builder()
    .contentType("application/json")
    .deliveryMode(2)
    .priority(5)
    .contentEncoding("UTF-8")
    .build())

  def init()(implicit config: RmqConfig, system: ActorSystem) {
    log.info("Starting RabbitMQ connection...")
    val connectionFactory = new ConnectionFactory()
    connectionFactory.setAutomaticRecoveryEnabled(true)
    connectionFactory.setTopologyRecoveryEnabled(true)
    connectionFactory.setVirtualHost(config.vhost)
    connectionFactory.setNetworkRecoveryInterval(5)

    connectionFactory.setPassword(config.pass)
    connectionFactory.setUsername(config.pass)
    connectionFactory.setSharedExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() * 4))
    log.info("Hosts:")
    val addresses = config.hosts.map(h => {
      log.info("\t" + h)
      val tokens = h.split(":")
      new Address(tokens(0), tokens(1).toInt)
    }).toArray
    connection = system.actorOf(ConnectionOwner.props(connectionFactory, 1 second, addresses = Option(addresses)))
    rawRmqConnection = connectionFactory.newConnection(addresses)
  }


  /**
    * Create a consumer
 *
    * @param queueName name of the queue
    * @param exchange name of the exchange
    * @param listener ActorRef of listener
    * @param system actor system that needs to be used to create the connection
    * @return ActorRef of consumer
    */
  def createConsumer(queueName: String, exchange: String, exchangeType: String, listener: ActorRef, qos: Int, deadLetter: Option[String], ttl: Int = 0, delayedDelivery: Boolean)(implicit system: ActorSystem) = {
    val exParameters = delayedDelivery match {
      case true => clientProps + ("x-delayed-type" -> exchangeType)
      case false => clientProps
    }
    val exchangeParams = delayedDelivery match {
      case true =>
        ExchangeParameters(name = exchange, passive = false,
          exchangeType = "x-delayed-message", durable = true, autodelete = false, exParameters)
      case false =>
        ExchangeParameters(name = exchange, passive = false,
          exchangeType = exchangeType, durable = true, autodelete = false, exParameters)
    }
    val cParams = Option(ChannelParameters(qos))
    val deadLetterOption = deadLetter match {
      case Some(x) => clientProps ++ Map("x-dead-letter-routing-key" -> x)
      case _ => clientProps
    }
    val ttlOption = ttl > 0 match {
      case true => deadLetterOption ++ Map("x-message-ttl" -> new Integer(ttl))
      case false => deadLetterOption
    }
    val queueParams = QueueParameters(queueName, passive = false, durable = true, exclusive = false, autodelete = false, ttlOption)
    val consumer = ConnectionOwner.createChildActor(connection, Consumer.props(listener, exchangeParams, queueParams, queueName,
      cParams, autoack = false))
    Amqp.waitForConnection(system, consumer).await()
    consumer ! DeclareExchange(exchangeParams)
    consumer ! DeclareQueue(queueParams)
    consumer ! QueueBind(queue = queueName, exchange = exchange, routing_key = queueName)
    consumer ! AddQueue(queueParams)
    consumer
  }

  /**
    * Create a producer and bind it to a exchange and a queue
 *
    * @param queueName name of the queue
    * @param exchange name of the exchange
    * @param system actor system that needs to be used to create the connection
    * @return ActorRef to a producer
    */
  def createProducer(queueName: String, exchange: String, exchangeType: String, deadLetter: Option[String], ttl: Int = 0, delayedDelivery: Boolean)(implicit system: ActorSystem) = {
    val exParameters = delayedDelivery match {
      case true => clientProps + ("x-delayed-type" -> exchangeType)
      case false => clientProps
    }
    val channelParameters = Option(ChannelParameters(1))
    val exchangeParams = delayedDelivery match {
      case true =>
        ExchangeParameters(name = exchange, passive = false,
          exchangeType = "x-delayed-message", durable = true, autodelete = false, exParameters)
      case false =>
        ExchangeParameters(name = exchange, passive = false,
          exchangeType = exchangeType, durable = true, autodelete = false, exParameters)
    }

    val producer = ConnectionOwner.createChildActor(connection, ChannelOwner.props(channelParams = channelParameters))
    Amqp.waitForConnection(system, producer).await()
    producer ! DeclareExchange(exchangeParams)
    val deadLetterOption = deadLetter match {
      case Some(x) => clientProps ++ Map("x-dead-letter-routing-key" -> x)
      case _ => clientProps
    }
    val ttlOption = ttl > 0 match {
      case true => deadLetterOption ++ Map("x-message-ttl" -> new Integer(ttl))
      case false => deadLetterOption
    }
    exchangeType match {
      case "direct" =>
        val queueParams = QueueParameters(queueName, passive = false, durable = true, exclusive = false, autodelete = false,
          ttlOption)
        producer ! DeclareQueue(queueParams)
        producer ! QueueBind(queue = queueName, exchange = exchange, routing_key = queueName)
      case _ =>
        log.warn("Not creating a default queue because exchange type is not direct")
    }
    producer
  }

  /**
    * Close all connections
    */
  def stop() {
    if (connection != null) {
      connection ! PoisonPill
    }
  }


  def rawConnection() = rawRmqConnection

  def isConnected = {
    try {
      rawRmqConnection.isOpen
    } catch {
      case ex: Exception => false
    }
  }
}