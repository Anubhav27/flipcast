package com.flipcast.services

import com.flipcast.common.{BaseHttpService, BaseHttpServiceWorker, QueueProducerCache}
import com.flipcast.model.requests.{ServiceRequest, SmsUnicastRequest, UnicastRequest}
import com.flipcast.model.responses.{ServiceBadRequestResponse, ServiceNotFoundResponse, ServiceSuccessResponse, ServiceUnhandledResponse, _}
import com.flipcast.push.common.{DeviceDataSourceManager, PushMessageTransformerRegistry}
import com.flipcast.push.model.requests.FlipcastPushRequest
import com.flipcast.push.model.{DeviceOperatingSystemType, PushMessage}
import com.flipcast.push.protocol.{FlipcastPushProtocol, PushMessageProtocol}
import com.flipcast.rmq.RabbitMQConnectionHelper
import com.flipcast.sms.model.requests.FlipcastSmsRequest
import com.flipcast.sms.protocol.FlipcastSmsProtocol
import com.github.sstone.amqp.Amqp.Publish
import spray.json._

/**
 * HTTP service for unicast push requests
 *
 * @author Phaneesh Nagaraja
 */
class UnicastHttpService (implicit val context: akka.actor.ActorRefFactory,
                          implicit val serviceRegistry: ServiceRegistry) extends BaseHttpService
                          with PushMessageProtocol {

  def actorRefFactory = context

  def worker = UnicastHttpServiceWorker

  val unicastRoute = path("flipcast" / "push" / "unicast" / Segment / Segment / Segment) {
    (configName: String, filterKeys: String, filterValues: String) => {
      post { ctx =>
        implicit val reqCtx = ctx
        val keys = filterKeys.split(",")
        val values = filterValues.split(",")
        val selectKeys = List.range(0, keys.length).map( i => keys(i) -> values(i)).toMap
        val payload = try {
          Left(JsonParser(ctx.request.entity.asString).convertTo[PushMessage])
        } catch {
          case ex: Exception =>
            log.error("Error converting message payload: ", ex)
            Right(ex)
        }
        payload.isLeft match {
          case true => worker.execute(ServiceRequest[UnicastRequest](UnicastRequest(configName, selectKeys, payload.left.get)))
          case false => worker.execute(ServiceBadRequestResponse(payload.right.get.getMessage))
        }
      }
    }
  } ~
  path("flipcast" / "sms" / "unicast") {
    post { ctx=>
      implicit val reqCtx = ctx
      val payload = try {
        Left(JsonParser(ctx.request.entity.asString).convertTo[SmsUnicastRequest])
      } catch {
        case ex: Exception =>
          log.error("Error converting message payload: ", ex)
          Right(ex)
      }
      payload.isLeft match {
        case true => worker.execute(ServiceRequest[SmsUnicastRequest](payload.left.get))
        case false => worker.execute(ServiceBadRequestResponse(payload.right.get.getMessage))
      }
    }
  }
}


object UnicastHttpServiceWorker extends BaseHttpServiceWorker with FlipcastPushProtocol with FlipcastSmsProtocol {

  def process[T](request: T) = {
    request match {
      case request: UnicastRequest =>
        val deviceResponse = DeviceDataSourceManager.dataSource(request.configName).get(request.configName, request.filter)
        deviceResponse match {
          case Some(device) =>
            val messagePayload = PushMessageTransformerRegistry.transformer(request.configName).transform(request.configName,
              request.message.message)
            device.osName match {
              case DeviceOperatingSystemType.ANDROID =>
                val framedMessage = FlipcastPushRequest(request.configName, List(device.cloudMessagingId),
                  messagePayload.getPayload(DeviceOperatingSystemType.ANDROID).getOrElse("{}"), request.message.ttl,
                  request.message.delayWhileIdle, request.message.priority)
                val configKey = "gcm_" +request.message.priority.getOrElse("default")
                QueueProducerCache.producer(configKey) ! Publish(configKey +"_exchange", configKey, framedMessage.toJson.compactPrint.getBytes, RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
                ServiceSuccessResponse[UnicastSuccessResponse](UnicastSuccessResponse(device.deviceId, device.osName.toString))
              case DeviceOperatingSystemType.iOS =>
                val framedMessage = FlipcastPushRequest(request.configName, List(device.cloudMessagingId),
                  messagePayload.getPayload(DeviceOperatingSystemType.iOS).getOrElse("{}"), request.message.ttl,
                  request.message.delayWhileIdle, request.message.priority)
                val configKey = "apns_" +request.message.priority.getOrElse("default")
                QueueProducerCache.producer(configKey) ! Publish(configKey +"_exchange", configKey, framedMessage.toJson.compactPrint.getBytes, RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
                ServiceSuccessResponse[UnicastSuccessResponse](UnicastSuccessResponse(device.deviceId, device.osName.toString))
              case DeviceOperatingSystemType.WindowsPhone =>
                val framedMessage = FlipcastPushRequest(request.configName, List(device.cloudMessagingId),
                  messagePayload.getPayload(DeviceOperatingSystemType.WindowsPhone).getOrElse("{}"), request.message.ttl,
                  request.message.delayWhileIdle, request.message.priority)
                val configKey = "mpns_" +request.message.priority.getOrElse("default")
                QueueProducerCache.producer(configKey) ! Publish(configKey +"_exchange", configKey, framedMessage.toJson.compactPrint.getBytes, RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
                ServiceSuccessResponse[UnicastSuccessResponse](UnicastSuccessResponse(device.deviceId, device.osName.toString))
              case _ =>
                ServiceBadRequestResponse("Invalid device type: " +device.osName.toString)
            }
          case _ =>
            ServiceNotFoundResponse("Device not found for: " +request.filter.map( f => f._1 +"->" +f._2).mkString(" / "))
        }
      case request: SmsUnicastRequest =>
        val framedMessage = FlipcastSmsRequest(request.configName, List(request.to), request.message, Some(request.configName))
        val configKey = request.provider +"_" +request.configName
        QueueProducerCache.producer(configKey) ! Publish(configKey +"_exchange", configKey, framedMessage.toJson.compactPrint.getBytes, RabbitMQConnectionHelper.messageProperties, mandatory = false, immediate = false)
        ServiceSuccessResponse[SmsUnicastSuccessResponse](SmsUnicastSuccessResponse(request.to))
      case bad: ServiceBadRequestResponse =>
        log.info("Invalid request to worker!!:" +bad)
        bad
      case _ =>
        ServiceUnhandledResponse()
    }
  }
}