package com.flipcast.push.mariadb

import java.util.Date

import akka.event.slf4j.Logger
import com.flipcast.push.common.DeviceDataSource
import com.flipcast.push.mariadb.entity.Device
import com.flipcast.push.model.{DeviceOperatingSystemType, DeviceData, PushHistoryData, SidelinedMessage}
import com.flipcast.push.protocol.DeviceDataProtocol
import scalikejdbc.AutoSession
import spray.json.JsonParser

/**
  * Created by phaneesh on 11/3/16.
  */
object MariadbDeviceDataSource extends DeviceDataSource with DeviceDataProtocol {

  val log = Logger("MariadbDeviceDataSource")

  implicit val session = AutoSession

  override def init(): Unit = {
    log.info("Initializing Mairadb DataSource..")
  }

  override def sidelineMessage(message: SidelinedMessage): Boolean = {
    //TODO: Implement foxtrot push
    true
  }

  override def count(config: String, filter: Map[String, Any]): Long = {
    Device.countBy(config, buildWhereFromFilter(filter))
  }

  override def autoUpdateDeviceId(config: String, deviceIdentifier: String, newDeviceIdentifier: String): Boolean = {
    Device.autoUpdateDeviceId(config, deviceIdentifier, newDeviceIdentifier)
  }

  override def get(config: String, filter: Map[String, Any]): Option[DeviceData] = {
    val result = Device.findBy(config, buildWhereFromFilter(filter))
    result match {
      case Some(device) =>
        Option(
          deviceToDeviceData(device)
        )
      case _ => None
    }
  }

  override def recordHistory(config: String, key: String, message: String): Boolean = {
    //TODO: Implement with foxtrot
    true
  }

  override def pushHistory(config: String, from: Date): PushHistoryData = {
    //TODO: Implement with foxtrot
    PushHistoryData(0, Map.empty)
  }

  override def register(config: String, deviceData: String, filter: Map[String, Any]): DeviceData = {
    val data = JsonParser(deviceData).convertTo[DeviceData]
    val deviceExists = Device.findByDeviceId(config, data.deviceId)
    deviceExists match {
      case Some(device) =>
        Device.save(device.id, data.configName, data.deviceId, data.cloudMessagingId, data.osName.toString, data.osVersion, data.brand,
          data.model, data.appName, data.appVersion)
        data
      case _ =>
        val newDevice = Device.create(
          configName = config,
          deviceId = data.deviceId,
          cloudMessagingId = Option(data.cloudMessagingId),
          osName = data.osName.toString,
          osVersion = data.osVersion,
          brand = data.brand,
          model = data.model,
          appName = data.appName,
          appVersion = data.appVersion
        )
        deviceToDeviceData(newDevice)
    }
  }

  override def doHouseKeeping(config: String, deviceIdentifier: String): Boolean = {
    Device.deleteBy(config, buildWhereFromFilter(Map("device_id" -> deviceIdentifier)))
  }

  override def list(config: String, filter: Map[String, Any], pageSize: Int, pageNo: Int): List[DeviceData] = {
    Device.listBy(config, buildWhereFromFilter(filter), pageSize, pageNo).par.map(deviceToDeviceData).toList
  }

  override def unregister(config: String, filter: Map[String, Any]): Boolean = Device.deleteBy(config, buildWhereFromFilter(filter))

  override def listSidelineMessage(config: String, filter: Map[String, Any], pageSize: Int, pageNo: Int): List[SidelinedMessage] = List.empty

  override def listAll(config: String, pageSize: Int, pageNo: Int): List[DeviceData] = {
    Device.listAll(config, pageSize, pageNo).par.map(deviceToDeviceData).toList
  }

  def buildWhereFromFilter(filter: Map[String, Any]) = {
    filter.map { case (key, v) =>
      val value = v match {
        case x: String => "'" + x + "'"
        case x: Int => x.toString
        case x: Long => x.toString
        case x: Double => x.toString
        case x: Boolean => x.toString
        case _ => v.toString
      }
      "d." +camelToUnderscores(key) + " = " + v
    }.mkString(" and ")
  }

  def deviceToDeviceData(device: Device) = {
    DeviceData(
      configName = device.configName,
      deviceId = device.deviceId,
      cloudMessagingId = device.cloudMessagingId.orNull,
      osName = DeviceOperatingSystemType.withName(device.osName),
      osVersion = device.osVersion,
      brand = device.brand,
      model = device.model,
      appName = device.appName,
      appVersion = device.appVersion
    )
  }

  private def camelToUnderscores(name: String) = "[A-Z\\d]".r.replaceAllIn(name, {m =>
    "_" + m.group(0).toLowerCase()
  })
}
