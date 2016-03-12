package com.flipcast.push.mariadb.entity

import java.util.UUID

import scalikejdbc._
import org.joda.time.{DateTime}

case class Device(
  id: String,
  configName: String,
  deviceId: String,
  cloudMessagingId: Option[String] = None,
  osName: String,
  osVersion: String,
  brand: String,
  model: String,
  appName: String,
  appVersion: String,
  created: DateTime,
  updated: DateTime,
  deleted: Boolean) {

//  def save()(implicit session: DBSession = Device.autoSession): Device = Device.save(this)(session)

  def destroy()(implicit session: DBSession = Device.autoSession): Unit = Device.destroy(this)(session)

}


object Device extends SQLSyntaxSupport[Device] {

  override val schemaName = Some("flipcast")

  override val tableName = "devices"

  override val columns = Seq("id", "config_name", "device_id", "cloud_messaging_id", "os_name", "os_version", "brand", "model", "app_name", "app_version", "created", "updated", "deleted")

  def apply(d: SyntaxProvider[Device])(rs: WrappedResultSet): Device = apply(d.resultName)(rs)
  def apply(d: ResultName[Device])(rs: WrappedResultSet): Device = new Device(
    id = rs.get(d.id),
    configName = rs.get(d.configName),
    deviceId = rs.get(d.deviceId),
    cloudMessagingId = rs.get(d.cloudMessagingId),
    osName = rs.get(d.osName),
    osVersion = rs.get(d.osVersion),
    brand = rs.get(d.brand),
    model = rs.get(d.model),
    appName = rs.get(d.appName),
    appVersion = rs.get(d.appVersion),
    created = rs.get(d.created),
    updated = rs.get(d.updated),
    deleted = rs.get(d.deleted)
  )

  val d = Device.syntax("d")

  override val autoSession = AutoSession

  def find(id: String)(implicit session: DBSession = autoSession): Option[Device] = {
    withSQL {
      select.from(Device as d).where.eq(d.id, id)
    }.map(Device(d.resultName)).single.apply()
  }

  def findById(id: String, config: String, deleted: Boolean = false)(implicit session: DBSession = autoSession): Option[Device] = {
    withSQL {
      select.from(Device as d).where.eq(d.deviceId, id).and.eq(d.configName, config).and.eq(d.deleted, deleted)
    }.map(Device(d.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Device] = {
    withSQL(select.from(Device as d)).map(Device(d.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Device as d)).map(rs => rs.long(1)).single.apply().get
  }

//  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Device] = {
//    withSQL {
//      select.from(Device as d).where.append(where)
//    }.map(Device(d.resultName)).single.apply()
//  }

  def findBy(config: String, where: String)(implicit session: DBSession = autoSession): Option[Device] = {
    withSQL {
      select.from(Device as d).where.eq(d.configName, config).and.append(SQLSyntax.createUnsafely(where))
    }.map(Device(d.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Device] = {
    withSQL {
      select.from(Device as d).where.append(where)
    }.map(Device(d.resultName)).list.apply()
  }

  def listAll(config: String, pageSize: Int, pageNo: Int)(implicit session: DBSession = autoSession) : List[Device] = {
    withSQL {
      select.from(Device as d).where.eq(d.configName, config).limit(pageSize).offset(pageNo * pageSize)
    }.map(Device(d.resultName)).list.apply()
  }

  def listBy(config: String, where: String, pageSize: Int, pageNo: Int)(implicit session: DBSession = autoSession) : List[Device] = {
    withSQL {
      select.from(Device as d).where.eq(d.configName, config).limit(pageSize).offset(pageNo * pageSize)
    }.map(Device(d.resultName)).list.apply()
  }

  def findByDeviceId(config: String, deviceId: String)(implicit session: DBSession = autoSession) : Option[Device] = {
    val result = withSQL {
      select.from(Device as d).where.eq(d.configName, config).and.eq(column.deviceId, deviceId)
    }.map(Device(d.resultName)).single.apply()
    result
  }



  //  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
//    withSQL {
//      select(sqls.count).from(Device as d).where.append(where)
//    }.map(_.long(1)).single.apply().get
//  }

  def countBy(config: String, where: String)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Device as d).where.eq(d.configName, config).and.append(SQLSyntax.createUnsafely(where))
    }.map(_.long(1)).single.apply().get
  }

  def create(
              id: String = UUID.randomUUID().toString,
              configName: String,
              deviceId: String,
              cloudMessagingId: Option[String] = None,
              osName: String,
              osVersion: String,
              brand: String,
              model: String,
              appName: String,
              appVersion: String,
              created: DateTime = DateTime.now(),
              updated: DateTime = DateTime.now(),
              deleted: Boolean = false)(implicit session: DBSession = autoSession): Device = {
    withSQL {
      insert.into(Device).columns(
        column.id,
        column.configName,
        column.deviceId,
        column.cloudMessagingId,
        column.osName,
        column.osVersion,
        column.brand,
        column.model,
        column.appName,
        column.appVersion,
        column.created,
        column.updated,
        column.deleted
      ).values(
        id,
        configName,
        deviceId,
        cloudMessagingId,
        osName,
        osVersion,
        brand,
        model,
        appName,
        appVersion,
        created,
        updated,
        deleted
      )
    }.update.apply()

    Device(
      id = id,
      configName = configName,
      deviceId = deviceId,
      cloudMessagingId = cloudMessagingId,
      osName = osName,
      osVersion = osVersion,
      brand = brand,
      model = model,
      appName = appName,
      appVersion = appVersion,
      created = created,
      updated = updated,
      deleted = deleted)
  }

  def batchInsert(entities: Seq[Device])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'id -> entity.id,
        'configName -> entity.configName,
        'deviceId -> entity.deviceId,
        'cloudMessagingId -> entity.cloudMessagingId,
        'osName -> entity.osName,
        'osVersion -> entity.osVersion,
        'brand -> entity.brand,
        'model -> entity.model,
        'appName -> entity.appName,
        'appVersion -> entity.appVersion,
        'created -> entity.created,
        'updated -> entity.updated,
        'deleted -> entity.deleted))
        SQL("""insert into devices(
        id,
        config_name,
        device_id,
        cloud_messaging_id,
        os_name,
        os_version,
        brand,
        model,
        app_name,
        app_version,
        created,
        updated,
        deleted
      ) values (
        {id},
        {configName},
        {deviceId},
        {cloudMessagingId},
        {osName},
        {osVersion},
        {brand},
        {model},
        {appName},
        {appVersion},
        {created},
        {updated},
        {deleted}
      )""").batchByName(params: _*).apply()
    }

  def save(id: String, configName: String,
           deviceId: String, cloudMessagingId: String,
           osName: String, osVersion: String, brand: String, model: String,
           appName: String, appVersion: String)(implicit session: DBSession = autoSession): Boolean = {
    val updatedTime = DateTime.now
    val result = withSQL {
      update(Device).set(
        column.configName -> configName,
        column.deviceId -> deviceId,
        column.cloudMessagingId -> cloudMessagingId,
        column.osName -> osName,
        column.osVersion -> osVersion,
        column.brand -> brand,
        column.model -> model,
        column.appName -> appName,
        column.appVersion -> appVersion,
        column.updated -> updatedTime,
        column.deleted -> false
      ).where.eq(column.id, id)
    }.update.apply()
    result > 0
  }

  def deleteBy(config: String, where: String)(implicit session: DBSession = autoSession): Boolean = {
    val result = withSQL {
      update(Device).set(
        column.deleted -> true,
        column.updated -> DateTime.now()
      ).where.eq(column.configName, config).and.append(SQLSyntax.createUnsafely(where))
    }.update.apply()
    result > 0
  }

  def autoUpdateDeviceId(config: String, oldCloudMessagingId: String, cloudMessagingId: String)(implicit session: DBSession = autoSession) : Boolean = {
    val result = withSQL {
      update(Device).set(
        column.cloudMessagingId -> cloudMessagingId,
        column.updated -> DateTime.now()
      ).where.eq(column.cloudMessagingId, oldCloudMessagingId).and.eq(column.configName, config)
    }.update.apply()
    result > 0
  }

  def destroy(entity: Device)(implicit session: DBSession = autoSession): Boolean = {
    withSQL { delete.from(Device).where.eq(column.id, entity.id) }.update.apply() > 0
  }


}
