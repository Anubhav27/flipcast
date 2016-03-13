package com.flipcast.push.service

import java.util.{Date, UUID}

import com.flipcast.Flipcast
import com.flipcast.common.BaseRequestConsumer
import com.flipcast.config.WorkerConfigurationManager
import com.flipcast.model.requests.BulkMessageRequest
import com.flipcast.protocol.BulkMessageRequestProtocol
import com.flipcast.push.common.{DeviceDataSourceManager, PushMessageTransformerRegistry}
import com.flipcast.push.model.requests.FlipcastPushRequest
import com.flipcast.push.model.{DeviceOperatingSystemType, SidelinedMessage}
import com.github.sstone.amqp.Amqp.Delivery
import spray.json.{JsonParser, _}

/**
 * Service that consumes bulk requests and batches the devices and routes it appropriate queues
 *
 * @author Phaneesh Nagaraja
 */
class BulkMessageConsumer(priorityTag: String) extends BaseRequestConsumer
                            with BulkMessageRequestProtocol {

  override def configType() = "bulk"

  override def priority(): String = priorityTag

  override def init() {

  }

  def receive = {
    case Delivery(consumerTag, envelope, properties, body) =>
      val message = JsonParser(body).convertTo[BulkMessageRequest]
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

  def consume(request: BulkMessageRequest) =  {
    val deviceResponse = request.query.isEmpty match {
      case true =>
        DeviceDataSourceManager.dataSource(request.configName)
          .listAll(request.configName, request.start, request.end).groupBy( _.osName)
      case false =>
        DeviceDataSourceManager.dataSource(request.configName)
          .list(request.configName, request.query, request.start, request.end).groupBy( _.osName)
    }
    val messagePayload = PushMessageTransformerRegistry.transformer(request.configName)
      .transform(request.configName, request.message.message)
    deviceResponse.contains(DeviceOperatingSystemType.ANDROID) match {
      case true =>
        deviceResponse(DeviceOperatingSystemType.ANDROID).grouped(100).foreach( dList => {
          val deviceIds = dList.map( _.cloudMessagingId).toList
          val framedMessage = FlipcastPushRequest(request.configName, deviceIds,
            messagePayload.getPayload(DeviceOperatingSystemType.ANDROID).getOrElse("{}"), request.message.ttl,
            request.message.delayWhileIdle, request.message.priority)
          Flipcast.serviceRegistry.actor(WorkerConfigurationManager.worker("gcm", request.message.priority.getOrElse("default"))) ! framedMessage
        })
      case false =>
        log.warn("No Android devices in batch for request: " +request)
    }
    deviceResponse.contains(DeviceOperatingSystemType.iOS) match {
      case true =>
        deviceResponse(DeviceOperatingSystemType.iOS).grouped(100).foreach( dList => {
          val deviceIds = dList.map( _.cloudMessagingId)
          val framedMessage = FlipcastPushRequest(request.configName, deviceIds,
            messagePayload.getPayload(DeviceOperatingSystemType.iOS).getOrElse("{}"), request.message.ttl,
            request.message.delayWhileIdle, request.message.priority)
          Flipcast.serviceRegistry.actor(WorkerConfigurationManager.worker("apns", request.message.priority.getOrElse("default"))) ! framedMessage
        })
      case false =>
        log.warn("No iOS devices in batch for request: " +request)
    }
    deviceResponse.contains(DeviceOperatingSystemType.WindowsPhone) match {
      case true =>
        deviceResponse(DeviceOperatingSystemType.WindowsPhone).grouped(100).foreach( dList => {
          val deviceIds = dList.map( _.cloudMessagingId).toList
          val framedMessage = FlipcastPushRequest(request.configName, deviceIds,
            messagePayload.getPayload(DeviceOperatingSystemType.WindowsPhone).getOrElse("{}"), request.message.ttl,
            request.message.delayWhileIdle, request.message.priority)
          Flipcast.serviceRegistry.actor(WorkerConfigurationManager.worker("apns", request.message.priority.getOrElse("default"))) ! framedMessage
        })
      case false =>
        log.warn("No Windows Phone devices in batch for request: " +request)
    }
    true
  }

  private def sideline(message: BulkMessageRequest) {
    Flipcast.serviceRegistry.actor(config.priorityConfigs(message.message.priority.getOrElse("default")).sidelineWorkerName) !
      SidelinedMessage(UUID.randomUUID().toString,
        message.configName, configType(), message.toJson.compactPrint, new Date())
  }

  def resend(message: BulkMessageRequest) {
      Flipcast.serviceRegistry.actor(config.priorityConfigs(message.message.priority.getOrElse("default")).workerName) ! message
  }
}
